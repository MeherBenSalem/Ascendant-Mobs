package com.nightbeam.remnants.mobs.fabric;

import com.nightbeam.remnants.mobs.client.ClientState;
import com.nightbeam.remnants.mobs.client.ConfigClient;
import com.nightbeam.remnants.mobs.client.LensHud;
import com.nightbeam.remnants.mobs.network.ConfigValuesPayload;
import com.nightbeam.remnants.mobs.network.DifficultyPayload;
import com.nightbeam.remnants.mobs.network.MobInfoPayload;
import com.nightbeam.remnants.mobs.network.PlayerStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class RemnantMobsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(MobInfoPayload.TYPE, (payload, context) -> ClientState.handleMobInfo(payload));
        ClientPlayNetworking.registerGlobalReceiver(PlayerStatePayload.TYPE, (payload, context) -> ClientState.handlePlayerState(payload));
        ClientPlayNetworking.registerGlobalReceiver(DifficultyPayload.TYPE, (payload, context) -> ClientState.handleDifficulty(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientState.reset());
        ClientPlayNetworking.registerGlobalReceiver(ConfigValuesPayload.TYPE, (payload, context) -> ConfigClient.onValues(payload));
        ConfigClient.setSender(new ConfigClient.Sender() {
            @Override
            public boolean canSend(CustomPacketPayload.Type<?> type) {
                return ClientPlayNetworking.canSend(type);
            }

            @Override
            public void send(CustomPacketPayload payload) {
                ClientPlayNetworking.send(payload);
            }
        });
        HudRenderCallback.EVENT.register((graphics, deltaTracker) -> LensHud.render(graphics));
    }
}
