package tn.mbs.ascendantmobs.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

public class RandomEffectsEntityProcedureProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		String bossName = "";
		String bossNameDisplay = "";
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 99999999, Mth.nextInt(RandomSource.create(), 1, 2), false, true));
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999999, Mth.nextInt(RandomSource.create(), 1, 2), false, true));
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999999, Mth.nextInt(RandomSource.create(), 1, 3), false, true));
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 99999999, Mth.nextInt(RandomSource.create(), 1, 3), false, true));
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 99999999, Mth.nextInt(RandomSource.create(), 1, 3), false, true));
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 99999999, Mth.nextInt(RandomSource.create(), 1, 3), false, true));
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 99999999, Mth.nextInt(RandomSource.create(), 1, 3), false, true));
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 99999999, Mth.nextInt(RandomSource.create(), 1, 3), false, true));
		bossName = ("ascendant:mob_" + entity.getDisplayName().getString() + Mth.nextInt(RandomSource.create(), 1, 3)).toLowerCase();
		bossNameDisplay = entity.getDisplayName().getString();
		entity.getPersistentData().putBoolean("am_boss", true);
		entity.getPersistentData().putString("am_bossbar_id", bossName);
		{
			Entity _ent = entity;
			if (!_ent.level().isClientSide() && _ent.getServer() != null) {
				_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
						_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), (("/bossbar add <display_name> " + "\"" + bossNameDisplay + "\"").replace("<display_name>", bossName)));
			}
		}
		{
			Entity _ent = entity;
			if (!_ent.level().isClientSide() && _ent.getServer() != null) {
				_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
						_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), ("/bossbar set <display_name> color red".replace("<display_name>", bossName)));
			}
		}
		{
			Entity _ent = entity;
			if (!_ent.level().isClientSide() && _ent.getServer() != null) {
				_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
						_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), ("/bossbar set <display_name> style progress".replace("<display_name>", bossName)));
			}
		}
		{
			Entity _ent = entity;
			if (!_ent.level().isClientSide() && _ent.getServer() != null) {
				_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
						_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), ("/bossbar set <display_name> players @a[distance=..32]".replace("<display_name>", bossName)));
			}
		}
		{
			Entity _ent = entity;
			if (!_ent.level().isClientSide() && _ent.getServer() != null) {
				_ent.getServer().getCommands().performPrefixedCommand(
						new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4, _ent.getName().getString(), _ent.getDisplayName(),
								_ent.level().getServer(), _ent),
						(("/bossbar set <display_name> max " + (new java.text.DecimalFormat("##").format(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1))).replace("<display_name>", bossName)));
			}
		}
		{
			Entity _ent = entity;
			if (!_ent.level().isClientSide() && _ent.getServer() != null) {
				_ent.getServer().getCommands().performPrefixedCommand(
						new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4, _ent.getName().getString(), _ent.getDisplayName(),
								_ent.level().getServer(), _ent),
						(("/bossbar set <display_name> value " + (new java.text.DecimalFormat("##").format(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1))).replace("<display_name>", bossName)));
			}
		}
	}
}
