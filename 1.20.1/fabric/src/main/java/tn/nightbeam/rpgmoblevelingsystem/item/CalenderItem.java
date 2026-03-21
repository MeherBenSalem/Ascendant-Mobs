package tn.nightbeam.rpgmoblevelingsystem.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import tn.nightbeam.rpgmoblevelingsystem.fabric.gameplay.FabricMobLeveling;

import java.util.List;

public class CalenderItem extends Item {
    public CalenderItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, level, list, flag);
        list.add(Component.translatable("item.rpgmoblevelingsystem.calender.description_0"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        FabricMobLeveling.sendCalendarMessage(world, entity);
        return super.use(world, entity, hand);
    }
}
