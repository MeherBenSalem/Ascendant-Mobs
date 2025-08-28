package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

public class GetDistanceFromSpawnCmdProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("\u00A74[Debug] \u00A7fAvreage level : " + CalculateSpawnDifferenceProcedure.execute(world, x, y, z, entity) + "(SpawnDiffrence) * "
					+ (double) MobsLevelsMainConfigConfiguration.SCALE_FACTOR.get() + "(scale_factor) = " + CalculateSpawnDifferenceProcedure.execute(world, x, y, z, entity) * (double) MobsLevelsMainConfigConfiguration.SCALE_FACTOR.get())), false);
	}
}