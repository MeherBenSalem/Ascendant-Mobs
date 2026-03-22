package tn.nightbeam.rpgmoblevelingsystem.fabric.gameplay;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelingLogic;

public final class FabricMobLeveling {
    private FabricMobLeveling() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> MobLevelingLogic.onEntityLoaded(level, entity));
    }
}
