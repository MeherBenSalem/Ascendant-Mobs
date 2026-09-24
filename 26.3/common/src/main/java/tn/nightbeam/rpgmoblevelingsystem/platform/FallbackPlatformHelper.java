package tn.nightbeam.rpgmoblevelingsystem.platform;

import tn.nightbeam.rpgmoblevelingsystem.platform.services.IPlatformHelper;

final class FallbackPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Unknown";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return false;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return false;
    }
}
