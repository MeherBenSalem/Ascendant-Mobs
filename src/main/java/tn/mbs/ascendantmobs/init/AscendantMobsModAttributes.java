/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.mbs.ascendantmobs.init;

import tn.mbs.ascendantmobs.AscendantMobsMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.attributes.Attribute;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AscendantMobsModAttributes {
	public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, AscendantMobsMod.MODID);
	public static final RegistryObject<Attribute> AMLEVEL_ATTRIBUTE = REGISTRY.register("amlevel_attribute", () -> new RangedAttribute("attribute.ascendant_mobs.amlevel_attribute", 0, 0, 9999).setSyncable(true));
	public static final RegistryObject<Attribute> AM_GOT_LEVEL = REGISTRY.register("am_got_level", () -> new RangedAttribute("attribute.ascendant_mobs.am_got_level", 0, 0, 1).setSyncable(true));

	@SubscribeEvent
	public static void addAttributes(EntityAttributeModificationEvent event) {
		event.getTypes().forEach(entity -> event.add(entity, AMLEVEL_ATTRIBUTE.get()));
		event.getTypes().forEach(entity -> event.add(entity, AM_GOT_LEVEL.get()));
	}

	@Mod.EventBusSubscriber
	public static class PlayerAttributesSync {
		@SubscribeEvent
		public static void playerClone(PlayerEvent.Clone event) {
			Player oldPlayer = event.getOriginal();
			Player newPlayer = event.getEntity();
			newPlayer.getAttribute(AMLEVEL_ATTRIBUTE.get()).setBaseValue(oldPlayer.getAttribute(AMLEVEL_ATTRIBUTE.get()).getBaseValue());
			newPlayer.getAttribute(AM_GOT_LEVEL.get()).setBaseValue(oldPlayer.getAttribute(AM_GOT_LEVEL.get()).getBaseValue());
		}
	}
}