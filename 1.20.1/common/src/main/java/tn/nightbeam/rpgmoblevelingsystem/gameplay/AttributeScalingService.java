package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;

import java.util.UUID;

public final class AttributeScalingService {
    private static final UUID LEVEL_MODIFIER_ID = UUID.fromString("8f3c2a10-6b4d-4e91-9c2f-1d7e8a5b3c40");
    private static final String MODIFIER_NAME = "rpgmoblevelingsystem:level";

    private AttributeScalingService() {
    }

    public static void apply(Entity entity, LivingEntity living, double level) {
        for (ModConfig.AttributeRule rule : ModConfig.attributes().attributes) {
            if (rule.mobFilter != null && !rule.mobFilter.isEmpty() && !EntityClassification.entityTypeId(entity).equals(rule.mobFilter)) {
                continue;
            }
            if (rule.hostileOnly && !EntityClassification.isHostile(living)) {
                continue;
            }
            if (ModConfig.mobs().hostileOnly && !EntityClassification.isHostile(living)) {
                continue;
            }

            var key = rule.attributeKey();
            if (key == null) {
                continue;
            }

            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(key);
            if (attribute == null) {
                continue;
            }

            AttributeInstance instance = living.getAttribute(attribute);
            if (instance == null) {
                continue;
            }

            removeLevelModifier(instance);

            double amount = computeAmount(instance, rule, level);
            if (amount == 0) {
                continue;
            }

            instance.addPermanentModifier(new AttributeModifier(LEVEL_MODIFIER_ID, MODIFIER_NAME, amount, AttributeModifier.Operation.ADDITION));
        }

        applyPlayerBalance(living, level);
        living.setHealth((float) Math.min(living.getHealth(), living.getMaxHealth()));
    }

    public static void clear(LivingEntity living) {
        for (ModConfig.AttributeRule rule : ModConfig.attributes().attributes) {
            var key = rule.attributeKey();
            if (key == null) {
                continue;
            }
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(key);
            if (attribute == null) {
                continue;
            }
            AttributeInstance instance = living.getAttribute(attribute);
            if (instance != null) {
                removeLevelModifier(instance);
            }
        }
        living.getTags().removeIf(tag -> tag.startsWith(MobLevelStorage.TAG_LEVEL_PREFIX));
        living.getTags().remove(MobLevelStorage.TAG_GOT_LEVEL);
    }

    private static void applyPlayerBalance(LivingEntity living, double level) {
        if (!ModConfig.scale().playerBalanceEnabled || !RasCompatibilityLayer.isRasLoaded()) {
            return;
        }
        if (!(living.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        var players = serverLevel.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class,
                living.getBoundingBox().inflate(ModConfig.scale().playerScaleRadius), net.minecraft.world.entity.player.Player::isAlive);
        if (players.isEmpty()) {
            return;
        }
        RasCompatibilityLayer.PlayerCombatSnapshot snapshot = RasCompatibilityLayer.getCombatSnapshot(players.get(0));
        if (snapshot.rpgLevel() <= 0) {
            return;
        }

        AttributeInstance health = living.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance damage = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (health != null && level < snapshot.rpgLevel()) {
            double target = snapshot.maxHealth() * ModConfig.scale().playerBalanceHealthRatio * (level / Math.max(1, snapshot.rpgLevel()));
            if (health.getBaseValue() > target && target > 0) {
                health.setBaseValue(target);
            }
        }
        if (damage != null && level < snapshot.rpgLevel()) {
            double target = snapshot.attackDamage() * ModConfig.scale().playerBalanceDamageRatio * (level / Math.max(1, snapshot.rpgLevel()));
            if (damage.getBaseValue() > target && target > 0) {
                damage.setBaseValue(target);
            }
        }
    }

    private static double computeAmount(AttributeInstance instance, ModConfig.AttributeRule rule, double level) {
        if (rule.isPercent()) {
            double boosted = instance.getBaseValue() * (level * rule.valuePerLevel / 100.0);
            return Math.min(boosted, rule.maxValue);
        }
        double additive = level * rule.valuePerLevel;
        return Math.min(additive, rule.maxValue);
    }

    private static void removeLevelModifier(AttributeInstance instance) {
        instance.removeModifier(LEVEL_MODIFIER_ID);
    }
}
