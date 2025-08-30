package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateOrReplaceAttributesConfigProcedure {
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
		filename = "attributes_settings";
		if (JaumlConfigLib.createConfigFile(dir, filename)) {
			JaumlConfigLib.createConfigFile(dir, filename);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, filename, "attributes_list")) {
			JaumlConfigLib.addStringToArray(dir, filename, "attributes_list", "[attribute]minecraft:generic.max_health[attributeEnd][value]2.0[valueEnd][max]200[maxEnd]");
			JaumlConfigLib.addStringToArray(dir, filename, "attributes_list", "[attribute]minecraft:generic.movement_speed[attributeEnd][value]0.0015[valueEnd][max]0.15[maxEnd]");
			JaumlConfigLib.addStringToArray(dir, filename, "attributes_list", "[attribute]minecraft:generic.armor[attributeEnd][value]0.1[valueEnd][max]10[maxEnd]");
			JaumlConfigLib.addStringToArray(dir, filename, "attributes_list", "[attribute]minecraft:generic.attack_damage[attributeEnd][value]1.0[valueEnd][max]100[maxEnd]");
		}
	}
}