package tn.nightbeam.rpgmoblevelingsystem.gameplay;

import net.minecraft.world.entity.player.Player;

/**
 * Reflection bridge to RPG Attribute System so mob-levels does not duplicate RAS formulas.
 */
public final class RasCompatibilityLayer {
    private static final String RAS_API = "tn.nightbeam.ras.api.RasApi";
    private static final String COMBAT_SNAPSHOT = "tn.nightbeam.ras.api.CombatSnapshot";

    private RasCompatibilityLayer() {
    }

    public static boolean isRasLoaded() {
        return tn.nightbeam.rpgmoblevelingsystem.platform.Services.PLATFORM.isModLoaded("rpg_attribute_system");
    }

    public static int getPlayerLevel(Player player) {
        try {
            Class<?> api = Class.forName(RAS_API);
            return (int) api.getMethod("getLevel", Player.class).invoke(null, player);
        } catch (Exception ignored) {
            return 0;
        }
    }

    public static PlayerCombatSnapshot getCombatSnapshot(Player player) {
        try {
            Class<?> api = Class.forName(RAS_API);
            Object snapshot = api.getMethod("getCombatSnapshot", Player.class).invoke(null, player);
            if (snapshot == null) {
                return PlayerCombatSnapshot.EMPTY;
            }
            Class<?> snapClass = Class.forName(COMBAT_SNAPSHOT);
            return new PlayerCombatSnapshot(
                    (int) snapClass.getMethod("rpgLevel").invoke(snapshot),
                    (double) snapClass.getMethod("maxHealth").invoke(snapshot),
                    (double) snapClass.getMethod("attackDamage").invoke(snapshot),
                    (double) snapClass.getMethod("armor").invoke(snapshot),
                    (double) snapClass.getMethod("armorToughness").invoke(snapshot),
                    (double) snapClass.getMethod("movementSpeed").invoke(snapshot)
            );
        } catch (Exception ignored) {
            return PlayerCombatSnapshot.EMPTY;
        }
    }

    public record PlayerCombatSnapshot(
            int rpgLevel,
            double maxHealth,
            double attackDamage,
            double armor,
            double armorToughness,
            double movementSpeed
    ) {
        public static final PlayerCombatSnapshot EMPTY = new PlayerCombatSnapshot(0, 20.0, 1.0, 0.0, 0.0, 0.1);
    }
}
