package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;

public final class AscendantService {
    private static final String TAG_ASCENDANT = "am_ascendant";

    private AscendantService() {
    }

    public static void tryApplyAscendant(LivingEntity living) {
        if (living.level().isClientSide() || living.getTags().contains(TAG_ASCENDANT)) {
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

        living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60 * 5, 1));
        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 60 * 5, 0));
        living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 60 * 5, 0));
        living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 60 * 5, 0));

        if (living.level() instanceof ServerLevel serverLevel) {
            var lootKey = ResourceLocation.tryParse(ModConfig.global().ascendantLootTable);
            if (lootKey != null) {
                living.addTag("am_ascendant_loot:" + lootKey);
            }
            String bossId = "am_" + living.getUUID();
            var source = serverLevel.getServer().createCommandSourceStack();
            serverLevel.getServer().getCommands().performPrefixedCommand(source, "bossbar add " + bossId + " {\"text\":\"Ascendant\"}");
            serverLevel.getServer().getCommands().performPrefixedCommand(source, "bossbar set " + bossId + " players @a[distance=..48]");
            serverLevel.getServer().getCommands().performPrefixedCommand(source, "bossbar set " + bossId + " max " + (int) living.getMaxHealth());
            serverLevel.getServer().getCommands().performPrefixedCommand(source, "bossbar set " + bossId + " value " + (int) living.getHealth());
        }
    }

    public static void onAscendantDeath(LivingEntity living) {
        if (!living.getTags().contains(TAG_ASCENDANT) || !(living.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        String bossId = "am_" + living.getUUID();
        serverLevel.getServer().getCommands().performPrefixedCommand(serverLevel.getServer().createCommandSourceStack(), "bossbar remove " + bossId);
        String loot = living.getTags().stream().filter(tag -> tag.startsWith("am_ascendant_loot:")).map(tag -> tag.substring("am_ascendant_loot:".length())).findFirst().orElse("");
        if (!loot.isEmpty()) {
            serverLevel.getServer().getCommands().performPrefixedCommand(
                    serverLevel.getServer().createCommandSourceStack(),
                    "loot spawn " + (int) living.getX() + " " + (int) living.getY() + " " + (int) living.getZ() + " loot " + loot
            );
        }
        var bolt = new net.minecraft.world.entity.LightningBolt(net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, serverLevel);
        bolt.moveTo(living.getX(), living.getY(), living.getZ());
        serverLevel.addFreshEntity(bolt);
    }
}
