package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsListConfigConfiguration;

import org.checkerframework.checker.units.qual.s;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.entity.Entity;

public class LockedMobsProcedureProcedure {
	public static double execute(Entity entity) {
		if (entity == null)
			return 0;
		double level = 0;
		level = -1;
		for (String stringiterator : MobsListConfigConfiguration.LOCKED_MOBS.get()) {
			if ((ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString()).equals(stringiterator.substring(4))) {
				level = new Object() {
					double convert(String s) {
						try {
							return Double.parseDouble(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert(stringiterator.substring(0, 3));
			}
		}
		return level;
	}
}
