package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;

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
		if ((MobsLevelsMainConfigConfiguration.SCALE_TYPE.get()).equals("MOTP")) {
			if (ModList.get().isLoaded("memory_of_the_past")) {
				return GetLowestEntityLevelProcedure.execute(world, x, y, z);
			}
		} else {
			if ((MobsLevelsMainConfigConfiguration.SCALE_TYPE.get()).equals("vertical")) {
				spawnlocation = Math.pow(entity.getY() - world.getLevelData().getYSpawn(), 2);
			} else if ((MobsLevelsMainConfigConfiguration.SCALE_TYPE.get()).equals("horizontal")) {
				spawnlocation = Math.pow(entity.getX() - world.getLevelData().getXSpawn(), 2) + Math.pow(entity.getZ() - world.getLevelData().getZSpawn(), 2);
			} else if ((MobsLevelsMainConfigConfiguration.SCALE_TYPE.get()).equals("time")) {
				return Math.floor((world.dayTime() / 24000) / (double) MobsLevelsMainConfigConfiguration.DAY_FACTOR.get());
			} else if ((MobsLevelsMainConfigConfiguration.SCALE_TYPE.get()).equals("random")) {
				return Mth.nextInt(RandomSource.create(), (int) GetBaseLevelFromConfigProcedure.execute(world), (int) GetMaxLevelFromConfigProcedure.execute(world));
			} else {
				spawnlocation = Math.pow(entity.getX() - world.getLevelData().getXSpawn(), 2) + Math.pow(entity.getZ() - world.getLevelData().getZSpawn(), 2) + Math.pow(entity.getY() - world.getLevelData().getYSpawn(), 2);
			}
		}
		return Math.floor(spawnlocation / (double) MobsLevelsMainConfigConfiguration.SCALE_DISTANCE.get());
	}
}
