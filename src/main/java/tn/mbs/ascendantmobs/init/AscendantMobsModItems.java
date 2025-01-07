
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.mbs.ascendantmobs.init;

import tn.mbs.ascendantmobs.item.CalenderItem;
import tn.mbs.ascendantmobs.AscendantMobsMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import net.minecraft.world.item.Item;

public class AscendantMobsModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(AscendantMobsMod.MODID);
	public static final DeferredItem<Item> CALENDER = REGISTRY.register("calender", CalenderItem::new);
	// Start of user code block custom items
	// End of user code block custom items
}
