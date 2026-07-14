package tn.nightbeam.rpgmoblevelingsystem.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.RpgMobLevelingSystemCommon;
import tn.nightbeam.rpgmoblevelingsystem.fabric.client.FabricMobLevelOverlay;
import tn.nightbeam.rpgmoblevelingsystem.fabric.gameplay.FabricMobLeveling;
import tn.nightbeam.rpgmoblevelingsystem.init.RpgMobLevelingSystemFabricItems;

public class RpgMobLevelingSystemFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Constants.LOG.info("Bootstrapping RPG Mob Leveling System on Fabric");
        RpgMobLevelingSystemCommon.init();
        RpgMobLevelingSystemFabricItems.registerItems();
        FabricMobLeveling.register();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            FabricMobLevelOverlay.register();
        }
    }
}
