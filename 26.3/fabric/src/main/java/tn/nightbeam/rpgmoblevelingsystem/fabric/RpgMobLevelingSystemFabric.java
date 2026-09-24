package tn.nightbeam.rpgmoblevelingsystem.fabric;

import net.fabricmc.api.ModInitializer;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.RpgMobLevelingSystemCommon;
import tn.nightbeam.rpgmoblevelingsystem.fabric.gameplay.FabricMobLeveling;
import tn.nightbeam.rpgmoblevelingsystem.init.RpgMobLevelingSystemFabricItems;

public final class RpgMobLevelingSystemFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Constants.LOG.info("Bootstrapping {} on Fabric", Constants.MOD_NAME);
        RpgMobLevelingSystemCommon.init();
        RpgMobLevelingSystemFabricItems.registerItems();
        FabricMobLeveling.register();
    }
}
