package com.nightbeam.remnants.mobs.network;

import com.nightbeam.remnants.mobs.RemnantMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Client to server: changes made in the settings screen, as JSON {@code {"levels.json": {"spread": 3}}}. The server
 * checks the player's permission and every value again before it writes anything.
 */
public record ConfigUpdatePayload(String changes) implements Payload {
    /** Serverbound custom payloads are limited to 32 KiB. */
    public static final int MAX_LENGTH = 30000;
    public static final ResourceLocation ID = RemnantMobs.id("config_update");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(changes, MAX_LENGTH);
    }

    public static ConfigUpdatePayload read(FriendlyByteBuf buf) {
        return new ConfigUpdatePayload(buf.readUtf(MAX_LENGTH));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
