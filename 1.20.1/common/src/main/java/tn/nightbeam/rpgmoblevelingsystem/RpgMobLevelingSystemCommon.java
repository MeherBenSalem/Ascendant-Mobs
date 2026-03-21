package tn.nightbeam.rpgmoblevelingsystem;

import tn.nightbeam.rpgmoblevelingsystem.platform.Services;

public final class RpgMobLevelingSystemCommon {
    private RpgMobLevelingSystemCommon() {
    }

    public static void init() {
        Constants.LOG.info("Initializing {} on {} ({})", Constants.MOD_NAME, Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
    }
}
