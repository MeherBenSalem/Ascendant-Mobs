package com.nightbeam.remnants.mobs.network;

import com.nightbeam.remnants.mobs.RemnantMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to client: the server's config values for the settings screen (every file but client.json, as JSON), what
 * the player may do with them, and the outcome of the last save.
 *
 * @param open   open the screen (the {@code /remnants config} command) instead of only updating an open one
 * @param access {@link ConfigSync#READ_ONLY}, {@link ConfigSync#OPERATOR} or {@link ConfigSync#HOST}
 * @param status {@link ConfigSync#NONE}, {@link ConfigSync#SAVED}, {@link ConfigSync#PARTLY}, {@link ConfigSync#DENIED}
 *               or {@link ConfigSync#FAILED}
 * @param count  how many values the last save wrote
 * @param detail what was refused, or why saving failed
 */
public record ConfigValuesPayload(boolean open, int access, int status, int count, String detail, String values)
        implements CustomPacketPayload {
    private static final int MAX_VALUES = 1 << 20;
    public static final Type<ConfigValuesPayload> TYPE = new Type<>(RemnantMobs.id("config_values"));
    public static final StreamCodec<FriendlyByteBuf, ConfigValuesPayload> CODEC =
            CustomPacketPayload.codec(ConfigValuesPayload::write, ConfigValuesPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeBoolean(open);
        buf.writeVarInt(access);
        buf.writeVarInt(status);
        buf.writeVarInt(count);
        buf.writeUtf(detail, 4096);
        buf.writeUtf(values, MAX_VALUES);
    }

    private static ConfigValuesPayload read(FriendlyByteBuf buf) {
        return new ConfigValuesPayload(buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(4096),
                buf.readUtf(MAX_VALUES));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
