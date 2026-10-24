package com.nightbeam.remnants.mobs.item;

import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.content.ModContent;
import com.nightbeam.remnants.mobs.content.Rank;
import com.nightbeam.remnants.mobs.content.Trait;
import com.nightbeam.remnants.mobs.mob.MobData;
import com.nightbeam.remnants.mobs.mob.MobLeveler;
import com.nightbeam.remnants.mobs.mob.RemnantMob;
import com.nightbeam.remnants.mobs.reward.Advancements;
import java.util.Set;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

/** Marks a leveled monster as your challenge: one rank higher, five levels stronger, another trait, double rewards. */
public class BrandOfChallengeItem extends Item {
    public static final int LEVELS = 5;

    public BrandOfChallengeItem(Properties properties) {
        super(properties);
    }

    /** Why a mob cannot be branded, or null if it can. */
    public static String refusal(MobData data) {
        if (data == null || data.flag(MobData.PASSIVE)) {
            return "message.remnant_mobs.brand.not_leveled";
        }
        if (data.flag(MobData.BOSS) || data.flag(MobData.MINION)) {
            return "message.remnant_mobs.brand.cannot";
        }
        if (data.flag(MobData.BRANDED)) {
            return "message.remnant_mobs.brand.already";
        }
        if (data.rank() == Rank.REMNANT) {
            return "message.remnant_mobs.brand.max";
        }
        return null;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Mob mob) || !mob.isAlive()) {
            return InteractionResult.PASS;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(mob.level() instanceof ServerLevel level)) {
            return RemnantMob.state(mob).data != null ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        MobData data = RemnantMob.data(mob);
        String refusal = refusal(data);
        if (refusal != null) {
            serverPlayer.displayClientMessage(Component.translatable(refusal).withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        Rank rank = data.rank().next();
        int wanted = Math.max(data.traits().size() + 1, RemnantsConfig.ranks().stats(rank).traits());
        Set<Trait> traits = MobLeveler.addTraits(mob.getRandom(), mob.getType(), rank, data.traits(), Math.max(0, wanted - data.traits().size()));
        MobData branded = new MobData(data.level() + LEVELS, rank, traits, data.seed(), data.flags() | MobData.BRANDED);
        MobLeveler.set(mob, branded);
        mob.setHealth(mob.getMaxHealth());
        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
        mob.setTarget(player);
        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModContent.BRAND_APPLY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, mob.getX(), mob.getY(0.5), mob.getZ(), 40, 0.4, 0.7, 0.4, 0.06);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, mob.getX(), mob.getY(1.0), mob.getZ(), 20, 0.4, 0.3, 0.4, 0.2);
        serverPlayer.displayClientMessage(Component.translatable("message.remnant_mobs.brand.applied",
                Component.translatable(rank.translationKey()).withStyle(style -> style.withColor(rank.color()))), true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        Advancements.award(serverPlayer, Advancements.ACCEPT_THE_CHALLENGE);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.remnant_mobs.brand_of_challenge.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.remnant_mobs.brand_of_challenge.tooltip.reward").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
