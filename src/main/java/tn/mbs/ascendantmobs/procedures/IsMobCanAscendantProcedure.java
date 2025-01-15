package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsListConfigConfiguration;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.entity.Entity;

public class IsMobCanAscendantProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		boolean canBeBoss = false;
		canBeBoss = false;
		for (String stringiterator : MobsListConfigConfiguration.CAN_BE_ASCENDANT.get()) {
			if ((ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString()).equals(stringiterator)) {
				canBeBoss = true;
			}
		}
		return canBeBoss;
	}
}
