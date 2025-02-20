package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.init.AscendantMobsModAttributes;
import tn.mbs.ascendantmobs.configuration.MobsListConfigConfiguration;
import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;
import tn.mbs.ascendantmobs.AscendantMobsMod;

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
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class ExperienceDropBasedOnLevelProcedure {
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getEntity());
		}
	}

	public static void execute(LevelAccessor world, Entity entity) {
		execute(null, world, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if ((entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get())
				? _livingEntity0.getAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get()).getValue()
				: 0) > 0) {
			if (!MobsListConfigConfiguration.DEATH_MESSAGES.get()) {
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
			if (world instanceof ServerLevel _level)
				_level.addFreshEntity(new ExperienceOrb(_level, (entity.getX()), (entity.getY()), (entity.getZ()),
						(int) Math.floor((entity instanceof LivingEntity _livingEntity7 && _livingEntity7.getAttributes().hasAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get())
								? _livingEntity7.getAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get()).getValue()
								: 0) * (double) MobsLevelsMainConfigConfiguration.XP_MODFIER.get())));
			AscendantMobsMod.LOGGER.info(Math.floor((entity instanceof LivingEntity _livingEntity10 && _livingEntity10.getAttributes().hasAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get())
					? _livingEntity10.getAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get()).getValue()
					: 0) * (double) MobsLevelsMainConfigConfiguration.XP_MODFIER.get()));
		}
	}
}
