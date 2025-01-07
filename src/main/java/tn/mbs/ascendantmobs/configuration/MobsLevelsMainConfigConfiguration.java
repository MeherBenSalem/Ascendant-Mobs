package tn.mbs.ascendantmobs.configuration;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class MobsLevelsMainConfigConfiguration {
	public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.ConfigValue<Double> SCALE_DISTANCE;
	public static final ModConfigSpec.ConfigValue<String> SCALE_TYPE;
	public static final ModConfigSpec.ConfigValue<Double> DAY_FACTOR;
	public static final ModConfigSpec.ConfigValue<Double> BASE_LEVEL;
	public static final ModConfigSpec.ConfigValue<Double> SCALE_FACTOR;
	public static final ModConfigSpec.ConfigValue<Boolean> RANDOM_EFFECTS;
	public static final ModConfigSpec.ConfigValue<Double> RANDOM_EFFECTS_LEVEL;
	public static final ModConfigSpec.ConfigValue<Double> RANDOM_EFFECTS_CHANCE;
	public static final ModConfigSpec.ConfigValue<String> ASCENDANT_LOOT_TABLE;
	public static final ModConfigSpec.ConfigValue<Boolean> DISPLAY_LVL_NAME;
	public static final ModConfigSpec.ConfigValue<Double> HEALTH_MODIFIER;
	public static final ModConfigSpec.ConfigValue<Double> DAMAGE_MODIFIER;
	public static final ModConfigSpec.ConfigValue<Double> XP_MODFIER;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> SCALING_FACTORS;
	static {
		BUILDER.push("Distance Scale Settings");
		SCALE_DISTANCE = BUILDER.comment("Recommended over 25 000").define("scale_distance", (double) 55000);
		SCALE_TYPE = BUILDER.comment("The scale type can be 'vertical' , 'horizontal' , 'both', 'random' or 'MOTP'").define("scale_type", "both");
		DAY_FACTOR = BUILDER.define("day_factor", (double) 1);
		BUILDER.pop();
		BUILDER.push("Mobs Levels Settings");
		BASE_LEVEL = BUILDER.define("base_level", (double) 0);
		SCALE_FACTOR = BUILDER.define("scale_factor", (double) 0.5);
		RANDOM_EFFECTS = BUILDER.comment("Mods after a cartain level get random buffs ").define("random_effects", true);
		RANDOM_EFFECTS_LEVEL = BUILDER.comment("[1-200]").define("ascendant_effects_min_level", (double) 25);
		RANDOM_EFFECTS_CHANCE = BUILDER.comment("[1-100]").define("ascendant_effects_chance", (double) 0.5);
		ASCENDANT_LOOT_TABLE = BUILDER.define("ascendant_loot_table", "minecraft:chests/end_city_treasure");
		BUILDER.pop();
		BUILDER.push("Ingame Overlay Settings");
		DISPLAY_LVL_NAME = BUILDER.define("display_lvl_name", true);
		BUILDER.pop();
		BUILDER.push("Scale Factors");
		HEALTH_MODIFIER = BUILDER.define("health_modifier", (double) 5);
		DAMAGE_MODIFIER = BUILDER.define("damage_modifier", (double) 5);
		XP_MODFIER = BUILDER.define("xp_modfier", (double) 5);
		SCALING_FACTORS = BUILDER.defineList("scaling_factors", List.of("[base]1[baseEnd][modifier]0.3[modifierEnd][max]30[maxEnd][cmd]attribute @s minecraft:generic.armor base set {value}"), entry -> true);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

}
