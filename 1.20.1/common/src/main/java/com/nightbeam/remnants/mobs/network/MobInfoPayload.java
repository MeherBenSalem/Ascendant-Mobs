package com.nightbeam.remnants.mobs.network;

import com.nightbeam.remnants.mobs.RemnantMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Server to client: a mob's level, rank and traits (level -1 clears them), plus stats for the Hunter's Lens. */
public record MobInfoPayload(int entityId, int level, int rank, int traits, int flags, long seed, float damage, float armor)
        implements Payload {
    public static final ResourceLocation ID = RemnantMobs.id("mob_info");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(level + 1);
        buf.writeByte(rank);
        buf.writeVarInt(traits);
        buf.writeVarInt(flags);
        buf.writeLong(seed);
        buf.writeFloat(damage);
        buf.writeFloat(armor);
    }

    public static MobInfoPayload read(FriendlyByteBuf buf) {
        return new MobInfoPayload(buf.readVarInt(), buf.readVarInt() - 1, buf.readByte(), buf.readVarInt(), buf.readVarInt(),
                buf.readLong(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
