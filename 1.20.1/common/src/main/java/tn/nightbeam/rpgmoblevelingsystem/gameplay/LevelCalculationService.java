package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;
import tn.nightbeam.rpgmoblevelingsystem.util.ScalingMath;

import java.util.List;
import java.util.UUID;

public final class LevelCalculationService {
    private LevelCalculationService() {
    }

    public static double calculateLevel(Level world, Entity entity) {
        return calculate(world, entity).finalLevel;
    }

    public static LevelDebugBreakdown calculate(Level world, Entity entity) {
        String entityId = EntityClassification.entityTypeId(entity);
        double locked = resolveLockedLevel(entityId);
        if (locked >= 0) {
            return new LevelDebugBreakdown(entityId, locked, 0, 0, 0, locked, locked, locked,
                    ModConfig.scale().scaleType, ModConfig.scale().playerScaleMode, RasCompatibilityLayer.isRasLoaded());
        }

        Double stored = MobLevelStorage.getStoredLevel(entity);
        if (stored != null) {
            return new LevelDebugBreakdown(entityId, stored, 0, 0, 0, -1, stored, stored,
                    ModConfig.scale().scaleType, ModConfig.scale().playerScaleMode, RasCompatibilityLayer.isRasLoaded());
        }

        double baseLevel = getBaseLevel(world);
        double spawnDifference = calculateSpawnDifference(world, entity);
        double playerContribution = calculatePlayerContribution(world, entity);
        double structureMin = resolveStructureMinLevel(world, entity);
        double raw = baseLevel + (spawnDifference + playerContribution) * ModConfig.scale().scaleFactor;
        raw = Math.max(raw, structureMin);
        double rounded = applyRounding(raw);
        double maxLevel = getMaxLevel(world);
        double finalLevel = Math.min(rounded, maxLevel);

        return new LevelDebugBreakdown(entityId, baseLevel, spawnDifference, playerContribution, structureMin, -1,
                raw, finalLevel, ModConfig.scale().scaleType, ModConfig.scale().playerScaleMode,
                RasCompatibilityLayer.isRasLoaded());
    }

    public static double resolveLockedLevel(String entityId) {
        Double boss = ModConfig.mobs().bossLevelLocks.get(entityId);
        if (boss != null) {
            return boss;
        }
        Double locked = ModConfig.mobs().lockedMobs.get(entityId);
        return locked != null ? locked : -1;
    }

    public static double calculateSpawnDifference(Level world, Entity entity) {
        BlockPos spawnPos = world.getSharedSpawnPos();
        String scaleType = ModConfig.scale().scaleType;
        double scaleDistance = Math.max(1.0, ModConfig.scale().scaleDistance);

        if ("vertical".equals(scaleType)) {
            double dy = Math.abs(entity.getY() - spawnPos.getY());
            return applyRounding(dy / scaleDistance);
        }
        if ("horizontal".equals(scaleType)) {
            double dx = entity.getX() - spawnPos.getX();
            double dz = entity.getZ() - spawnPos.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            return applyRounding(horizontal / scaleDistance);
        }
        if ("time".equals(scaleType)) {
            double days = world.getLevelData().getGameTime() / 24000.0;
            return applyRounding(days / Math.max(1.0, ModConfig.scale().dayFactor));
        }
        if ("random".equals(scaleType)) {
            int min = (int) Math.floor(getBaseLevel(world));
            int max = (int) Math.floor(getMaxLevel(world));
            if (max < min) {
                max = min;
            }
            int range = Math.max(1, (max - min) + 1);
            UUID id = entity.getUUID();
            return min + (Math.abs(id.hashCode()) % range);
        }

        double dx = entity.getX() - spawnPos.getX();
        double dy = entity.getY() - spawnPos.getY();
        double dz = entity.getZ() - spawnPos.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return applyRounding(distance / scaleDistance);
    }

    public static double calculatePlayerContribution(Level world, Entity entity) {
        String mode = ModConfig.scale().playerScaleMode;
        if (mode == null || "none".equalsIgnoreCase(mode) || !RasCompatibilityLayer.isRasLoaded()) {
            return 0;
        }
        if (!(world instanceof ServerLevel serverLevel)) {
            return 0;
        }

        double radius = ModConfig.scale().playerScaleRadius;
        AABB box = entity.getBoundingBox().inflate(radius);
        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, box, Player::isAlive);
        if (players.isEmpty()) {
            return 0;
        }

        return switch (mode.toLowerCase()) {
            case "nearest" -> {
                Player nearest = null;
                double best = Double.MAX_VALUE;
                for (Player player : players) {
                    double dist = player.distanceToSqr(entity);
                    if (dist < best) {
                        best = dist;
                        nearest = player;
                    }
                }
                yield nearest == null ? 0 : RasCompatibilityLayer.getPlayerLevel(nearest) * ModConfig.scale().playerScaleFactor;
            }
            case "highest_nearby" -> {
                int highest = 0;
                for (Player player : players) {
                    highest = Math.max(highest, RasCompatibilityLayer.getPlayerLevel(player));
                }
                yield highest * ModConfig.scale().playerScaleFactor;
            }
            case "average_nearby" -> {
                int sum = 0;
                for (Player player : players) {
                    sum += RasCompatibilityLayer.getPlayerLevel(player);
                }
                yield (sum / (double) players.size()) * ModConfig.scale().playerScaleFactor;
            }
            default -> 0;
        };
    }

    public static double resolveStructureMinLevel(Level world, Entity entity) {
        if (!(world instanceof ServerLevel serverLevel) || ModConfig.mobs().structureMinLevels.isEmpty()) {
            return 0;
        }
        BlockPos pos = entity.blockPosition();
        double min = 0;
        var registry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (var entry : ModConfig.mobs().structureMinLevels.entrySet()) {
            ResourceLocation structureId = ResourceLocation.tryParse(entry.getKey());
            if (structureId == null) {
                continue;
            }
            Structure structure = registry.get(structureId);
            if (structure == null) {
                continue;
            }
            StructureStart start = serverLevel.structureManager().getStructureAt(pos, structure);
            if (start != null && start.isValid()) {
                min = Math.max(min, entry.getValue());
            }
        }
        return min;
    }

    public static double applyRounding(double value) {
        return ScalingMath.applyRounding(value, ModConfig.scale().roundingMode);
    }

    public static double getBaseLevel(Level world) {
        double fallback = ModConfig.scale().baseLevel;
        String dimName = world.dimension().location().toString();
        for (ModConfig.DimensionRange range : ModConfig.dimensions().dimensions) {
            if (dimName.contains(range.dimensionId)) {
                return range.min;
            }
        }
        return fallback;
    }

    public static double getMaxLevel(Level world) {
        String dimName = world.dimension().location().toString();
        for (ModConfig.DimensionRange range : ModConfig.dimensions().dimensions) {
            if (dimName.contains(range.dimensionId)) {
                return range.max;
            }
        }
        return ModConfig.scale().baseLevel;
    }
}
