package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsListConfigConfiguration;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.entity.Entity;

public class CanGetLevelProcedureProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		boolean found = false;
		found = false;
		for (String stringiterator : MobsListConfigConfiguration.BANNED.get()) {
			if ((ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString()).equals(stringiterator)) {
				found = true;
			}
		}
		return found;
	}
}