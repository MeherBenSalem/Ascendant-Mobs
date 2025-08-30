package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateOrReplaceMobListConfigProcedure {
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
		filename = "mobs_list_settings";
		if (JaumlConfigLib.createConfigFile(dir, filename)) {
			JaumlConfigLib.createConfigFile(dir, filename);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "banned")) {
			JaumlConfigLib.addStringToArray(dir, filename, "banned", "");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "death_messages")) {
			JaumlConfigLib.setBooleanValue(dir, filename, "death_messages", false);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "locked_mobs")) {
			JaumlConfigLib.addStringToArray(dir, filename, "locked_mobs", "100/minecraft:ender_dragon");
			JaumlConfigLib.addStringToArray(dir, filename, "locked_mobs", "150/minecraft:wither");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "can_be_ascendant")) {
			JaumlConfigLib.addStringToArray(dir, filename, "can_be_ascendant", "minecraft:zombie");
			JaumlConfigLib.addStringToArray(dir, filename, "can_be_ascendant", "minecraft:skeleton");
		}
	}
}