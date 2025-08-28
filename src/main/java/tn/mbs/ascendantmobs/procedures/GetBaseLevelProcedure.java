package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;
import tn.mbs.ascendantmobs.configuration.CustomDimensionsConfigConfiguration;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class GetBaseLevelProcedure {
	public static double execute(LevelAccessor world) {
		String dimName = "";
		double toReturn = 0;
		dimName = "" + (world instanceof Level _lvl ? _lvl.dimension() : (world instanceof WorldGenLevel _wgl ? _wgl.getLevel().dimension() : Level.OVERWORLD));
		toReturn = (double) MobsLevelsMainConfigConfiguration.BASE_LEVEL.get();
		for (String stringiterator : CustomDimensionsConfigConfiguration.DIMENSIONS_SETTINGS.get()) {
			if (stringiterator.contains(dimName)) {
				toReturn = new Object() {
					double convert(String s) {
						try {
							return Double.parseDouble(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert(stringiterator.substring(5, stringiterator.indexOf("[minEnd]", 6)));
			}
		}
		return toReturn;
	}
}