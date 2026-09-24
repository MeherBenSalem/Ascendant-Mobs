package tn.nightbeam.rpgmoblevelingsystem.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.item.CalenderItem;

public final class ModItems {
    public static final String CALENDER_ID = "calender";

    private ModItems() {
    }

    public static Item.Properties itemProperties(String name) {
        return itemProperties(Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    public static Item.Properties itemProperties(Identifier name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, name));
    }

    public static Item createCalender(String name) {
        return createCalender(Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    public static Item createCalender(Identifier name) {
        return new CalenderItem(itemProperties(name).stacksTo(1).rarity(Rarity.RARE));
    }
}
