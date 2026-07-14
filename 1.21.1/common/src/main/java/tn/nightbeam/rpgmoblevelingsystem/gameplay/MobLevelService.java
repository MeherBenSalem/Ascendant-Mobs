package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;

import net.minecraft.util.RandomSource;

public final class MobLevelService {
    private MobLevelService() {
    }

    public static void onEntityLoaded(Level level, Entity entity) {
        if (level.isClientSide() || !(level instanceof ServerLevel)) {
            return;
        }
        applyMobLevel(level, entity, false);
    }

    public static void recalculate(Level level, Entity entity) {
        if (level.isClientSide() || !(entity instanceof LivingEntity living)) {
            return;
        }
        AttributeScalingService.clear(living);
        applyMobLevel(level, entity, true);
    }

    public static int adjustDroppedExperience(LivingEntity living, int originalXp) {
        Double storedLevel = MobLevelStorage.getStoredLevel(living);
        double level = storedLevel == null ? 0 : storedLevel;
        if (level <= 0) {
            int preview = (int) LevelCalculationService.calculateLevel(living.level(), living);
            level = preview;
        }
        if (level <= 0) {
            return Math.max(originalXp, 1);
        }
        double multiplier = Math.max(1.0, ModConfig.scale().xpModifier);
        return (int) Math.floor(originalXp + (level * multiplier));
    }

    public static Integer getDisplayLevel(Level world, Entity entity) {
        if (EntityClassification.shouldSkip(entity)) {
            return null;
        }
        Double stored = MobLevelStorage.getStoredLevel(entity);
        if (stored != null) {
            return (int) Math.floor(stored);
        }
        double locked = LevelCalculationService.resolveLockedLevel(EntityClassification.entityTypeId(entity));
        if (locked >= 0) {
            return (int) Math.floor(locked);
        }
        return (int) Math.floor(LevelCalculationService.calculateLevel(world, entity));
    }

    public static void onMobDeath(LivingEntity living) {
        if (living.level().isClientSide() || !ModConfig.lootByLevel().enabled) {
            return;
        }
        Double stored = MobLevelStorage.getStoredLevel(living);
        int level = stored == null ? 0 : (int) Math.floor(stored);
        if (level <= 0) {
            return;
        }
        RandomSource random = living.getRandom();
        Vec3 pos = living.position();
        for (ModConfig.LootBand band : ModConfig.lootByLevel().bands) {
            if (level < band.minLevel || level > band.maxLevel) {
                continue;
            }
            if (random.nextDouble() > band.chance) {
                continue;
            }
            var itemKey = net.minecraft.resources.ResourceLocation.tryParse(band.itemId);
            if (itemKey == null) {
                continue;
            }
            var item = BuiltInRegistries.ITEM.get(itemKey);
            if (item == null) {
                continue;
            }
            ItemStack stack = new ItemStack(item, Math.max(1, band.count));
            living.level().addFreshEntity(new ItemEntity(living.level(), pos.x, pos.y, pos.z, stack));
        }
    }

    private static void applyMobLevel(Level world, Entity entity, boolean force) {
        if (!(entity instanceof LivingEntity living) || entity instanceof Player) {
            return;
        }
        if (!force && (MobLevelStorage.hasBeenLeveled(entity) || EntityClassification.shouldSkip(entity))) {
            return;
        }

        double level = LevelCalculationService.calculateLevel(world, entity);
        AttributeScalingService.apply(entity, living, level);
        living.setHealth(living.getMaxHealth());
        MobLevelStorage.storeLevel(entity, level);
    }
}
