package tn.nightbeam.rpgmoblevelingsystem.util;

public final class ScalingMath {
    private ScalingMath() {
    }

    public static double applyRounding(double value, String mode) {
        if (mode == null) {
            return Math.floor(value);
        }
        return switch (mode.toLowerCase()) {
            case "ceil" -> Math.ceil(value);
            case "round" -> Math.round(value);
            default -> Math.floor(value);
        };
    }

    public static double horizontalDistance(double dx, double dz) {
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double threeDimensionalDistance(double dx, double dy, double dz) {
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Scales non-melee damage by the same ratio melee gets from the level attack-damage modifier.
     */
    public static float applyLevelDamageRatio(float amount, double attackWithLevel, double levelBonus) {
        if (amount <= 0.0f || levelBonus == 0.0) {
            return amount;
        }
        double withoutLevel = attackWithLevel - levelBonus;
        if (withoutLevel <= 1.0E-4) {
            withoutLevel = 1.0E-4;
        }
        return amount * (float) (attackWithLevel / withoutLevel);
    }
}
