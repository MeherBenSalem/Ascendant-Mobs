package tn.nightbeam.rpgmoblevelingsystem.fabric.gameplay;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import tn.nightbeam.rpgmoblevelingsystem.command.RmlCommands;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.AscendantService;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelService;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelingLogic;

public final class FabricMobLeveling {
    private FabricMobLeveling() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> MobLevelingLogic.onEntityLoaded(level, entity));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            MobLevelService.onMobDeath(entity);
            AscendantService.onAscendantDeath(entity);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> RmlCommands.register(dispatcher));
    }
}
