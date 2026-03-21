package tn.nightbeam.rpgmoblevelingsystem.platform;

import net.fabricmc.loader.api.FabricLoader;
import tn.nightbeam.rpgmoblevelingsystem.platform.services.IPlatformHelper;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
