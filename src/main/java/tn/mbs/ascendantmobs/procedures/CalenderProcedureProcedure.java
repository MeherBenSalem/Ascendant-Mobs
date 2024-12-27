package tn.mbs.ascendantmobs.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

public class CalenderProcedureProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(
					("\u00A71Current Day : \u00A7f" + new java.text.DecimalFormat("##.##").format(world.dayTime() / 24000) + " \u00A7c" + ReturnDebugDimensionIDProcedure.execute(world) + "\u00A79 " + ReturnMaxMinLevelsProcedure.execute(world))),
					true);
	}
}
