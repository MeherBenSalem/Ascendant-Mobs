package com.nightbeam.remnants.mobs.network;

import com.nightbeam.remnants.mobs.RemnantMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Client to server: the settings screen opened in a world and asks for the server's config values. */
public record ConfigRequestPayload() implements Payload {
    public static final ResourceLocation ID = RemnantMobs.id("config_request");

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    public static ConfigRequestPayload read(FriendlyByteBuf buf) {
        return new ConfigRequestPayload();
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
