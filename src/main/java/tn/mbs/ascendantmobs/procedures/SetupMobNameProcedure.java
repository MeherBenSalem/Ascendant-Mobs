package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class SetupMobNameProcedure {
	@SubscribeEvent
	public static void onEntitySpawned(EntityJoinLevelEvent event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		String dimensionName = "";
		String mobType = "";
		String cmd = "";
		double level = 0;
		double health = 0;
		double attack_damage = 0;
		double baseLevel = 0;
		double maxLevel = 0;
		double lockedLevel = 0;
		double baseValue = 0;
		double modifier = 0;
		double maxValue = 0;
		double value = 0;
		if (MobsLevelsMainConfigConfiguration.DISPLAY_LVL_NAME.get() && !(entity.getDisplayName().getString()).contains("[Lv.") && entity.getPersistentData().getDouble("AM_mobs_level") > 0) {
			entity.setCustomName(Component.literal(("\u00A72[Lv." + Math.round(entity.getPersistentData().getDouble("AM_mobs_level")) + "] \u00A7f" + entity.getDisplayName().getString())));
		}
	}
}
