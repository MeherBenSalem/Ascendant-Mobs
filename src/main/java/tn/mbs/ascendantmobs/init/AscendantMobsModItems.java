/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.mbs.ascendantmobs.init;

import tn.mbs.ascendantmobs.item.CalenderItem;
import tn.mbs.ascendantmobs.AscendantMobsMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.Item;

public class AscendantMobsModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, AscendantMobsMod.MODID);
	public static final RegistryObject<Item> CALENDER = REGISTRY.register("calender", () -> new CalenderItem());
	// Start of user code block custom items
	// End of user code block custom items
}