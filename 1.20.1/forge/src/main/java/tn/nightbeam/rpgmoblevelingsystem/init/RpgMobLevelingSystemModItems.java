/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.rpgmoblevelingsystem.init;

import tn.nightbeam.rpgmoblevelingsystem.item.CalenderItem;
import tn.nightbeam.rpgmoblevelingsystem.RpgMobLevelingSystemMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.Item;

public class RpgMobLevelingSystemModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, RpgMobLevelingSystemMod.MODID);
	public static final RegistryObject<Item> CALENDER = REGISTRY.register("calender", () -> new CalenderItem());
	// Start of user code block custom items
	// End of user code block custom items
}