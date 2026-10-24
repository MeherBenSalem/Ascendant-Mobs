package com.nightbeam.remnants.mobs.config;

import com.google.gson.JsonObject;
import java.util.Locale;

import static com.nightbeam.remnants.mobs.config.Json.obj;

/** {@code config/remnant_mobs/client.json}: what you see. Only read on the client. */
public final class ClientConfig {
    public final boolean plates;
    public final double distance;
    public final double commonDistance;
    public final double traitsDistance;
    public final boolean lookAtOnly;
    public final boolean healthNumbers;
    public final double scale;
    public final boolean relativeColors;
    public final boolean hideVanillaName;
    public final boolean hideWithHud;
    public final boolean lensHud;
    public final boolean moonIndicator;

    private ClientConfig(JsonObject o) {
        plates = Json.bool(o, "nameplates.enabled", true);
        distance = Math.max(2.0, Math.min(64.0, Json.num(o, "nameplates.distance", 24)));
        commonDistance = Math.max(0.0, Math.min(64.0, Json.num(o, "nameplates.common_distance", 12)));
        traitsDistance = Math.max(0.0, Math.min(64.0, Json.num(o, "nameplates.traits_distance", 12)));
        lookAtOnly = Json.bool(o, "nameplates.look_at_only", false);
        healthNumbers = Json.bool(o, "nameplates.health_numbers", true);
        scale = Math.max(0.5, Math.min(2.0, Json.num(o, "nameplates.scale", 1.0)));
        relativeColors = !"absolute".equals(Json.str(o, "nameplates.level_colors", "relative").toLowerCase(Locale.ROOT));
        hideVanillaName = Json.bool(o, "nameplates.replace_name_tag", true);
        hideWithHud = Json.bool(o, "nameplates.hide_with_hud", true);
        lensHud = Json.bool(o, "hud.lens", true);
        moonIndicator = Json.bool(o, "hud.hunters_moon", true);
    }

    public static ClientConfig from(JsonObject json) {
        return new ClientConfig(json);
    }

    public static CommentedJson spec() {
        JsonObject d = obj(
                "nameplates", obj("enabled", true, "distance", 24, "common_distance", 12, "traits_distance", 12,
                        "look_at_only", false, "health_numbers", true, "scale", 1.0, "level_colors", "relative",
                        "replace_name_tag", true, "hide_with_hud", true),
                "hud", obj("lens", true, "hunters_moon", true));
        return new CommentedJson(d)
                .header("Remnants: RPG Mobs 3.0: client-side visuals. These only change what you see.")
                .comment("nameplates", "Plates above leveled mobs. distance: how far ranked mobs show theirs; common_distance: the same\n"
                        + "for Common mobs (0 hides them); traits_distance: how close you must be to read traits.\n"
                        + "look_at_only: only the mob under your crosshair. health_numbers: current health beside the bar.\n"
                        + "scale: plate size (0.5-2). level_colors: \"relative\" colours levels against your own level (RPG\n"
                        + "Attribute System, or experience levels if the server uses them), \"absolute\" keeps them neutral.\n"
                        + "replace_name_tag: named mobs show their name on the plate instead of a separate name tag.\n"
                        + "hide_with_hud: hide plates along with the HUD (F1), like vanilla name tags.")
                .comment("hud", "lens: the Hunter's Lens threat readout and scope stat sheet. hunters_moon: a moon icon while a\n"
                        + "Hunter's Moon is up.");
    }
}
