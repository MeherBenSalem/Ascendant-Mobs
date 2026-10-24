package com.nightbeam.remnants.mobs.item;

import com.nightbeam.remnants.mobs.reward.Advancements;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** A spyglass that reads monsters: held, it shows the threat of the area; scoped, it shows a mob's full stat sheet. */
public class HuntersLensItem extends SpyglassItem {
    public HuntersLensItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            Advancements.award(serverPlayer, Advancements.KNOW_THY_ENEMY);
        }
        return super.use(level, player, hand);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.remnant_mobs.hunters_lens.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.remnant_mobs.hunters_lens.tooltip.scope").withStyle(ChatFormatting.DARK_GRAY));
    }
}
