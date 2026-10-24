package com.nightbeam.remnants.mobs.mob;

import com.nightbeam.remnants.mobs.compat.RasBridge;
import com.nightbeam.remnants.mobs.config.LevelsConfig;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.event.HuntersMoon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.Structure;
import javax.annotation.Nullable;

/**
 * The threat of a place: the level mobs spawning there get, before their personal spread. Each part is kept so
 * {@code /remnants threat} and the Hunter's Lens can explain it.
 */
public record Threat(int min, int max, double distance, double days, double depth, int moon, int structure,
                     @Nullable String structureId, int players, double playerLevel, double weight, double raw, int level) {

    public static Threat at(ServerLevel level, BlockPos pos, boolean withPlayers) {
        LevelsConfig c = RemnantsConfig.levels();
        String dimension = level.dimension().location().toString();
        LevelsConfig.Band band = c.band(dimension);

        BlockPos spawn = level.getSharedSpawnPos();
        double scale = level.dimensionType().coordinateScale();
        double dx = pos.getX() * scale - spawn.getX();
        double dz = pos.getZ() * scale - spawn.getZ();
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD) && scale == 1.0) {
            // Dimensions without a spawn of their own measure from their origin (the End's main island).
            dx = pos.getX();
            dz = pos.getZ();
        }
        double distance = Math.sqrt(dx * dx + dz * dz) / c.blocksPerLevel * band.distanceScale();
        double days = Math.min(WorldTime.day(level) / c.daysPerLevel, c.maxDayLevels);
        double depth = level.dimensionType().hasSkyLight() && pos.getY() < c.depthStartY
                ? Math.min((c.depthStartY - pos.getY()) / c.blocksPerDepthLevel, c.maxDepthLevels) : 0;
        int moon = HuntersMoon.affects(level) ? c.moonLevelBonus : 0;
        double raw = band.min() + distance + days + depth + moon;

        int structure = 0;
        String structureId = null;
        for (Map.Entry<String, Integer> entry : sortedStructures(c)) {
            if (entry.getValue() <= structure) {
                break;
            }
            ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
            if (id == null) {
                continue;
            }
            ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, id);
            if (level.structureManager().getStructureWithPieceAt(pos, key).isValid()) {
                structure = entry.getValue();
                structureId = entry.getKey();
            }
        }
        raw = Math.max(raw, structure);

        int players = 0;
        double playerLevel = 0;
        double weight = 0;
        if (withPlayers && !"none".equals(c.playerSource) && c.playerWeight > 0) {
            List<Integer> levels = new ArrayList<>();
            double best = Double.MAX_VALUE;
            int nearest = 0;
            double radiusSq = c.playerRadius * c.playerRadius;
            for (ServerPlayer player : level.players()) {
                if (player.isSpectator() || !player.isAlive()) {
                    continue;
                }
                double distSq = player.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                if (distSq > radiusSq) {
                    continue;
                }
                int value = hunterLevel(player);
                if (value < 0) {
                    continue;
                }
                levels.add(value);
                if (distSq < best) {
                    best = distSq;
                    nearest = value;
                }
            }
            if (!levels.isEmpty()) {
                players = levels.size();
                playerLevel = switch (c.playerMode) {
                    case "nearest" -> nearest;
                    case "highest" -> levels.stream().mapToInt(Integer::intValue).max().orElse(0);
                    default -> levels.stream().mapToInt(Integer::intValue).average().orElse(0);
                };
                weight = c.playerWeight;
                raw = raw + (playerLevel - raw) * weight;
            }
        }
        int result = Mth.clamp((int) Math.floor(raw), band.min(), band.max());
        return new Threat(band.min(), band.max(), distance, days, depth, moon, structure, structureId, players, playerLevel, weight, raw, result);
    }

    /**
     * The level a player brings to the fight: their RPG Attribute System level, or their experience level when
     * {@code players.source} is "vanilla". -1 when player levels are not used.
     */
    public static int hunterLevel(ServerPlayer player) {
        String source = RemnantsConfig.levels().playerSource;
        if ("vanilla".equals(source)) {
            return player.experienceLevel;
        }
        if ("ras".equals(source)) {
            return RasBridge.level(player);
        }
        return -1;
    }

    private static List<Map.Entry<String, Integer>> sortedStructures(LevelsConfig c) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(c.structures.entrySet());
        entries.sort(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()));
        return entries;
    }
}
