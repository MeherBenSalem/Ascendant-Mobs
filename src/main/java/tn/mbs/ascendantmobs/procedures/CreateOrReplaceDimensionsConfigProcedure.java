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
		filename = "dimensions";
		if (JaumlConfigLib.createConfigFile(dir, filename)) {
			JaumlConfigLib.createConfigFile(dir, filename);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "settings")) {
			JaumlConfigLib.addStringToArray(dir, filename, "settings", "[min]0[minEnd][max]75[maxEnd]minecraft:overworld");
			JaumlConfigLib.addStringToArray(dir, filename, "settings", "[min]50[minEnd][max]100[maxEnd]minecraft:the_nether");
			JaumlConfigLib.addStringToArray(dir, filename, "settings", "[min]100[minEnd][max]200[maxEnd]minecraft:the_end");
			JaumlConfigLib.addStringToArray(dir, filename, "settings", "[min]20[minEnd][max]50[maxEnd]the_bumblezone:the_bumblezone");
			JaumlConfigLib.addStringToArray(dir, filename, "settings", "[min]5[minEnd][max]10[maxEnd]twilightforest:twilight_forest");
		}
	}
}