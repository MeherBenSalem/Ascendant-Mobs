package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class GetMaxLevelProcedure {
	public static double execute(LevelAccessor world) {
		double toReturn = 0;
		double count = 0;
		String dimName = "";
		String Line = "";
		dimName = "" + (world instanceof Level _lvl ? _lvl.dimension() : (world instanceof WorldGenLevel _wgl ? _wgl.getLevel().dimension() : Level.OVERWORLD));
		toReturn = JaumlConfigLib.getNumberValue("ascendant_mobs", "scale_settings", "base_level");
		count = 0;
		for (int index0 = 0; index0 < (int) JaumlConfigLib.getArrayLength("ascendant_mobs", "dimensions_settings", "dimensions"); index0++) {
			Line = JaumlConfigLib.getArrayElement("ascendant_mobs", "dimensions_settings", "dimensions", ((int) count));
			if (dimName.contains(Line.substring(Line.indexOf("[maxEnd]", 0) + 8))) {
				toReturn = new Object() {
					double convert(String s) {
						try {
							return Double.parseDouble(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert(Line.substring(Line.indexOf("[max]", 0) + 5, Line.indexOf("[maxEnd]", 0)));
				break;
			}
			count = count + 1;
		}
		return toReturn;
	}
}