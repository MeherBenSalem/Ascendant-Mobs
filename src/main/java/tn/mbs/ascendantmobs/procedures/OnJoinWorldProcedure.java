package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@EventBusSubscriber
public class OnJoinWorldProcedure {
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		double YourLevel = 0;
		YourLevel = 0;
		if ((MobsLevelsMainConfigConfiguration.SCALE_TYPE.get()).equals("MOTP")) {
			if (!ModList.get().isLoaded("memory_of_the_past")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal(
							"\u00A7cConfig is set to use the \u00A7eMemories of the Past\u00A7c mod, but it doesn't exist. Please check the config files or make sure you have the correct mods installed. \u00A77(\u00A76scale_type\u00A77 has been set to default value)"),
							false);
			}
		}
	}
}
