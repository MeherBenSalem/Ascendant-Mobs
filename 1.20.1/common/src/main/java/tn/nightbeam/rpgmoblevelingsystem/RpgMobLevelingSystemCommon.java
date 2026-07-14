package tn.nightbeam.rpgmoblevelingsystem;

import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;
import tn.nightbeam.rpgmoblevelingsystem.platform.Services;

public final class RpgMobLevelingSystemCommon {
    private RpgMobLevelingSystemCommon() {
    }

    public static void init() {
        ModConfig.ensureDefaults();
        tn.nightbeam.rpgmoblevelingsystem.compat.JadeCompat.registerIfPresent();
        Constants.LOG.info("Initializing {} on {} ({})", Constants.MOD_NAME, Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
    }
}
