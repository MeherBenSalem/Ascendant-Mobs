package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;

import org.checkerframework.checker.units.qual.s;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class MobslevelupprocedureProcedure {
	@SubscribeEvent
	public static void onEntitySpawned(EntityJoinLevelEvent event) {
		execute(event, event.getLevel(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
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
		if (!(entity instanceof ServerPlayer || entity instanceof Player)) {
			if (entity.getPersistentData().getBoolean("AM_mobs_level") || CanGetLevelProcedureProcedure.execute(entity)) {
				return;
			}
			lockedLevel = LockedMobsProcedureProcedure.execute(entity);
			if (lockedLevel != -1) {
				level = lockedLevel;
			} else {
				baseLevel = GetBaseLevelFromConfigProcedure.execute(world);
				maxLevel = GetMaxLevelFromConfigProcedure.execute(world);
				level = Math.floor(baseLevel + CalculateSpawnDifferenceProcedure.execute(world, x, y, z, entity) * (double) MobsLevelsMainConfigConfiguration.SCALE_FACTOR.get());
				if (level >= maxLevel) {
					level = maxLevel;
				}
			}
			for (String stringiterator : MobsLevelsMainConfigConfiguration.SCALING_FACTORS.get()) {
				baseValue = new Object() {
					double convert(String s) {
						try {
							return Double.parseDouble(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert(stringiterator.substring((int) (stringiterator.indexOf("[base]") + 6), (int) stringiterator.indexOf("[baseEnd]")));
				modifier = new Object() {
					double convert(String s) {
						try {
							return Double.parseDouble(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert(stringiterator.substring((int) (stringiterator.indexOf("[modifier]") + 10), (int) stringiterator.indexOf("[modifierEnd]")));
				maxValue = new Object() {
					double convert(String s) {
						try {
							return Double.parseDouble(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert(stringiterator.substring((int) (stringiterator.indexOf("[max]") + 5), (int) stringiterator.indexOf("[maxEnd]")));
				value = (level + 1) * baseValue * modifier;
				if (value >= maxValue) {
					value = maxValue;
				}
				cmd = stringiterator.substring((int) (stringiterator.indexOf("[cmd]") + 5));
				{
					Entity _ent = entity;
					if (!_ent.level().isClientSide() && _ent.getServer() != null) {
						_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
								_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), (cmd.replace("{value}", new java.text.DecimalFormat("##.##").format(value))));
					}
				}
			}
			health = level * (double) MobsLevelsMainConfigConfiguration.HEALTH_MODIFIER.get() + (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1);
			attack_damage = health / (double) MobsLevelsMainConfigConfiguration.DAMAGE_MODIFIER.get();
			{
				Entity _ent = entity;
				if (!_ent.level().isClientSide() && _ent.getServer() != null) {
					_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
							_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), ("attribute @s minecraft:generic.max_health base set {health}".replace("{health}", "" + health)));
				}
			}
			{
				Entity _ent = entity;
				if (!_ent.level().isClientSide() && _ent.getServer() != null) {
					_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
							_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), ("attribute @s minecraft:generic.attack_damage base set {damage}".replace("{damage}", "" + attack_damage)));
				}
			}
			if (entity instanceof LivingEntity _entity)
				_entity.setHealth((float) health);
			if (MobsLevelsMainConfigConfiguration.RANDOM_EFFECTS.get() && IsMobCanAscendantProcedure.execute(entity) && level >= (double) MobsLevelsMainConfigConfiguration.RANDOM_EFFECTS_LEVEL.get()) {
				if (Mth.nextDouble(RandomSource.create(), 0, 100) <= (double) MobsLevelsMainConfigConfiguration.RANDOM_EFFECTS_CHANCE.get()) {
					RandomEffectsEntityProcedureProcedure.execute(entity);
				}
			}
			entity.getPersistentData().putBoolean("AM_mobs_level", true);
			entity.getPersistentData().putDouble("AM_mobs_level", level);
		}
	}
}
