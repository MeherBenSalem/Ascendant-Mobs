package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsListConfigConfiguration;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;

public class CanGetLevelProcedureProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		boolean found = false;
		found = false;
		for (String stringiterator : MobsListConfigConfiguration.BANNED.get()) {
			if ((BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()).equals(stringiterator)) {
				found = true;
			}
		}
		return found;
	}
}
