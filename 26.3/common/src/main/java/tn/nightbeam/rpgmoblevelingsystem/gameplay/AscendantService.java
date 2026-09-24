package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;

public final class AscendantService {
    private static final String TAG_ASCENDANT = "am_ascendant";

    private AscendantService() {
    }

    public static void tryApplyAscendant(LivingEntity living) {
        if (living.level().isClientSide() || living.entityTags().contains(TAG_ASCENDANT)) {
            return;
        }
        String id = EntityClassification.entityTypeId(living);
        if (!ModConfig.mobs().canBeAscendant.contains(id)) {
            return;
        }
        Double level = MobLevelStorage.getStoredLevel(living);
        if (level == null || level < ModConfig.global().ascendantEffectsMinLevel) {
            return;
        }
        if (living.getRandom().nextDouble() > ModConfig.global().ascendantEffectsChance) {
            return;
        }

        living.addTag(TAG_ASCENDANT);
        if (!ModConfig.global().randomEffects) {
            return;
        }

        addEffect(living, MobEffects.STRENGTH, 20 * 60 * 5, 1);
        addEffect(living, MobEffects.SPEED, 20 * 60 * 5, 0);
        addEffect(living, MobEffects.RESISTANCE, 20 * 60 * 5, 0);
        addEffect(living, MobEffects.REGENERATION, 20 * 60 * 5, 0);

        if (living.level() instanceof ServerLevel serverLevel) {
            var lootKey = Identifier.tryParse(ModConfig.global().ascendantLootTable);
            if (lootKey != null) {
                living.addTag("am_ascendant_loot:" + lootKey);
            }
            String bossId = "am_" + living.getUUID();
            CommandSourceStack source = commandSource(serverLevel, living);
            serverLevel.getServer().getCommands().performPrefixedCommand(source, "bossbar add " + bossId + " {\"text\":\"Ascendant\"}");
            serverLevel.getServer().getCommands().performPrefixedCommand(source, "bossbar set " + bossId + " players @a[distance=..48]");
            serverLevel.getServer().getCommands().performPrefixedCommand(source, "bossbar set " + bossId + " max " + (int) living.getMaxHealth());
            serverLevel.getServer().getCommands().performPrefixedCommand(source, "bossbar set " + bossId + " value " + (int) living.getHealth());
        }
    }

    public static void onAscendantDeath(LivingEntity living) {
        if (!living.entityTags().contains(TAG_ASCENDANT) || !(living.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        CommandSourceStack source = commandSource(serverLevel, living);
        String bossId = "am_" + living.getUUID();
        serverLevel.getServer().getCommands().performPrefixedCommand(source, "bossbar remove " + bossId);
        String loot = living.entityTags().stream().filter(tag -> tag.startsWith("am_ascendant_loot:")).map(tag -> tag.substring("am_ascendant_loot:".length())).findFirst().orElse("");
        if (!loot.isEmpty()) {
            serverLevel.getServer().getCommands().performPrefixedCommand(
                    source,
                    "loot spawn " + (int) living.getX() + " " + (int) living.getY() + " " + (int) living.getZ() + " loot " + loot
            );
        }
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.EVENT);
        if (bolt != null) {
            bolt.snapTo(living.getX(), living.getY(), living.getZ());
            serverLevel.addFreshEntity(bolt);
        }
    }

    private static void addEffect(LivingEntity living, Holder<MobEffect> effect, int duration, int amplifier) {
        living.addEffect(new MobEffectInstance(effect, duration, amplifier));
    }

    private static CommandSourceStack commandSource(ServerLevel serverLevel, LivingEntity living) {
        return new CommandSourceStack(
                CommandSource.NULL,
                living.position(),
                Vec2.ZERO,
                serverLevel,
                PermissionSet.ALL_PERMISSIONS,
                serverLevel.getServer(),
                living
        );
    }
}
