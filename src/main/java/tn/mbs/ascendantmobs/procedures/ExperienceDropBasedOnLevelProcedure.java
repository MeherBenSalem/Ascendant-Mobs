package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.ascendantmobs.init.AscendantMobsModAttributes;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class ExperienceDropBasedOnLevelProcedure {
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getEntity(), event.getSource().getEntity());
		}
	}

	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
		execute(null, world, entity, sourceentity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if ((entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get())
				? _livingEntity0.getAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get()).getValue()
				: 0) > 0) {
			if (!JaumlConfigLib.getBooleanValue("ascendant_mobs", "mobs_list_settings", "death_messages")) {
				if (world instanceof Level _level) {
					PlayerTeam _pt = _level.getScoreboard().getPlayerTeam("am_no_msg");
					if (_pt != null)
						_pt.setDeathMessageVisibility(Team.Visibility.NEVER);
				}
				{
					Entity _entityTeam = entity;
					PlayerTeam _pt = _entityTeam.level().getScoreboard().getPlayerTeam("am_no_msg");
					if (_pt != null) {
						if (_entityTeam instanceof Player _player)
							_entityTeam.level().getScoreboard().addPlayerToTeam(_player.getGameProfile().getName(), _pt);
						else
							_entityTeam.level().getScoreboard().addPlayerToTeam(_entityTeam.getStringUUID(), _pt);
					}
				}
			}
			if (sourceentity instanceof Player _player)
				_player.giveExperiencePoints((int) Math.floor((entity instanceof LivingEntity _livingEntity4 && _livingEntity4.getAttributes().hasAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get())
						? _livingEntity4.getAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get()).getValue()
						: 0) * JaumlConfigLib.getNumberValue("ascendant_mobs", "scale_settings", "xp_modfier")));
		}
	}
}