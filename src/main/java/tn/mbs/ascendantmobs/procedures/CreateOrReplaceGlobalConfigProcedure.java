package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateOrReplaceGlobalConfigProcedure {
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		execute();
	}

	public static void execute() {
		execute(null);
	}

	private static void execute(@Nullable Event event) {
		String dir = "";
		String filename = "";
		dir = "ascendant_mobs";
		filename = "global_settings";
		if (JaumlConfigLib.createConfigFile(dir, filename)) {
			JaumlConfigLib.createConfigFile(dir, filename);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "ascendant_loot_table")) {
			JaumlConfigLib.setStringValue(dir, filename, "ascendant_loot_table", "minecraft:chests/end_city_treasure");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "ascendant_effects_min_level")) {
			JaumlConfigLib.setNumberValue(dir, filename, "ascendant_effects_min_level", 50);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "ascendant_effects_chance")) {
			JaumlConfigLib.setNumberValue(dir, filename, "ascendant_effects_chance", 0.5);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "random_effects")) {
			JaumlConfigLib.setBooleanValue(dir, filename, "random_effects", true);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "use_legacy_hud")) {
			JaumlConfigLib.setBooleanValue(dir, filename, "use_legacy_hud", true);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "hide_hud_for")) {
			JaumlConfigLib.addStringToArray(dir, filename, "hide_hud_for", "");
		}
	}
}