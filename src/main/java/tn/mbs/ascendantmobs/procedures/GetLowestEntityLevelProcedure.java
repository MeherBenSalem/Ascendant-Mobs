package tn.mbs.ascendantmobs.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Comparator;

public class GetLowestEntityLevelProcedure {
	public static double execute(LevelAccessor world, double x, double y, double z) {
		double lowestLevel = 0;
		lowestLevel = 9999;
		{
			final Vec3 _center = new Vec3(x, y, z);
			List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(20 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
			for (Entity entityiterator : _entfound) {
				if (entityiterator instanceof Player || entityiterator instanceof ServerPlayer) {
					if (entityiterator.getPersistentData().getDouble("motp_level") < lowestLevel) {
						lowestLevel = entityiterator.getPersistentData().getDouble("motp_level");
					}
				}
			}
		}
		if (lowestLevel == 9999) {
			lowestLevel = 0;
		}
		return lowestLevel;
	}
}
