package com.nightbeam.remnants.mobs.compat;

import com.nightbeam.remnants.mobs.RemnantMobs;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Locale;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * RPG Attribute System, reached by reflection so Remnants runs without it. Reads player levels through
 * {@code RasApi.getLevel} and grants Valor through {@code LevelingService.addXp} (or the {@code /ras xp} command).
 */
public final class RasBridge {
    public static final String MOD_ID = "rpg_attribute_system";
    private static boolean resolved;
    private static @Nullable MethodHandle getLevel;
    private static @Nullable MethodHandle addXp;

    private RasBridge() {
    }

    public static boolean present() {
        resolve();
        return getLevel != null;
    }

    private static synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        if (RemnantMobs.platform() == null || !RemnantMobs.platform().isModLoaded(MOD_ID)) {
            return;
        }
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        try {
            Class<?> api = Class.forName("tn.nightbeam.ras.api.RasApi");
            getLevel = lookup.findStatic(api, "getLevel", MethodType.methodType(int.class, Player.class));
        } catch (ReflectiveOperationException | LinkageError e) {
            RemnantMobs.LOG.warn("RPG Attribute System is installed but its API could not be found ({}); playing without it", e.toString());
            return;
        }
        try {
            Class<?> leveling = Class.forName("tn.nightbeam.ras.procedures.LevelingService");
            addXp = lookup.findStatic(leveling, "addXp", MethodType.methodType(void.class, Entity.class, double.class));
        } catch (ReflectiveOperationException | LinkageError e) {
            RemnantMobs.LOG.info("RPG Attribute System: granting Valor through /ras xp ({})", e.toString());
        }
        RemnantMobs.LOG.info("RPG Attribute System found: mob levels, plates and rewards use RAS levels and Valor");
    }

    /** The player's RAS level, or -1 without RAS. */
    public static int level(Player player) {
        resolve();
        if (getLevel == null || !(player instanceof ServerPlayer)) {
            return -1;
        }
        try {
            return (int) getLevel.invokeExact(player);
        } catch (Throwable t) {
            return -1;
        }
    }

    /** Grants Valor (RAS experience). Does nothing without RAS. */
    public static void addValor(ServerPlayer player, double amount) {
        resolve();
        if (getLevel == null || !(amount > 0) || !Double.isFinite(amount)) {
            return;
        }
        if (addXp != null) {
            try {
                addXp.invokeExact((Entity) player, amount);
                return;
            } catch (Throwable t) {
                RemnantMobs.LOG.debug("RAS addXp failed, falling back to /ras xp", t);
            }
        }
        var server = player.level().getServer();
        String command = String.format(Locale.ROOT, "ras xp %.2f %s", amount, player.getGameProfile().name());
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
    }
}
