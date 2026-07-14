package tn.nightbeam.rpgmoblevelingsystem.forge;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.command.RmlCommands;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.AscendantService;
import tn.nightbeam.rpgmoblevelingsystem.gameplay.MobLevelService;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public final class ForgeGameplayEvents {
    private ForgeGameplayEvents() {
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        int updated = MobLevelService.adjustDroppedExperience(event.getEntity(), event.getDroppedExperience());
        event.setDroppedExperience(updated);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        MobLevelService.onMobDeath(event.getEntity());
        AscendantService.onAscendantDeath(event.getEntity());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        RmlCommands.register(event.getDispatcher());
    }
}
