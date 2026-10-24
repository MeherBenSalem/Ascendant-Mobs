package com.nightbeam.remnants.mobs.mob;

import com.nightbeam.remnants.mobs.RemnantMobs;
import com.nightbeam.remnants.mobs.config.LevelsConfig;
import com.nightbeam.remnants.mobs.config.RanksConfig;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.config.TraitsConfig;
import com.nightbeam.remnants.mobs.content.Trait;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Turns level, rank and traits into attribute modifiers. Every modifier has a fixed id, so applying twice is safe. */
public final class Stats {
    public static final UUID LEVEL = uuid("level");
    public static final UUID RANK = uuid("rank");
    public static final UUID SWIFT = uuid("trait/swift");
    public static final UUID JUGGERNAUT = uuid("trait/juggernaut");
    public static final UUID BERSERK = uuid("trait/berserk");
    /** The single modifier 2.x (RPG Mob Leveling System) put on every scaled attribute. */
    public static final UUID LEGACY = UUID.fromString("8f3c2a10-6b4d-4e91-9c2f-1d7e8a5b3c40");
    private static final List<UUID> ALL = List.of(LEVEL, RANK, SWIFT, JUGGERNAUT, BERSERK, LEGACY);

    /** Fixed modifier ids (1.20.1 identifies modifiers by UUID). */
    private static UUID uuid(String path) {
        return UUID.nameUUIDFromBytes((RemnantMobs.MOD_ID + ":" + path).getBytes(StandardCharsets.UTF_8));
    }

    private static String name(UUID id) {
        return id.equals(LEVEL) ? "Remnants level" : id.equals(RANK) ? "Remnants rank" : "Remnants trait";
    }

    private Stats() {
    }

    /** Applies everything, keeping the mob's health at the same fraction of its (new) maximum. */
    public static void apply(Mob mob, MobData data) {
        float ratio = mob.getMaxHealth() > 0 ? Math.max(0.0F, Math.min(1.0F, mob.getHealth() / mob.getMaxHealth())) : 1.0F;
        clear(mob);
        LevelsConfig levels = RemnantsConfig.levels();
        if (!data.flag(MobData.BOSS) || levels.bossStats) {
            for (LevelsConfig.AttributeRule rule : levels.attributes) {
                Optional<Attribute> attribute = attribute(rule.attribute());
                if (attribute.isEmpty()) {
                    continue;
                }
                AttributeInstance instance = mob.getAttribute(attribute.get());
                if (instance == null) {
                    continue;
                }
                double amount = amount(rule, data.level());
                if (amount != 0) {
                    instance.addPermanentModifier(new AttributeModifier(LEVEL, name(LEVEL), amount, operation(rule)));
                }
            }
            if (data.ranked()) {
                RanksConfig.Stats rank = RemnantsConfig.ranks().stats(data.rank());
                add(mob, Attributes.MAX_HEALTH, RANK, rank.health() - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
                add(mob, Attributes.ATTACK_DAMAGE, RANK, rank.damage() - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
                add(mob, Attributes.ARMOR, RANK, rank.armor(), AttributeModifier.Operation.ADDITION);
            }
        }
        TraitsConfig traits = RemnantsConfig.traits();
        if (data.has(Trait.SWIFT)) {
            add(mob, Attributes.MOVEMENT_SPEED, SWIFT, traits.value(Trait.SWIFT, "speed_bonus"), AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        if (data.has(Trait.JUGGERNAUT)) {
            add(mob, Attributes.KNOCKBACK_RESISTANCE, JUGGERNAUT, 1.0, AttributeModifier.Operation.ADDITION);
            add(mob, Attributes.MAX_HEALTH, JUGGERNAUT, traits.value(Trait.JUGGERNAUT, "health_bonus"), AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(mob, Attributes.ARMOR, JUGGERNAUT, traits.value(Trait.JUGGERNAUT, "armor"), AttributeModifier.Operation.ADDITION);
            add(mob, Attributes.MOVEMENT_SPEED, JUGGERNAUT, -traits.value(Trait.JUGGERNAUT, "speed_penalty"), AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        if (mob.isAlive()) {
            mob.setHealth(Math.max(1.0F, ratio * mob.getMaxHealth()));
        }
        MobState state = RemnantMob.state(mob);
        state.berserk = false;
        state.wardUp = false;
        if (data.has(Trait.WARDED)) {
            float shield = (float) (traits.value(Trait.WARDED, "shield_fraction") * mob.getMaxHealth());
            mob.setAbsorptionAmount(shield);
            state.wardUp = shield > 0;
        }
    }

    /** Removes every modifier Remnants (or 2.x) added. */
    public static void clear(Mob mob) {
        for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
            AttributeInstance instance = mob.getAttributes().hasAttribute(attribute) ? mob.getAttribute(attribute) : null;
            if (instance != null) {
                for (UUID id : ALL) {
                    instance.removeModifier(id);
                }
            }
        }
    }

    /** Berserk's damage and speed, switched on and off as the mob's health crosses the threshold. */
    public static void berserk(Mob mob, boolean on) {
        AttributeInstance damage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (on) {
            TraitsConfig traits = RemnantsConfig.traits();
            if (damage != null) {
                damage.removeModifier(BERSERK);
                damage.addTransientModifier(new AttributeModifier(BERSERK, name(BERSERK), traits.value(Trait.BERSERK, "damage_bonus"),
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            if (speed != null) {
                speed.removeModifier(BERSERK);
                speed.addTransientModifier(new AttributeModifier(BERSERK, name(BERSERK), traits.value(Trait.BERSERK, "speed_bonus"),
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        } else {
            if (damage != null) {
                damage.removeModifier(BERSERK);
            }
            if (speed != null) {
                speed.removeModifier(BERSERK);
            }
        }
    }

    /**
     * How much harder this mob hits than its unleveled self, for damage that does not come from its attack damage
     * attribute (arrows, fireballs, explosions).
     */
    public static double damageFactor(Mob mob, MobData data) {
        double factor = 1.0;
        if (!data.flag(MobData.BOSS) || RemnantsConfig.levels().bossStats) {
            for (LevelsConfig.AttributeRule rule : RemnantsConfig.levels().attributes) {
                if (!normalize(rule.attribute()).equals("minecraft:attack_damage")) {
                    continue;
                }
                double amount = amount(rule, data.level());
                switch (operation(rule)) {
                    case ADDITION -> {
                        AttributeInstance instance = mob.getAttribute(Attributes.ATTACK_DAMAGE);
                        double base = instance != null ? Math.max(0.5, instance.getBaseValue()) : 2.0;
                        factor *= (base + amount) / base;
                    }
                    default -> factor *= 1.0 + amount;
                }
            }
            factor *= RemnantsConfig.ranks().stats(data.rank()).damage();
        }
        if (RemnantMob.state(mob).berserk) {
            factor *= 1.0 + RemnantsConfig.traits().value(Trait.BERSERK, "damage_bonus");
        }
        return Math.max(0.0, factor);
    }

    static double amount(LevelsConfig.AttributeRule rule, int level) {
        double max = Math.abs(rule.max());
        return switch (rule.mode()) {
            case "additive" -> clamp(rule.perLevel() * level, max);
            case "multiplicative" -> clamp(Math.pow(1.0 + rule.perLevel() / 100.0, level) - 1.0, max / 100.0);
            default -> clamp(rule.perLevel() * level, max) / 100.0;
        };
    }

    private static AttributeModifier.Operation operation(LevelsConfig.AttributeRule rule) {
        return switch (rule.mode()) {
            case "additive" -> AttributeModifier.Operation.ADDITION;
            case "multiplicative" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> AttributeModifier.Operation.MULTIPLY_BASE;
        };
    }

    private static double clamp(double value, double max) {
        return max <= 0 ? value : Math.max(-max, Math.min(max, value));
    }

    private static void add(Mob mob, Attribute attribute, UUID id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null && amount != 0) {
            instance.removeModifier(id);
            instance.addPermanentModifier(new AttributeModifier(id, name(id), amount, operation));
        }
    }

    /** Accepts {@code max_health}, {@code minecraft:max_health} and {@code minecraft:generic.max_health}. */
    public static String normalize(String id) {
        String value = id.trim().toLowerCase(Locale.ROOT);
        if (!value.contains(":")) {
            value = "minecraft:" + value;
        }
        int colon = value.indexOf(':');
        String path = value.substring(colon + 1);
        for (String prefix : new String[] {"generic.", "player.", "zombie."}) {
            if (path.startsWith(prefix)) {
                path = path.substring(prefix.length());
            }
        }
        return value.substring(0, colon) + ":" + path;
    }

    /** 1.20.1 names most attributes generic.*, so both spellings are tried. */
    public static Optional<Attribute> attribute(String id) {
        String normalized = normalize(id);
        int colon = normalized.indexOf(':');
        for (String candidate : new String[] {normalized, normalized.substring(0, colon + 1) + "generic." + normalized.substring(colon + 1),
                normalized.substring(0, colon + 1) + "zombie." + normalized.substring(colon + 1)}) {
            ResourceLocation parsed = ResourceLocation.tryParse(candidate);
            if (parsed != null && BuiltInRegistries.ATTRIBUTE.containsKey(parsed)) {
                return Optional.ofNullable(BuiltInRegistries.ATTRIBUTE.get(parsed));
            }
        }
        return Optional.empty();
    }
}
