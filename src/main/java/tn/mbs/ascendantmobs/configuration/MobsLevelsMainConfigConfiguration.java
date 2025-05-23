package tn.mbs.ascendantmobs.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class MobsLevelsMainConfigConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ATTRIBUTES_LIST;
	public static final ForgeConfigSpec.ConfigValue<Double> XP_MODFIER;
	public static final ForgeConfigSpec.ConfigValue<Double> SCALE_DISTANCE;
	public static final ForgeConfigSpec.ConfigValue<String> SCALE_TYPE;
	public static final ForgeConfigSpec.ConfigValue<Double> DAY_FACTOR;
	public static final ForgeConfigSpec.ConfigValue<Double> BASE_LEVEL;
	public static final ForgeConfigSpec.ConfigValue<Double> SCALE_FACTOR;
	public static final ForgeConfigSpec.ConfigValue<Boolean> RANDOM_EFFECTS;
	public static final ForgeConfigSpec.ConfigValue<Double> RANDOM_EFFECTS_LEVEL;
	public static final ForgeConfigSpec.ConfigValue<Double> RANDOM_EFFECTS_CHANCE;
	public static final ForgeConfigSpec.ConfigValue<String> ASCENDANT_LOOT_TABLE;
	public static final ForgeConfigSpec.ConfigValue<Boolean> USE_LEGACY_HUD;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HIDE_HUD_FOR;
	static {
		BUILDER.push("Attributes Scale Settings");
		ATTRIBUTES_LIST = BUILDER.comment("Configures attribute scaling with mob levels: [attribute] defines the stat, [value] is per-level increment, [max] is the cap, [mob] (optional) targets specific mobs or all").defineList("attributes_list",
				List.of("[attribute]minecraft:generic.max_health[attributeEnd][value]4[valueEnd][max]800[maxEnd]", "[attribute]minecraft:generic.movement_speed[attributeEnd][value]0.0025[valueEnd][max]0.33[maxEnd]",
						"[attribute]minecraft:generic.armor[attributeEnd][value]0.2[valueEnd][max]30[maxEnd]", "[attribute]minecraft:generic.attack_damage[attributeEnd][value]1[valueEnd][max]400[maxEnd]"),
				entry -> true);
		XP_MODFIER = BUILDER.comment("extra xp per level").define("xp_modfier", (double) 5);
		BUILDER.pop();
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
		USE_LEGACY_HUD = BUILDER.define("use_legacy_hud", true);
		HIDE_HUD_FOR = BUILDER.defineList("hide_hud_for", List.of(" "), entry -> true);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

}
