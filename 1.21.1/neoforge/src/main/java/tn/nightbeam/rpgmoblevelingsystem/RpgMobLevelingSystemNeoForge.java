package tn.nightbeam.rpgmoblevelingsystem;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelingLogic;
import tn.nightbeam.rpgmoblevelingsystem.init.RpgMobLevelingSystemNeoForgeItems;

@Mod(Constants.MOD_ID)
public final class RpgMobLevelingSystemNeoForge {
    public RpgMobLevelingSystemNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onBuildCreativeTab);

        RpgMobLevelingSystemNeoForgeItems.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(this::onLivingExperienceDrop);

        Constants.LOG.info("Bootstrapping {} on NeoForge", Constants.MOD_NAME);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        RpgMobLevelingSystemCommon.init();
    }

    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(RpgMobLevelingSystemNeoForgeItems.CALENDER.get());
        }
    }

    private void onEntityJoinLevel(EntityJoinLevelEvent event) {
        MobLevelingLogic.onEntityLoaded(event.getLevel(), event.getEntity());
    }

    private void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        int updated = MobLevelingLogic.adjustDroppedExperience(event.getEntity(), event.getDroppedExperience());
        event.setDroppedExperience(updated);
    }
}
