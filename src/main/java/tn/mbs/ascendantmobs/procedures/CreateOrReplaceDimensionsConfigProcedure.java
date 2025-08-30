package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateOrReplaceDimensionsConfigProcedure {
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
		filename = "dimensions_settings";
		if (JaumlConfigLib.createConfigFile(dir, filename)) {
			JaumlConfigLib.createConfigFile(dir, filename);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "settings")) {
			JaumlConfigLib.addStringToArray(dir, filename, "dimensions", "[min]0[minEnd][max]40[maxEnd]minecraft:overworld");
			JaumlConfigLib.addStringToArray(dir, filename, "dimensions", "[min]20[minEnd][max]80[maxEnd]minecraft:the_nether");
			JaumlConfigLib.addStringToArray(dir, filename, "dimensions", "[min]40[minEnd][max]100[maxEnd]minecraft:the_end");
			JaumlConfigLib.addStringToArray(dir, filename, "dimensions", "[min]10[minEnd][max]60[maxEnd]aether:the_aether");
			JaumlConfigLib.addStringToArray(dir, filename, "dimensions", "[min]10[minEnd][max]60[maxEnd]twilightforest:twilight_forest");
			JaumlConfigLib.addStringToArray(dir, filename, "dimensions", "[min]10[minEnd][max]60[maxEnd]eternal_starlight:starligh");
			JaumlConfigLib.addStringToArray(dir, filename, "dimensions", "[min]10[minEnd][max]60[maxEnd]deeperdarker:otherside");
		}
	}
}