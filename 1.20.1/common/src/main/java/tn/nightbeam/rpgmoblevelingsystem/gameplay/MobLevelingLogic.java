package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Backward-compatible facade delegating to {@link MobLevelService}. */
public final class MobLevelingLogic {
    private MobLevelingLogic() {
    }

    public static void onEntityLoaded(Level level, Entity entity) {
        MobLevelService.onEntityLoaded(level, entity);
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            AscendantService.tryApplyAscendant(living);
        }
    }

    public static int adjustDroppedExperience(LivingEntity living, int originalXp) {
        return MobLevelService.adjustDroppedExperience(living, originalXp);
    }

    public static Integer getDisplayLevel(Level world, Entity entity) {
        return MobLevelService.getDisplayLevel(world, entity);
    }

    public static void sendCalendarMessage(Level world, net.minecraft.world.entity.player.Player player) {
        if (world.isClientSide()) {
            return;
        }
        String msg = "Current day: " + (int) Math.floor(world.getLevelData().getGameTime() / 24000.0)
                + " | Current Dimension: " + world.dimension().location()
                + " | Current Min-Max Levels: [" + (int) LevelCalculationService.getBaseLevel(world)
                + " - " + (int) LevelCalculationService.getMaxLevel(world) + "]";
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(msg), false);
    }
}
