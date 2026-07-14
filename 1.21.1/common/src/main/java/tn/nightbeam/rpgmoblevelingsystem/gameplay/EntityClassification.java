package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;

public final class EntityClassification {
    private EntityClassification() {
    }

    public static boolean shouldSkip(Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity instanceof Player) {
            return true;
        }
        if (isBanned(entity)) {
            return true;
        }
        if (ModConfig.mobs().hostileOnly && !isHostile(living)) {
            return true;
        }
        return false;
    }

    public static boolean isBanned(Entity entity) {
        String id = entityTypeId(entity);
        if (ModConfig.mobs().banned.contains(id)) {
            return true;
        }
        String namespace = id.contains(":") ? id.substring(0, id.indexOf(':')) : id;
        return ModConfig.mobs().bannedNamespaces.contains(namespace);
    }

    public static boolean isHostile(LivingEntity entity) {
        return entity instanceof Enemy || entity instanceof Monster;
    }

    public static boolean isBoss(Entity entity) {
        String id = entityTypeId(entity);
        return ModConfig.mobs().bossLevelLocks.containsKey(id)
                || entity.getTags().contains("rpgmoblevelingsystem:boss");
    }

    public static String entityTypeId(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }
}
