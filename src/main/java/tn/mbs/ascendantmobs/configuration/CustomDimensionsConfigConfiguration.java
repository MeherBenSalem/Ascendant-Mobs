package tn.mbs.ascendantmobs.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CustomDimensionsConfigConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DIMENSIONS_SETTINGS;
	static {
		BUILDER.push("Custom Dimensions Config");
		DIMENSIONS_SETTINGS = BUILDER.defineList("dimensions_settings", List.of("[min]0[minEnd][max]75[maxEnd]minecraft:overworld", "[min]50[minEnd][max]100[maxEnd]minecraft:the_nether", "[min]100[minEnd][max]200[maxEnd]minecraft:the_end",
				"[min]20[minEnd][max]50[maxEnd]the_bumblezone:the_bumblezone", "[min]5[minEnd][max]10[maxEnd]twilightforest:twilight_forest"), entry -> true);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

}