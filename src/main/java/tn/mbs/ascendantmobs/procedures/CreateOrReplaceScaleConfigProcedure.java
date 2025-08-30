package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateOrReplaceScaleConfigProcedure {
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
		filename = "scale_settings";
		if (JaumlConfigLib.createConfigFile(dir, filename)) {
			JaumlConfigLib.createConfigFile(dir, filename);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "scale_type")) {
			JaumlConfigLib.setStringValue(dir, filename, "scale_type", "both");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "base_level")) {
			JaumlConfigLib.setNumberValue(dir, filename, "base_level", 0);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "scale_factor")) {
			JaumlConfigLib.setNumberValue(dir, filename, "scale_factor", 0.5);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "scale_distance")) {
			JaumlConfigLib.setNumberValue(dir, filename, "scale_distance", 55000);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "day_factor")) {
			JaumlConfigLib.setNumberValue(dir, filename, "day_factor", 1);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "xp_modfier")) {
			JaumlConfigLib.setNumberValue(dir, filename, "xp_modfier", 5);
		}
	}
}