package com.nightbeam.remnants.mobs.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** A server-to-client message. 1.20.1 has no payload API, so each loader sends these on its own channel. */
public interface Payload {
    ResourceLocation id();

    void write(FriendlyByteBuf buf);
}
