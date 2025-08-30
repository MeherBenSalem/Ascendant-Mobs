package tn.mbs.ascendantmobs.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class MobsLevelsMainConfigConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Double> BASE_LEVEL;
	public static final ForgeConfigSpec.ConfigValue<Double> SCALE_FACTOR;
	public static final ForgeConfigSpec.ConfigValue<Boolean> RANDOM_EFFECTS;
	public static final ForgeConfigSpec.ConfigValue<Double> RANDOM_EFFECTS_LEVEL;
	public static final ForgeConfigSpec.ConfigValue<Double> RANDOM_EFFECTS_CHANCE;
	public static final ForgeConfigSpec.ConfigValue<String> ASCENDANT_LOOT_TABLE;
	public static final ForgeConfigSpec.ConfigValue<Boolean> USE_LEGACY_HUD;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HIDE_HUD_FOR;
	static {
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