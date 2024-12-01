package tn.mbs.ascendantmobs.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CustomDimensionsConfigConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DIMENSIONS_SETTINGS;
	static {
		BUILDER.push("Custom Dimensions Config");
		DIMENSIONS_SETTINGS = BUILDER
				.comment("(min/max/dimensionName) Make sure levels are written in 3 digits, e.g. (005/010/twilightforest:twilight_forest) in this example, you will limit the spawning level for the twilight forest to level 5 and level 10.")
				.defineList("dimensions_settings", List.of("000/075/minecraft:overworld", "050/100/minecraft:the_nether", "100/200/minecraft:the_end", "020/050/the_bumblezone:the_bumblezone", "005/010/twilightforest:twilight_forest"), entry -> true);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

}
