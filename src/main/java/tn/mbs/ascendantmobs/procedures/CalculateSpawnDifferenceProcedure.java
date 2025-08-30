package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.ModList;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;

public class CalculateSpawnDifferenceProcedure {
	public static double execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return 0;
		double spawnlocation = 0;
		Entity lowestLevelEntity = null;
		String scaletype = "";
		scaletype = JaumlConfigLib.getStringValue("ascendant_mobs", "scale_settings", "scale_type");
		if ((scaletype).equals("MOTP")) {
			if (ModList.get().isLoaded("memory_of_the_past")) {
				return GetLowestEntityLevelProcedure.execute(world, x, y, z);
			}
		} else {
			if ((scaletype).equals("vertical")) {
				spawnlocation = Math.pow(entity.getY() - world.getLevelData().getYSpawn(), 2);
			} else if ((scaletype).equals("horizontal")) {
				spawnlocation = Math.pow(entity.getX() - world.getLevelData().getXSpawn(), 2) + Math.pow(entity.getZ() - world.getLevelData().getZSpawn(), 2);
			} else if ((scaletype).equals("time")) {
				return Math.floor((world.dayTime() / 24000) / JaumlConfigLib.getNumberValue("ascendant_mobs", "scale_settings", "day_factor"));
			} else if ((scaletype).equals("random")) {
				return Mth.nextInt(RandomSource.create(), (int) GetBaseLevelProcedure.execute(world), (int) GetMaxLevelProcedure.execute(world));
			} else {
				spawnlocation = Math.pow(entity.getX() - world.getLevelData().getXSpawn(), 2) + Math.pow(entity.getZ() - world.getLevelData().getZSpawn(), 2) + Math.pow(entity.getY() - world.getLevelData().getYSpawn(), 2);
			}
		}
		return Math.floor(spawnlocation / JaumlConfigLib.getNumberValue("ascendant_mobs", "scale_settings", "scale_distance"));
	}
}