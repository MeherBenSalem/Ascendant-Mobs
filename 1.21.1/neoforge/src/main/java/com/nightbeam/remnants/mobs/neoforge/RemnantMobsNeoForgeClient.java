package com.nightbeam.remnants.mobs.neoforge;

import com.nightbeam.remnants.mobs.RemnantMobs;
import com.nightbeam.remnants.mobs.client.ClientState;
import com.nightbeam.remnants.mobs.client.LensHud;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = RemnantMobs.MOD_ID, dist = Dist.CLIENT)
public final class RemnantMobsNeoForgeClient {
    public RemnantMobsNeoForgeClient(IEventBus modBus) {
        modBus.addListener((RegisterGuiLayersEvent event) ->
                event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, RemnantMobs.id("lens"), (graphics, deltaTracker) -> LensHud.render(graphics)));
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> ClientState.reset());
    }
}
