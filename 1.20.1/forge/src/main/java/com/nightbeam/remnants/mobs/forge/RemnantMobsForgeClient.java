package com.nightbeam.remnants.mobs.forge;

import com.nightbeam.remnants.mobs.client.ClientState;
import com.nightbeam.remnants.mobs.client.LensHud;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client setup. Forge 47 has no client-only {@code @Mod} entry point, so {@link RemnantMobsForge} calls {@link #init}. */
public final class RemnantMobsForgeClient {
    private RemnantMobsForgeClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener((RegisterGuiOverlaysEvent event) -> event.registerAbove(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(), "lens",
                (gui, graphics, partialTick, width, height) -> LensHud.render(graphics)));
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> ClientState.reset());
    }
}
