package tn.mbs.ascendantmobs.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class MobsListConfigConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BANNED;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DEATH_MESSAGES;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOCKED_MOBS;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CAN_BE_ASCENDANT;
	static {
		BUILDER.push("Blacklist");
		BANNED = BUILDER.comment("write the registry name of the entities").defineList("banned", List.of(""), entry -> true);
		DEATH_MESSAGES = BUILDER.defineList("death_messages", List.of(), entry -> true);
		LOCKED_MOBS = BUILDER.comment("please respect this format 'xxx/namespace:mobname' where xxx is the level writen in 3 digits for example '010/minecraft:zombie'").defineList("locked_mobs",
				List.of("100/minecraft:ender_dragon", "150/minecraft:wither"), entry -> true);
		CAN_BE_ASCENDANT = BUILDER.defineList("can_be_ascendant", List.of("minecraft:zombie", "minecraft:skeleton"), entry -> true);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

}
