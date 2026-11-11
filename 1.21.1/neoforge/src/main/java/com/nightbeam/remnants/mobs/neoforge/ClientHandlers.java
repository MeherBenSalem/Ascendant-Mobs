package com.nightbeam.remnants.mobs.neoforge;

import com.nightbeam.remnants.mobs.client.ClientState;
import com.nightbeam.remnants.mobs.client.ConfigClient;
import com.nightbeam.remnants.mobs.network.ConfigValuesPayload;
import com.nightbeam.remnants.mobs.network.DifficultyPayload;
import com.nightbeam.remnants.mobs.network.MobInfoPayload;
import com.nightbeam.remnants.mobs.network.PlayerStatePayload;

/** Keeps client classes out of the common entry point: only loaded when a payload reaches a client. */
final class ClientHandlers {
    private ClientHandlers() {
    }

    static void mobInfo(MobInfoPayload payload) {
        ClientState.handleMobInfo(payload);
    }

    static void playerState(PlayerStatePayload payload) {
        ClientState.handlePlayerState(payload);
    }

    static void configValues(ConfigValuesPayload payload) {
        ConfigClient.onValues(payload);
    }

    static void difficulty(DifficultyPayload payload) {
        ClientState.handleDifficulty(payload);
    }
}
