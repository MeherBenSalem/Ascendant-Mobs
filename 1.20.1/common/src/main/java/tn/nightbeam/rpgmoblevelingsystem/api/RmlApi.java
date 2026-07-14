package tn.nightbeam.rpgmoblevelingsystem.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.LevelCalculationService;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.LevelDebugBreakdown;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelService;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelStorage;

public final class RmlApi {
    private RmlApi() {
    }

    public static int getLevel(Entity entity) {
        Double stored = MobLevelStorage.getStoredLevel(entity);
        if (stored != null) {
            return (int) Math.floor(stored);
        }
        return (int) Math.floor(LevelCalculationService.calculateLevel(entity.level(), entity));
    }

    public static void recalculate(Entity entity) {
        MobLevelService.recalculate(entity.level(), entity);
    }

    public static LevelDebugBreakdown getDebugBreakdown(Entity entity) {
        return LevelCalculationService.calculate(entity.level(), entity);
    }

    public static Integer getDisplayLevel(Level world, Entity entity) {
        return MobLevelService.getDisplayLevel(world, entity);
    }
}
