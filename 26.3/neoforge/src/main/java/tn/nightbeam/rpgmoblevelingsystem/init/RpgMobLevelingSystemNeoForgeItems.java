package tn.nightbeam.rpgmoblevelingsystem.init;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.registry.ModItems;

public final class RpgMobLevelingSystemNeoForgeItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
    public static final DeferredHolder<Item, Item> CALENDER = ITEMS.register(
            ModItems.CALENDER_ID,
            name -> ModItems.createCalender(name));

    private RpgMobLevelingSystemNeoForgeItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
