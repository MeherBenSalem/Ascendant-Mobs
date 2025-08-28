package tn.mbs.ascendantmobs.procedures;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class ReturnDebugDimensionIDProcedure {
	public static String execute(LevelAccessor world) {
		String dimensionName = "";
		dimensionName = "" + (world instanceof Level _lvl ? _lvl.dimension() : (world instanceof WorldGenLevel _wgl ? _wgl.getLevel().dimension() : Level.OVERWORLD));
		return dimensionName.substring(34, (dimensionName).length() - 1);
	}
}