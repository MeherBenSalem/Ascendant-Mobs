package tn.mbs.ascendantmobs.procedures;

import net.minecraft.world.level.LevelAccessor;

public class ReturnMaxMinLevelsProcedure {
	public static String execute(LevelAccessor world) {
		return "[" + new java.text.DecimalFormat("##").format(GetBaseLevelFromConfigProcedure.execute(world)) + " - " + new java.text.DecimalFormat("##").format(GetMaxLevelFromConfigProcedure.execute(world)) + "]";
	}
}
