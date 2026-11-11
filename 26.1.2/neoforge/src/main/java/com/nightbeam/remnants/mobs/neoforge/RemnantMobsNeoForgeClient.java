package com.nightbeam.remnants.mobs.neoforge;

import com.nightbeam.remnants.mobs.RemnantMobs;
import com.nightbeam.remnants.mobs.client.ClientState;
import com.nightbeam.remnants.mobs.client.ConfigClient;
import com.nightbeam.remnants.mobs.network.ConfigValuesPayload;
import com.nightbeam.remnants.mobs.client.LensHud;
import com.nightbeam.remnants.mobs.network.DifficultyPayload;
import com.nightbeam.remnants.mobs.network.MobInfoPayload;
import com.nightbeam.remnants.mobs.network.PlayerStatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = RemnantMobs.MOD_ID, dist = Dist.CLIENT)
public final class RemnantMobsNeoForgeClient {
    public RemnantMobsNeoForgeClient(IEventBus modBus, ModContainer container) {
        // The mod list's Config button opens the settings screen.
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> ConfigClient.screen(parent));
        ConfigClient.setSender(new ConfigClient.Sender() {
            @Override
            public boolean canSend(CustomPacketPayload.Type<?> type) {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                return connection != null && connection.hasChannel(type);
            }

            @Override
            public void send(CustomPacketPayload payload) {
                ClientPacketDistributor.sendToServer(payload);
            }
        });
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> {
            event.register(ConfigValuesPayload.TYPE, (payload, context) -> ConfigClient.onValues(payload));
            event.register(MobInfoPayload.TYPE, (payload, context) -> ClientState.handleMobInfo(payload));
            event.register(PlayerStatePayload.TYPE, (payload, context) -> ClientState.handlePlayerState(payload));
            event.register(DifficultyPayload.TYPE, (payload, context) -> ClientState.handleDifficulty(payload));
        });
        modBus.addListener((RegisterGuiLayersEvent event) ->
                event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, RemnantMobs.id("lens"), (graphics, deltaTracker) -> LensHud.render(graphics)));
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> ClientState.reset());
    }
}
