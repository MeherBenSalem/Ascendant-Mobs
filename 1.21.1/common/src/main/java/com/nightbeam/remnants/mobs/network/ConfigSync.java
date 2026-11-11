package com.nightbeam.remnants.mobs.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nightbeam.remnants.mobs.RemnantMobs;
import com.nightbeam.remnants.mobs.config.CommentedJson;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.config.edit.ConfigFiles;
import com.nightbeam.remnants.mobs.config.edit.ConfigOption.Side;
import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * The server side of the settings screen. The JSON files stay the only source of truth: the server sends their values,
 * and writes a player's changes only after checking that the player may change server settings (the host of a
 * single-player or LAN world, or an operator: the permission level of {@code /remnants reload}) and checking every value
 * against the loader's rules again. Then it reloads like {@code /remnants reload} and syncs every player.
 */
public final class ConfigSync {
    public static final int READ_ONLY = 0;
    public static final int OPERATOR = 1;
    public static final int HOST = 2;

    public static final int NONE = 0;
    public static final int SAVED = 1;
    public static final int PARTLY = 2;
    public static final int DENIED = 3;
    public static final int FAILED = 4;

    private ConfigSync() {
    }

    /** What {@code player} may do with server settings. */
    public static int access(ServerPlayer player) {
        if (player.server.isSingleplayerOwner(player.getGameProfile())) {
            return HOST;
        }
        return player.hasPermissions(2) ? OPERATOR : READ_ONLY;
    }

    /** {@code /remnants config}: opens the screen on the player's client. False when their client cannot show it. */
    public static boolean open(ServerPlayer player) {
        if (!RemnantMobs.platform().hasChannel(player, ConfigValuesPayload.TYPE)) {
            return false;
        }
        RemnantMobs.platform().sendToPlayer(player, values(player, true, NONE, 0, ""));
        return true;
    }

    /** When each player last asked: requests and saves read every file, so a client may not flood the server with them. */
    private static final Map<ServerPlayer, Long> LAST = new WeakHashMap<>();
    private static final long MIN_INTERVAL_MS = 250;

    /** Runs on the server thread. False when the player asked less than {@link #MIN_INTERVAL_MS} ago. */
    private static boolean allow(ServerPlayer player) {
        long now = System.currentTimeMillis();
        Long last = LAST.put(player, now);
        return last == null || now - last >= MIN_INTERVAL_MS;
    }

    public static void onRequest(ServerPlayer player) {
        MinecraftServer server = player.server;
        server.execute(() -> {
            if (allow(player)) {
                RemnantMobs.platform().sendToPlayer(player, values(player, false, NONE, 0, ""));
            }
        });
    }

    public static void onUpdate(ServerPlayer player, ConfigUpdatePayload payload) {
        MinecraftServer server = player.server;
        server.execute(() -> {
            if (allow(player)) {
                RemnantMobs.platform().sendToPlayer(player, apply(server, player, payload.changes()));
            } else {
                RemnantMobs.LOG.warn("Ignoring Remnants settings from {}: sent too quickly", player.getScoreboardName());
            }
        });
    }

    private static ConfigValuesPayload apply(MinecraftServer server, ServerPlayer player, String text) {
        int access = access(player);
        if (access == READ_ONLY) {
            RemnantMobs.LOG.warn("{} tried to change Remnants settings without permission", player.getScoreboardName());
            return values(player, false, DENIED, 0, "");
        }
        Map<String, Map<String, JsonElement>> changes = parse(text);
        // The host's own settings live in the same files as the world's, so the host may change both.
        Set<Side> sides = access == HOST ? EnumSet.allOf(Side.class) : EnumSet.of(Side.SERVER);
        ConfigFiles.Result result;
        try {
            result = ConfigFiles.apply(RemnantsConfig.dir(), changes, sides);
        } catch (IOException | RuntimeException e) {
            RemnantMobs.LOG.error("Could not save Remnants settings changed by {}", player.getScoreboardName(), e);
            return values(player, false, FAILED, 0, String.valueOf(e.getMessage()));
        }
        if (result.written() > 0) {
            RemnantMobs.LOG.info("{} changed {} Remnants setting(s) in {}", player.getScoreboardName(), result.written(), changes.keySet());
            reload(server);
        }
        if (!result.refused().isEmpty()) {
            RemnantMobs.LOG.warn("Refused Remnants settings from {}: {}", player.getScoreboardName(), result.refused());
        }
        return values(player, false, result.refused().isEmpty() ? SAVED : PARTLY, result.written(), String.join(", ", result.refused()));
    }

    /** What {@code /remnants reload} does: read every file again and resend what players see. */
    public static void reload(MinecraftServer server) {
        RemnantsConfig.load(RemnantMobs.platform().configDir());
        Network.clear();
        Network.syncPlayers(server);
    }

    private static ConfigValuesPayload values(ServerPlayer player, boolean open, int status, int count, String detail) {
        JsonObject root = new JsonObject();
        ConfigFiles.serverFiles(ConfigFiles.read(RemnantsConfig.dir())).forEach(root::add);
        String shortDetail = detail.length() > 1000 ? detail.substring(0, 1000) + "..." : detail;
        return new ConfigValuesPayload(open, access(player), status, count, shortDetail, root.toString());
    }

    /** file → path → value; anything that is not shaped like that is dropped (and so refused). */
    static Map<String, Map<String, JsonElement>> parse(String text) {
        Map<String, Map<String, JsonElement>> changes = new LinkedHashMap<>();
        try {
            JsonElement root = CommentedJson.parse(text);
            if (root != null && root.isJsonObject()) {
                for (Map.Entry<String, JsonElement> file : root.getAsJsonObject().entrySet()) {
                    if (file.getValue().isJsonObject()) {
                        Map<String, JsonElement> values = new LinkedHashMap<>();
                        file.getValue().getAsJsonObject().entrySet().forEach(e -> values.put(e.getKey(), e.getValue()));
                        changes.put(file.getKey(), values);
                    }
                }
            }
        } catch (RuntimeException e) {
            RemnantMobs.LOG.warn("Ignoring malformed Remnants settings update: {}", e.getMessage());
        }
        return changes;
    }

    /** The JSON a client sends: file → path → value. */
    public static String encode(Map<String, Map<String, JsonElement>> changes) {
        JsonObject root = new JsonObject();
        changes.forEach((file, values) -> {
            JsonObject object = new JsonObject();
            values.forEach(object::add);
            root.add(file, object);
        });
        return root.toString();
    }
}
