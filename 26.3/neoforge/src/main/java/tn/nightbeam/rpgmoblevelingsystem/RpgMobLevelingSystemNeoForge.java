package tn.nightbeam.rpgmoblevelingsystem;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import tn.nightbeam.rpgmoblevelingsystem.command.RmlCommands;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.AscendantService;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelService;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelingLogic;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.OutgoingDamageScaling;
import tn.nightbeam.rpgmoblevelingsystem.init.RpgMobLevelingSystemNeoForgeItems;

@Mod(Constants.MOD_ID)
public final class RpgMobLevelingSystemNeoForge {
    public RpgMobLevelingSystemNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onBuildCreativeTab);

        RpgMobLevelingSystemNeoForgeItems.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(this::onLivingIncomingDamage);
        NeoForge.EVENT_BUS.addListener(this::onLivingExperienceDrop);
        NeoForge.EVENT_BUS.addListener(this::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        Constants.LOG.info("Bootstrapping {} on NeoForge", Constants.MOD_NAME);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        RpgMobLevelingSystemCommon.init();
    }

    private static final ResourceKey<CreativeModeTab> REDSTONE_BLOCKS = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("redstone_blocks"));

    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == REDSTONE_BLOCKS) {
            event.accept(RpgMobLevelingSystemNeoForgeItems.CALENDER.get());
        }
    }

    private void onEntityJoinLevel(EntityJoinLevelEvent event) {
        MobLevelingLogic.onEntityLoaded(event.getLevel(), event.getEntity());
    }

    private void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        float scaled = OutgoingDamageScaling.scale(event.getSource(), event.getAmount());
        event.setAmount(scaled);
    }

    private void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        int updated = MobLevelService.adjustDroppedExperience(event.getEntity(), event.getDroppedExperience());
        event.setDroppedExperience(updated);
    }

    private void onLivingDeath(LivingDeathEvent event) {
        MobLevelService.onMobDeath(event.getEntity());
        AscendantService.onAscendantDeath(event.getEntity());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        RmlCommands.register(event.getDispatcher());
    }
}
