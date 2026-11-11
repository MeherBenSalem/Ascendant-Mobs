package com.nightbeam.remnants.mobs.network;

import com.nightbeam.remnants.mobs.RemnantMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: the settings screen opened in a world and asks for the server's config values. */
public record ConfigRequestPayload() implements CustomPacketPayload {
    public static final Type<ConfigRequestPayload> TYPE = new Type<>(RemnantMobs.id("config_request"));
    public static final StreamCodec<FriendlyByteBuf, ConfigRequestPayload> CODEC =
            CustomPacketPayload.codec((payload, buf) -> { }, buf -> new ConfigRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
