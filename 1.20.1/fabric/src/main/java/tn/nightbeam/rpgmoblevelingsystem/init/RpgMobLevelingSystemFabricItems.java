package tn.nightbeam.rpgmoblevelingsystem.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.item.CalenderItem;

public class RpgMobLevelingSystemFabricItems {
    public static final Item CALENDER = new CalenderItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static void registerItems() {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Constants.MOD_ID, "calender"), CALENDER);
    }
}
