package tn.mbs.ascendantmobs.configuration;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class MobsListConfigConfiguration {
	public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.ConfigValue<List<? extends String>> BANNED;
	public static final ModConfigSpec.ConfigValue<Boolean> DEATH_MESSAGES;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> LOCKED_MOBS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> CAN_BE_ASCENDANT;
	static {
		BUILDER.push("Blacklist");
		BANNED = BUILDER.comment("write the registry name of the entities").defineList("banned", List.of(" "), entry -> true);
		DEATH_MESSAGES = BUILDER.define("death_messages", false);
		LOCKED_MOBS = BUILDER.defineList("locked_mobs", List.of("100/minecraft:ender_dragon", "150/minecraft:wither"), entry -> true);
		CAN_BE_ASCENDANT = BUILDER.defineList("can_be_ascendant", List.of("minecraft:zombie", "minecraft:skeleton"), entry -> true);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

}
