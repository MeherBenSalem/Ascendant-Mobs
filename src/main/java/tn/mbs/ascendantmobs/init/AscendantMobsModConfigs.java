package tn.mbs.ascendantmobs.init;

import tn.mbs.ascendantmobs.configuration.MobsListConfigConfiguration;
import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;
import tn.mbs.ascendantmobs.configuration.CustomDimensionsConfigConfiguration;
import tn.mbs.ascendantmobs.AscendantMobsMod;

import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = AscendantMobsMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class AscendantMobsModConfigs {
	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			ModList.get().getModContainerById("ascendant_mobs").get().registerConfig(ModConfig.Type.COMMON, MobsLevelsMainConfigConfiguration.SPEC, "AscendantMobs/main_config.toml");
			ModList.get().getModContainerById("ascendant_mobs").get().registerConfig(ModConfig.Type.COMMON, MobsListConfigConfiguration.SPEC, "AscendantMobs/list_config.toml");
			ModList.get().getModContainerById("ascendant_mobs").get().registerConfig(ModConfig.Type.COMMON, CustomDimensionsConfigConfiguration.SPEC, "AscendantMobs/config_dims.toml");
		});
	}
}
