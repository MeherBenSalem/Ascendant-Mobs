package tn.mbs.ascendantmobs.init;

import tn.mbs.ascendantmobs.configuration.MobsListConfigConfiguration;
import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;
import tn.mbs.ascendantmobs.configuration.CustomDimensionsConfigConfiguration;
import tn.mbs.ascendantmobs.AscendantMobsMod;

import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = AscendantMobsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AscendantMobsModConfigs {
	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobsLevelsMainConfigConfiguration.SPEC, "AscendantMobs/main_config.toml");
			ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobsListConfigConfiguration.SPEC, "AscendantMobs/list_config.toml");
			ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CustomDimensionsConfigConfiguration.SPEC, "AscendantMobs/config_dims.toml");
		});
	}
}
