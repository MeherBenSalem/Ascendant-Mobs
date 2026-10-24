package com.nightbeam.remnants.mobs.network;

import com.nightbeam.remnants.mobs.RemnantMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: a mob's level, rank and traits (level -1 clears them), plus stats for the Hunter's Lens. */
public record MobInfoPayload(int entityId, int level, int rank, int traits, int flags, long seed, float damage, float armor)
        implements CustomPacketPayload {
    public static final Type<MobInfoPayload> TYPE = new Type<>(RemnantMobs.id("mob_info"));
    public static final StreamCodec<FriendlyByteBuf, MobInfoPayload> CODEC = CustomPacketPayload.codec(MobInfoPayload::write, MobInfoPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(level + 1);
        buf.writeByte(rank);
        buf.writeVarInt(traits);
        buf.writeVarInt(flags);
        buf.writeLong(seed);
        buf.writeFloat(damage);
        buf.writeFloat(armor);
    }

    private static MobInfoPayload read(FriendlyByteBuf buf) {
        return new MobInfoPayload(buf.readVarInt(), buf.readVarInt() - 1, buf.readByte(), buf.readVarInt(), buf.readVarInt(),
                buf.readLong(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
