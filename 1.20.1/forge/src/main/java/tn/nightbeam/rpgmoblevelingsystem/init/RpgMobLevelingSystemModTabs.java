/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.rpgmoblevelingsystem.init;

import tn.nightbeam.rpgmoblevelingsystem.RpgMobLevelingSystemMod;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.Registries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RpgMobLevelingSystemModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RpgMobLevelingSystemMod.MODID);

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
			tabData.accept(RpgMobLevelingSystemModItems.CALENDER.get());
		}
	}
}