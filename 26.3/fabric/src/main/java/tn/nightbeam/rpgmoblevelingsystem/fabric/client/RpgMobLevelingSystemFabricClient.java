package tn.nightbeam.rpgmoblevelingsystem.fabric.client;

import net.fabricmc.api.ClientModInitializer;

public final class RpgMobLevelingSystemFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricMobLevelOverlay.register();
    }
}
