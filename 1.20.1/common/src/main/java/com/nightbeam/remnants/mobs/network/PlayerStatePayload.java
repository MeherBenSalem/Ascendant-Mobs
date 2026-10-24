package com.nightbeam.remnants.mobs.network;

import com.nightbeam.remnants.mobs.RemnantMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Server to client: the player's own hunter level (-1 when levels are not compared), whether a Hunter's Moon is up,
 * and while they hold a Hunter's Lens, the threat where they stand (-1 otherwise).
 */
public record PlayerStatePayload(int hunterLevel, boolean ras, boolean moon, int threat, int rangeMin, int rangeMax, int structure)
        implements Payload {
    public static final ResourceLocation ID = RemnantMobs.id("player_state");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(hunterLevel + 1);
        buf.writeBoolean(ras);
        buf.writeBoolean(moon);
        buf.writeVarInt(threat + 1);
        buf.writeVarInt(rangeMin);
        buf.writeVarInt(rangeMax);
        buf.writeVarInt(structure);
    }

    public static PlayerStatePayload read(FriendlyByteBuf buf) {
        return new PlayerStatePayload(buf.readVarInt() - 1, buf.readBoolean(), buf.readBoolean(), buf.readVarInt() - 1,
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
