package com.nightbeam.remnants.mobs.forge;

import com.nightbeam.remnants.mobs.RemnantMobs;
import com.nightbeam.remnants.mobs.client.ClientState;
import com.nightbeam.remnants.mobs.client.ConfigClient;
import com.nightbeam.remnants.mobs.client.LensHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;

/** Client setup, called from {@link RemnantMobsForge} on the client only. */
final class RemnantMobsForgeClient {
    private RemnantMobsForgeClient() {
    }

    static void init(FMLJavaModLoadingContext context) {
        // The mod list's Config button opens the settings screen.
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> ConfigClient.screen(parent)));
        ConfigClient.setSender(new ConfigClient.Sender() {
            @Override
            public boolean canSend(CustomPacketPayload.Type<?> type) {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                return connection != null && RemnantMobsForge.configChannel().isRemotePresent(connection.getConnection());
            }

            @Override
            public void send(CustomPacketPayload payload) {
                RemnantMobsForge.configChannel().send(payload, PacketDistributor.SERVER.noArg());
            }
        });
        // Forge draws the HUD through layers, so the lens readouts and the difficulty meter go above the boss bars.
        AddGuiOverlayLayersEvent.BUS.addListener(event -> event.getLayeredDraw().addAbove(ForgeLayeredDraw.PRE_SLEEP_STACK,
                RemnantMobs.id("lens"), ForgeLayeredDraw.BOSS_OVERLAY, (graphics, deltaTracker) -> LensHud.render(graphics)));
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> ClientState.reset());
    }
}
