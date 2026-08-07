package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;
import tn.nightbeam.rpgmoblevelingsystem.platform.Services;

import java.util.List;

public final class MotpCompatibilityLayer {
    private static final String[] MOD_IDS = {"memory_of_the_past", "memories_of_the_past"};
    private static final List<ResourceLocation> LEVEL_ATTRIBUTES = List.of(
            new ResourceLocation("memory_of_the_past", "motp_level"),
            new ResourceLocation("memories_of_the_past", "motp_level")
    );
    private static final double DEFAULT_SEARCH_RADIUS = 128;
    private static boolean warnedMissingMod;

    private MotpCompatibilityLayer() {
    }

    public static boolean isMotpLoaded() {
        for (String modId : MOD_IDS) {
            if (Services.platform().isModLoaded(modId)) {
                return true;
            }
        }
        return false;
    }

    public static double getNearestPlayerLevel(Level world, Entity entity) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return 0;
        }
        double radius = ModConfig.scale().motpSearchRadius > 0
                ? ModConfig.scale().motpSearchRadius
                : DEFAULT_SEARCH_RADIUS;
        double radiusSqr = radius * radius;

        Player nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (Player player : serverLevel.players()) {
            if (!player.isAlive()) {
                continue;
            }
            double distance = player.distanceToSqr(entity);
            if (distance > radiusSqr) {
                continue;
            }
            double level = getPlayerMotpLevel(player);
            if (level <= 0) {
                continue;
            }
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = player;
            }
        }
        return nearest == null ? 0 : getPlayerMotpLevel(nearest);
    }

    public static void warnIfMissing() {
        if (!warnedMissingMod) {
            warnedMissingMod = true;
            Constants.LOG.warn("scale_type is MOTP but Memories of the Past is not installed; using distance scaling instead");
        }
    }

    private static double getPlayerMotpLevel(Player player) {
        for (ResourceLocation key : LEVEL_ATTRIBUTES) {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(key);
            if (attribute == null) {
                continue;
            }
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                return instance.getValue();
            }
        }
        return 0;
    }
}
