package tn.nightbeam.rpgmoblevelingsystem.fabric.config;

import tn.naizo.jauml.JaumlConfigLib;

public final class FabricConfigBootstrap {
    private FabricConfigBootstrap() {
    }

    public static void ensureDefaults() {
        ensureGlobalConfig();
        ensureScaleConfig();
        ensureDimensionsConfig();
        ensureAttributesConfig();
        ensureMobListConfig();
    }

    private static void ensureGlobalConfig() {
        String dir = "rpgmoblevelingsystem";
        String file = "global_settings";
        JaumlConfigLib.createConfigFile(dir, file);

        if (!JaumlConfigLib.arrayKeyExists(dir, file, "ascendant_loot_table")) {
            JaumlConfigLib.setStringValue(dir, file, "ascendant_loot_table", "minecraft:chests/end_city_treasure");
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "ascendant_effects_min_level")) {
            JaumlConfigLib.setNumberValue(dir, file, "ascendant_effects_min_level", 50);
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "ascendant_effects_chance")) {
            JaumlConfigLib.setNumberValue(dir, file, "ascendant_effects_chance", 0.5);
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "random_effects")) {
            JaumlConfigLib.setBooleanValue(dir, file, "random_effects", true);
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "use_legacy_hud")) {
            JaumlConfigLib.setBooleanValue(dir, file, "use_legacy_hud", true);
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "hide_hud_for")) {
            JaumlConfigLib.addStringToArray(dir, file, "hide_hud_for", "");
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "overlay_x_offset")) {
            JaumlConfigLib.setNumberValue(dir, file, "overlay_x_offset", 0);
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "overlay_y_offset")) {
            JaumlConfigLib.setNumberValue(dir, file, "overlay_y_offset", 0.45);
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "overlay_z_offset")) {
            JaumlConfigLib.setNumberValue(dir, file, "overlay_z_offset", 0);
        }
    }

    private static void ensureScaleConfig() {
        String dir = "rpgmoblevelingsystem";
        String file = "scale_settings";
        JaumlConfigLib.createConfigFile(dir, file);

        if (!JaumlConfigLib.arrayKeyExists(dir, file, "scale_type")) {
            JaumlConfigLib.setStringValue(dir, file, "scale_type", "both");
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "base_level")) {
            JaumlConfigLib.setNumberValue(dir, file, "base_level", 0);
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "scale_factor")) {
            JaumlConfigLib.setNumberValue(dir, file, "scale_factor", 0.05);
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "scale_distance")) {
            JaumlConfigLib.setNumberValue(dir, file, "scale_distance", 100000);
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "day_factor")) {
            JaumlConfigLib.setNumberValue(dir, file, "day_factor", 1);
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "xp_modfier")) {
            JaumlConfigLib.setNumberValue(dir, file, "xp_modfier", 5);
        }
    }

    private static void ensureDimensionsConfig() {
        String dir = "rpgmoblevelingsystem";
        String file = "dimensions_settings";
        JaumlConfigLib.createConfigFile(dir, file);

        if (!JaumlConfigLib.arrayKeyExists(dir, file, "dimensions")) {
            JaumlConfigLib.addStringToArray(dir, file, "dimensions", "[min]0[minEnd][max]40[maxEnd]minecraft:overworld");
            JaumlConfigLib.addStringToArray(dir, file, "dimensions", "[min]20[minEnd][max]80[maxEnd]minecraft:the_nether");
            JaumlConfigLib.addStringToArray(dir, file, "dimensions", "[min]40[minEnd][max]100[maxEnd]minecraft:the_end");
            JaumlConfigLib.addStringToArray(dir, file, "dimensions", "[min]10[minEnd][max]60[maxEnd]aether:the_aether");
            JaumlConfigLib.addStringToArray(dir, file, "dimensions", "[min]10[minEnd][max]60[maxEnd]twilightforest:twilight_forest");
            JaumlConfigLib.addStringToArray(dir, file, "dimensions", "[min]10[minEnd][max]60[maxEnd]eternal_starlight:starligh");
            JaumlConfigLib.addStringToArray(dir, file, "dimensions", "[min]10[minEnd][max]60[maxEnd]deeperdarker:otherside");
        }
    }

    private static void ensureAttributesConfig() {
        String dir = "rpgmoblevelingsystem";
        String file = "attributes_settings";
        JaumlConfigLib.createConfigFile(dir, file);

        if (!JaumlConfigLib.arrayKeyExists(dir, file, "attributes_list")) {
            JaumlConfigLib.addStringToArray(dir, file, "attributes_list", "[attribute]minecraft:generic.max_health[attributeEnd][value]2.0[valueEnd][max]200[maxEnd]");
            JaumlConfigLib.addStringToArray(dir, file, "attributes_list", "[attribute]minecraft:generic.movement_speed[attributeEnd][value]0.0015[valueEnd][max]0.15[maxEnd]");
            JaumlConfigLib.addStringToArray(dir, file, "attributes_list", "[attribute]minecraft:generic.armor[attributeEnd][value]0.1[valueEnd][max]10[maxEnd]");
            JaumlConfigLib.addStringToArray(dir, file, "attributes_list", "[attribute]minecraft:generic.attack_damage[attributeEnd][value]1.0[valueEnd][max]100[maxEnd]");
        }
    }

    private static void ensureMobListConfig() {
        String dir = "rpgmoblevelingsystem";
        String file = "mobs_list_settings";
        JaumlConfigLib.createConfigFile(dir, file);

        if (!JaumlConfigLib.arrayKeyExists(dir, file, "banned")) {
            JaumlConfigLib.addStringToArray(dir, file, "banned", "minecraft:dolphin");
            JaumlConfigLib.addStringToArray(dir, file, "banned", "minecraft:bat");
            JaumlConfigLib.addStringToArray(dir, file, "banned", "minecraft:pufferfish");
            JaumlConfigLib.addStringToArray(dir, file, "banned", "minecraft:salmon");
            JaumlConfigLib.addStringToArray(dir, file, "banned", "minecraft:cod");
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "death_messages")) {
            JaumlConfigLib.setBooleanValue(dir, file, "death_messages", false);
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "locked_mobs")) {
            JaumlConfigLib.addStringToArray(dir, file, "locked_mobs", "10/minecraft:ender_dragon");
            JaumlConfigLib.addStringToArray(dir, file, "locked_mobs", "10/minecraft:wither");
            JaumlConfigLib.addStringToArray(dir, file, "locked_mobs", "10/minecraft:warden");
        }
        if (!JaumlConfigLib.arrayKeyExists(dir, file, "can_be_ascendant")) {
            JaumlConfigLib.addStringToArray(dir, file, "can_be_ascendant", "minecraft:zombie");
            JaumlConfigLib.addStringToArray(dir, file, "can_be_ascendant", "minecraft:skeleton");
        }
    }
}
