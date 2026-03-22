package tn.nightbeam.rpgmoblevelingsystem.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.registry.ModItems;

public final class RpgMobLevelingSystemFabricItems {
    public static Item CALENDER;

    private RpgMobLevelingSystemFabricItems() {
    }

    public static void registerItems() {
        CALENDER = Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "calender"), ModItems.CALENDER.get());
    }
}
