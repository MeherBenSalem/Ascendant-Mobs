package tn.nightbeam.rpgmoblevelingsystem.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelingLogic;

import java.util.List;

public class CalenderItem extends Item {
    public CalenderItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(itemStack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.rpgmoblevelingsystem.calender.description_0"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        MobLevelingLogic.sendCalendarMessage(level, player);
        return super.use(level, player, hand);
    }
}
