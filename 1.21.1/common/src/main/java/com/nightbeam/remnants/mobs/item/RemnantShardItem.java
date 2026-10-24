package com.nightbeam.remnants.mobs.item;

import com.nightbeam.remnants.mobs.compat.RasBridge;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.config.RewardsConfig;
import com.nightbeam.remnants.mobs.content.ModContent;
import com.nightbeam.remnants.mobs.reward.Advancements;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Dropped by ranked mobs. Use to absorb it for Valor (RPG Attribute System) or experience; sneak to absorb the stack. */
public class RemnantShardItem extends Item {
    public RemnantShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }
        int count = player.isShiftKeyDown() ? stack.getCount() : 1;
        RewardsConfig rewards = RemnantsConfig.rewards();
        if (RasBridge.present() && rewards.shardValor > 0) {
            double valor = rewards.shardValor * count;
            RasBridge.addValor(serverPlayer, valor);
            serverPlayer.displayClientMessage(Component.translatable("message.remnant_mobs.shard_valor",
                    Component.literal("+" + Math.round(valor)).withStyle(ChatFormatting.GOLD)), true);
        } else {
            serverPlayer.giveExperiencePoints(rewards.shardExperience * count);
        }
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(count);
        }
        server.playSound(null, player.getX(), player.getY(), player.getZ(), ModContent.SHARD_ABSORB.get(), SoundSource.PLAYERS, 0.8F,
                0.9F + player.getRandom().nextFloat() * 0.2F);
        server.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY(0.6), player.getZ(), Math.min(24, 4 + count), 0.3, 0.4, 0.3, 0.02);
        player.getCooldowns().addCooldown(this, 4);
        Advancements.award(serverPlayer, Advancements.TASTE_OF_VALOR);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.remnant_mobs.remnant_shard.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.remnant_mobs.remnant_shard.tooltip.sneak").withStyle(ChatFormatting.DARK_GRAY));
    }
}
