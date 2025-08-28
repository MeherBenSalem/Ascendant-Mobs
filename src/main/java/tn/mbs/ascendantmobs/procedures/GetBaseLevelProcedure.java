package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class GetBaseLevelProcedure {
	public static double execute(LevelAccessor world) {
		double toReturn = 0;
		double count = 0;
		String dimName = "";
		String stringToCheck = "";
		dimName = "" + (world instanceof Level _lvl ? _lvl.dimension() : (world instanceof WorldGenLevel _wgl ? _wgl.getLevel().dimension() : Level.OVERWORLD));
		toReturn = (double) MobsLevelsMainConfigConfiguration.BASE_LEVEL.get();
		count = 0;
		for (int index0 = 0; index0 < (int) (JaumlConfigLib.getArrayLength("ascendant_mobs", "dimensions_settings", "dimensions") - 1); index0++) {
			stringToCheck = JaumlConfigLib.getArrayElement("ascendant_mobs", "dimensions_settings", "dimensions", ((int) count));
			if (dimName.contains(stringToCheck.substring(stringToCheck.indexOf("[maxEnd]", 0) + 8))) {
				toReturn = new Object() {
					double convert(String s) {
						try {
							return Double.parseDouble(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert(stringToCheck.substring(5, stringToCheck.indexOf("[minEnd]", 6)));
				break;
			}
			count = count + 1;
		}
		return toReturn;
	}
}