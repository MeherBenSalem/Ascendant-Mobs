package tn.mbs.ascendantmobs.procedures;

import tn.mbs.ascendantmobs.configuration.MobsListConfigConfiguration;
import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class ExperienceDropBasedOnLevelProcedure {
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
		}
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity.getPersistentData().getBoolean("am_level")) {
			for (String stringiterator : MobsListConfigConfiguration.DEATH_MESSAGES.get()) {
				if ((ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString()).equals(stringiterator)) {
					if (!world.isClientSide() && world.getServer() != null)
						world.getServer().getPlayerList().broadcastSystemMessage(Component.literal((entity.getDisplayName().getString() + " was killed")), false);
				}
			}
			if (world instanceof ServerLevel _level)
				_level.addFreshEntity(new ExperienceOrb(_level, x, y, z, (int) Math.floor(entity.getPersistentData().getDouble("am_lvl") * (double) MobsLevelsMainConfigConfiguration.XP_MODFIER.get())));
		}
	}
}
