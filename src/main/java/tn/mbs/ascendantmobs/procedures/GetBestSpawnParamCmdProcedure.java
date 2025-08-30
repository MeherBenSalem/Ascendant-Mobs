package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class GetBestSpawnParamCmdProcedure {
	public static void execute(LevelAccessor world, CommandContext<CommandSourceStack> arguments, Entity entity) {
		if (entity == null)
			return;
		double spawnlocation = 0;
		Entity lowestLevelEntity = null;
		String scaletype = "";
		scaletype = JaumlConfigLib.getStringValue("ascendant_mobs", "scale_settings", "scale_type");
		if ((scaletype).equals("MOTP")) {
			if (entity instanceof Player _player && !_player.level().isClientSide())
				_player.displayClientMessage(Component.literal("\u00A74[Debug] \u00A7fscale_type is set to \u00A74MOTP"), false);
		} else {
			if ((scaletype).equals("vertical")) {
				spawnlocation = Math.pow(entity.getY() - world.getLevelData().getYSpawn(), 2);
			} else if ((scaletype).equals("horizontal")) {
				spawnlocation = Math.pow(entity.getX() - world.getLevelData().getXSpawn(), 2) + Math.pow(entity.getZ() - world.getLevelData().getZSpawn(), 2);
			} else if ((scaletype).equals("time")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("\u00A74[Debug] \u00A7fscale_type is set to \u00A74time"), false);
			} else if ((scaletype).equals("random")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("\u00A74[Debug] \u00A7fscale_type is set to \u00A74random"), false);
			} else {
				spawnlocation = Math.pow(entity.getX() - world.getLevelData().getXSpawn(), 2) + Math.pow(entity.getZ() - world.getLevelData().getZSpawn(), 2) + Math.pow(entity.getY() - world.getLevelData().getYSpawn(), 2);
			}
		}
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("\u00A74[Debug] \u00A7fRecommended distance to get this level at this distance is \u00A76"
					+ Math.round((spawnlocation * JaumlConfigLib.getNumberValue("ascendant_mobs", "scale_settings", "scale_factor")) / DoubleArgumentType.getDouble(arguments, "level")))), false);
	}
}