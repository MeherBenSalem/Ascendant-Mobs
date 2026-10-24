package com.nightbeam.remnants.mobs.item;

import com.nightbeam.remnants.mobs.reward.Advancements;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.item.TooltipFlag;
import javax.annotation.Nullable;
import net.minecraft.world.level.Level;

/** A spyglass that reads monsters: held, it shows the threat of the area; scoped, it shows a mob's full stat sheet. */
public class HuntersLensItem extends SpyglassItem {
    public HuntersLensItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            Advancements.award(serverPlayer, Advancements.KNOW_THY_ENEMY);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.remnant_mobs.hunters_lens.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.remnant_mobs.hunters_lens.tooltip.scope").withStyle(ChatFormatting.DARK_GRAY));
    }
}
