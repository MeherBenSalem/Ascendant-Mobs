package tn.nightbeam.rpgmoblevelingsystem.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.registry.ModItems;

public final class RpgMobLevelingSystemFabricItems {
    public static Item CALENDER;

    private RpgMobLevelingSystemFabricItems() {
    }

    public static void registerItems() {
        CALENDER = Registry.register(
                BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, ModItems.CALENDER_ID),
                ModItems.createCalender(ModItems.CALENDER_ID));
    }
}
