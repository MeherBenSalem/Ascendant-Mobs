package com.nightbeam.remnants.mobs.network;

import com.nightbeam.remnants.mobs.RemnantMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client to server: changes made in the settings screen, as JSON {@code {"levels.json": {"spread": 3}}}. The server
 * checks the player's permission and every value again before it writes anything.
 */
public record ConfigUpdatePayload(String changes) implements CustomPacketPayload {
    /** Serverbound custom payloads are limited to 32 KiB. */
    public static final int MAX_LENGTH = 30000;
    public static final Type<ConfigUpdatePayload> TYPE = new Type<>(RemnantMobs.id("config_update"));
    public static final StreamCodec<FriendlyByteBuf, ConfigUpdatePayload> CODEC =
            CustomPacketPayload.codec(ConfigUpdatePayload::write, ConfigUpdatePayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeUtf(changes, MAX_LENGTH);
    }

    private static ConfigUpdatePayload read(FriendlyByteBuf buf) {
        return new ConfigUpdatePayload(buf.readUtf(MAX_LENGTH));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
