package tn.nightbeam.rpgmoblevelingsystem.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import tn.nightbeam.rpgmoblevelingsystem.item.CalenderItem;

import java.util.function.Supplier;

public final class ModItems {
    public static final Supplier<Item> CALENDER = () -> new CalenderItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    private ModItems() {
    }
}
