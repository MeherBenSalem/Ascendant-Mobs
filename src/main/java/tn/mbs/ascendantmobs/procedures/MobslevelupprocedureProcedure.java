package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
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
		double level = 0;
		double health = 0;
		double attack_damage = 0;
		double speed = 0;
		double protection = 0;
		double baseLevel = 0;
		double maxLevel = 0;
		double lockedLevel = 0;
		String dimensionName = "";
		if (!(entity instanceof ServerPlayer || entity instanceof Player)) {
			if (entity.getPersistentData().getBoolean("am_level") || CanGetLevelProcedureProcedure.execute(entity)) {
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
			health = level * (double) MobsLevelsMainConfigConfiguration.HEALTH_MODIFIER.get() + (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1);
			attack_damage = health / (double) MobsLevelsMainConfigConfiguration.DAMAGE_MODIFIER.get();
			protection = Mth.nextDouble(RandomSource.create(), 0, (level / maxLevel) * (double) MobsLevelsMainConfigConfiguration.PROTECTION_MODIFIER.get());
			speed = Mth.nextDouble(RandomSource.create(), (double) MobsLevelsMainConfigConfiguration.MIN_SPEED.get(), (double) MobsLevelsMainConfigConfiguration.SPEED_MODIFIER.get());
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
			{
				Entity _ent = entity;
				if (!_ent.level().isClientSide() && _ent.getServer() != null) {
					_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
							_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), ("attribute @s minecraft:generic.armor base set {protection}".replace("{protection}", "" + protection)));
				}
			}
			{
				Entity _ent = entity;
				if (!_ent.level().isClientSide() && _ent.getServer() != null) {
					_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
							_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), ("attribute @s minecraft:generic.movement_speed base set {speed}".replace("{speed}", "" + speed)));
				}
			}
			if (entity instanceof LivingEntity _entity)
				_entity.setHealth((float) health);
			if (MobsLevelsMainConfigConfiguration.RANDOM_EFFECTS.get() && IsMobCanAscendantProcedure.execute(entity) && level >= (double) MobsLevelsMainConfigConfiguration.RANDOM_EFFECTS_LEVEL.get()) {
				if (Mth.nextDouble(RandomSource.create(), 0, 100) <= (double) MobsLevelsMainConfigConfiguration.RANDOM_EFFECTS_CHANCE.get()) {
					RandomEffectsEntityProcedureProcedure.execute(entity);
				}
			}
			if (MobsLevelsMainConfigConfiguration.DISPLAY_LVL_NAME.get() && !(entity.getDisplayName().getString()).contains("[Lv.")) {
				if (entity.getPersistentData().getBoolean("am_leader")) {
					entity.setCustomName(Component.literal(("\u00A74[Lv." + new java.text.DecimalFormat("##").format(level) + "] \u00A7eLeader " + entity.getDisplayName().getString())));
				} else if (entity instanceof LivingEntity _livEnt26 && _livEnt26.getMobType() == MobType.ILLAGER || entity instanceof LivingEntity _livEnt27 && _livEnt27.getMobType() == MobType.UNDEAD) {
					entity.setCustomName(Component.literal(("\u00A74[Lv." + new java.text.DecimalFormat("##").format(level) + "] \u00A7f" + entity.getDisplayName().getString())));
				} else if (entity instanceof LivingEntity _livEnt30 && _livEnt30.getMobType() == MobType.WATER) {
					entity.setCustomName(Component.literal(("\u00A71[Lv." + new java.text.DecimalFormat("##").format(level) + "] \u00A7f" + entity.getDisplayName().getString())));
				} else {
					entity.setCustomName(Component.literal(("\u00A72[Lv." + new java.text.DecimalFormat("##").format(level) + "] \u00A7f" + entity.getDisplayName().getString())));
				}
			}
			entity.getPersistentData().putBoolean("am_level", true);
			entity.getPersistentData().putDouble("am_lvl", level);
		}
	}
}
