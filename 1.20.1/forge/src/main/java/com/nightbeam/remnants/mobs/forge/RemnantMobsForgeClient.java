package com.nightbeam.remnants.mobs.forge;

import com.nightbeam.remnants.mobs.client.ClientState;
import com.nightbeam.remnants.mobs.client.ConfigClient;
import com.nightbeam.remnants.mobs.client.LensHud;
import com.nightbeam.remnants.mobs.client.Plates;
import com.nightbeam.remnants.mobs.network.Payload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.network.simple.SimpleChannel;

/** Client setup. Forge 47 has no client-only {@code @Mod} entry point, so {@link RemnantMobsForge} calls {@link #init}. */
public final class RemnantMobsForgeClient {
    private RemnantMobsForgeClient() {
    }

    public static void init(IEventBus modBus) {
        // The mod list's Config button opens the settings screen.
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> ConfigClient.screen(parent)));
        ConfigClient.setSender(new ConfigClient.Sender() {
            @Override
            public boolean canSend(ResourceLocation id) {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                SimpleChannel channel = RemnantMobsForge.existing(id);
                return connection != null && channel != null && channel.isRemotePresent(connection.getConnection());
            }

            @Override
            public void send(Payload payload) {
                SimpleChannel channel = RemnantMobsForge.existing(payload.id());
                if (channel != null) {
                    channel.sendToServer(payload);
                }
            }
        });
        modBus.addListener((RegisterGuiOverlaysEvent event) -> event.registerAbove(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(), "lens",
                (gui, graphics, partialTick, width, height) -> LensHud.render(graphics)));
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> ClientState.reset());
        // Last, and also when cancelled: a cancelled Pre means another mod (Epic Fight) draws this mob itself.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, RenderLivingEvent.Pre.class,
                RemnantMobsForgeClient::onRenderLivingPre);
    }

    private static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (event.isCanceled() && event.getEntity() instanceof Mob mob) {
            Plates.renderFallback(mob, event.getPoseStack(), event.getMultiBufferSource(), event.getPartialTick());
        } else {
            Plates.clearFallback();
        }
    }
}
