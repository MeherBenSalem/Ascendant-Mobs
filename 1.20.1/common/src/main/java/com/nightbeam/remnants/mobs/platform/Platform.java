package com.nightbeam.remnants.mobs.platform;

import com.nightbeam.remnants.mobs.network.Payload;
import java.nio.file.Path;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** The few things common code needs from the loader. Each loader passes an implementation to {@code RemnantMobs.init}. */
public interface Platform {
    Path configDir();

    boolean isModLoaded(String modId);

    void sendToPlayer(ServerPlayer player, Payload payload);

    /** Sends to every player that currently tracks (sees) {@code entity}. */
    void sendToTracking(Entity entity, Payload payload);

    /** True for automation players (deployers, turtles, fake players of other mods). */
    boolean isFakePlayer(ServerPlayer player);

    boolean isClient();
}
