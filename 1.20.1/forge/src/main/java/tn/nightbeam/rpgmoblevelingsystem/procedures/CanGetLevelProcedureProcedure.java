package tn.nightbeam.rpgmoblevelingsystem.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.entity.Entity;

public class CanGetLevelProcedureProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		boolean found = false;
		found = false;
		if (JaumlConfigLib.stringExistsInArray("RpgMobLevelingSystem", "mobs_list_settings", "banned", (ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString()))) {
			found = true;
		}
		return found;
	}
}