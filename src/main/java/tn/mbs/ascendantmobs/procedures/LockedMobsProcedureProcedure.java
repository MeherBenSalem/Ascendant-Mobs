package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import org.checkerframework.checker.units.qual.s;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.entity.Entity;

public class LockedMobsProcedureProcedure {
	public static double execute(Entity entity) {
		if (entity == null)
			return 0;
		double level = 0;
		double count = 0;
		String line = "";
		level = -1;
		count = 0;
		for (int index0 = 0; index0 < (int) JaumlConfigLib.getArrayLength("ascendant_mobs", "mobs_list_settings", "locked_mobs"); index0++) {
			line = JaumlConfigLib.getArrayElement("ascendant_mobs", "mobs_list_settings", "locked_mobs", ((int) count));
			if (line.contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString())) {
				level = new Object() {
					double convert(String s) {
						try {
							return Double.parseDouble(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert(line.substring(0, line.indexOf("/", 0)));
				break;
			}
			count = count + 1;
		}
		return level;
	}
}