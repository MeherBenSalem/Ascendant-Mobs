package tn.nightbeam.rpgmoblevelingsystem.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelingLogic;

import javax.annotation.Nullable;
import net.minecraftforge.eventbus.api.Event;

@Mod.EventBusSubscriber
public class MobsLevelSystemProcedure {
    @SubscribeEvent
    public static void onEntitySpawned(EntityJoinLevelEvent event) {
        execute(event, event.getLevel(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        execute(null, world, x, y, z, entity);
    }

    private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null || !(world instanceof net.minecraft.world.level.Level level)) {
            return;
        }
        MobLevelingLogic.onEntityLoaded(level, entity);
    }
}
