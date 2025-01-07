package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsListConfigConfiguration;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;

public class IsMobCanAscendantProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		boolean canBeBoss = false;
		canBeBoss = false;
		for (String stringiterator : MobsListConfigConfiguration.CAN_BE_ASCENDANT.get()) {
			if ((BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()).equals(stringiterator)) {
				canBeBoss = true;
			}
		}
		return canBeBoss;
	}
}
