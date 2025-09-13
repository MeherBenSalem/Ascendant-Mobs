package tn.mbs.ascendantmobs.procedures;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;

public class GetLowestEntityLevelProcedure {
	public static double execute(LevelAccessor world, double x, double y, double z) {
		Entity nearest = null;
		nearest = (Entity) world.getEntitiesOfClass(Player.class, AABB.ofSize(new Vec3(x, y, z), 200, 200, 200), e -> true).stream().sorted(new Object() {
			Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
				return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
			}
		}.compareDistOf(x, y, z)).findFirst().orElse(null);
		if (!(nearest == null)) {
			return ((LivingEntity) nearest).getAttribute(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("memory_of_the_past", "motp_level"))).getBaseValue();
		}
		return 0;
	}
}