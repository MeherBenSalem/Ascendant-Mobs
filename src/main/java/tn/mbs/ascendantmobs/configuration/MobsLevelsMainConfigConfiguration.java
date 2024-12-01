package tn.mbs.ascendantmobs.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class MobsLevelsMainConfigConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.ConfigValue<Double> SCALE_DISTANCE;
	public static final ForgeConfigSpec.ConfigValue<String> SCALE_TYPE;
	public static final ForgeConfigSpec.ConfigValue<Double> DAY_FACTOR;
	public static final ForgeConfigSpec.ConfigValue<Double> BASE_LEVEL;
	public static final ForgeConfigSpec.ConfigValue<Double> SCALE_FACTOR;
	public static final ForgeConfigSpec.ConfigValue<Boolean> RANDOM_EFFECTS;
	public static final ForgeConfigSpec.ConfigValue<Double> RANDOM_EFFECTS_LEVEL;
	public static final ForgeConfigSpec.ConfigValue<Double> RANDOM_EFFECTS_CHANCE;
	public static final ForgeConfigSpec.ConfigValue<String> ASCENDANT_LOOT_TABLE;
	public static final ForgeConfigSpec.ConfigValue<Boolean> DISPLAY_LVL_NAME;
	public static final ForgeConfigSpec.ConfigValue<Double> DAMAGE_MODIFIER;
	public static final ForgeConfigSpec.ConfigValue<Double> HEALTH_MODIFIER;
	public static final ForgeConfigSpec.ConfigValue<Double> PROTECTION_MODIFIER;
	public static final ForgeConfigSpec.ConfigValue<Double> SPEED_MODIFIER;
	public static final ForgeConfigSpec.ConfigValue<Double> XP_MODFIER;
	public static final ForgeConfigSpec.ConfigValue<Double> MIN_SPEED;
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
		DAMAGE_MODIFIER = BUILDER.define("damage_modifier", (double) 5);
		HEALTH_MODIFIER = BUILDER.define("health_modifier", (double) 5);
		PROTECTION_MODIFIER = BUILDER.define("protection_modifier", (double) 5);
		SPEED_MODIFIER = BUILDER.define("speed_modifier", (double) 0.3);
		XP_MODFIER = BUILDER.define("xp_modfier", (double) 5);
		MIN_SPEED = BUILDER.define("min_speed", (double) 0.1);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

}
