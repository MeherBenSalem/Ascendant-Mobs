package com.nightbeam.remnants.mobs.event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nightbeam.remnants.mobs.RemnantMobs;
import com.nightbeam.remnants.mobs.config.LevelsConfig;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.content.ModContent;
import com.nightbeam.remnants.mobs.mob.WorldTime;
import com.nightbeam.remnants.mobs.network.Network;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

/**
 * The Hunter's Moon: on full-moon nights, a chance that monsters spawn stronger, ranks come more often and shards
 * drop more until dawn. Its state is kept in {@code <world>/remnant_mobs.json}.
 */
public final class HuntersMoon {
    private static boolean active;
    private static long endTime;
    private static long startTime;
    private static long rolledDay = Long.MIN_VALUE;
    private static @Nullable Path file;

    private HuntersMoon() {
    }

    public static boolean active() {
        return active;
    }

    /** True when the Hunter's Moon is up and {@code level} has a day-night cycle. */
    public static boolean affects(ServerLevel level) {
        return active && !level.dimensionType().hasFixedTime();
    }

    public static void load(MinecraftServer server) {
        active = false;
        rolledDay = Long.MIN_VALUE;
        file = server.getWorldPath(LevelResource.ROOT).resolve(RemnantMobs.MOD_ID + ".json");
        try {
            if (Files.exists(file)) {
                JsonObject o = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                active = o.has("moon_active") && o.get("moon_active").getAsBoolean();
                startTime = o.has("moon_start") ? o.get("moon_start").getAsLong() : 0;
                endTime = o.has("moon_end") ? o.get("moon_end").getAsLong() : 0;
                rolledDay = o.has("moon_rolled_day") ? o.get("moon_rolled_day").getAsLong() : Long.MIN_VALUE;
            }
        } catch (IOException | RuntimeException e) {
            RemnantMobs.LOG.warn("Could not read {}: {}", file, e.toString());
        }
    }

    public static void unload() {
        save();
        active = false;
        file = null;
    }

    private static void save() {
        if (file == null) {
            return;
        }
        JsonObject o = new JsonObject();
        o.addProperty("moon_active", active);
        o.addProperty("moon_start", startTime);
        o.addProperty("moon_end", endTime);
        o.addProperty("moon_rolled_day", rolledDay);
        try {
            Files.writeString(file, o.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            RemnantMobs.LOG.warn("Could not write {}: {}", file, e.toString());
        }
    }

    /** Checked once a second. */
    public static void tick(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        LevelsConfig c = RemnantsConfig.levels();
        long time = WorldTime.time(overworld);
        if (active) {
            if (!c.moonEnabled || time >= endTime || time < startTime) {
                stop(server, true);
            }
            return;
        }
        if (!c.moonEnabled || !WorldTime.night(overworld) || !WorldTime.fullMoon(overworld)) {
            return;
        }
        long day = WorldTime.dayIndex(overworld);
        if (rolledDay == day) {
            return;
        }
        rolledDay = day;
        if (overworld.getRandom().nextDouble() < c.moonChance) {
            start(server);
        } else {
            save();
        }
    }

    /** Raises the Hunter's Moon until the next dawn. */
    public static void start(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        long time = WorldTime.time(overworld);
        long day = WorldTime.dayIndex(overworld);
        int timeOfDay = WorldTime.timeOfDay(overworld);
        active = true;
        startTime = time;
        endTime = day * WorldTime.DAY + 23000L + (timeOfDay >= 23000 ? WorldTime.DAY : 0);
        rolledDay = day;
        save();
        Component title = Component.translatable("event.remnant_mobs.hunters_moon.title").withStyle(ChatFormatting.DARK_RED);
        Component subtitle = Component.translatable("event.remnant_mobs.hunters_moon.subtitle").withStyle(ChatFormatting.GOLD);
        Component chat = Component.translatable("event.remnant_mobs.hunters_moon.chat").withStyle(ChatFormatting.RED);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Network.syncPlayer(player);
            if (player.level().dimensionType().hasFixedTime()) {
                continue;
            }
            Feedback.title(player, title, subtitle, 20, 70, 30);
            Feedback.sound(player, ModContent.MOON_RISE.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
            player.sendSystemMessage(chat);
        }
        RemnantMobs.LOG.info("A Hunter's Moon rises");
    }

    public static void stop(MinecraftServer server, boolean announce) {
        if (!active) {
            return;
        }
        active = false;
        save();
        Component message = Component.translatable("event.remnant_mobs.hunters_moon.end").withStyle(ChatFormatting.GRAY);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Network.syncPlayer(player);
            if (announce && !player.level().dimensionType().hasFixedTime()) {
                Feedback.actionBar(player, message);
            }
        }
    }

    /** Remaining ticks, for {@code /remnants moon}. */
    public static long remaining(MinecraftServer server) {
        return active ? Math.max(0, endTime - WorldTime.time(server.overworld())) : 0;
    }
}
