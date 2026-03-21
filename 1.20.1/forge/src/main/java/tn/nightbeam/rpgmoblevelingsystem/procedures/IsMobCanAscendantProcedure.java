package tn.nightbeam.rpgmoblevelingsystem.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.entity.Entity;

public class IsMobCanAscendantProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		boolean canBeBoss = false;
		canBeBoss = false;
		if (JaumlConfigLib.stringExistsInArray("RpgMobLevelingSystem", "mobs_list_settings", "can_be_ascendant", (ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString()))) {
			canBeBoss = true;
		}
		return canBeBoss;
	}
}