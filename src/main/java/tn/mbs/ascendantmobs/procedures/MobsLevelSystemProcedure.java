package tn.mbs.ascendantmobs.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.ascendantmobs.init.AscendantMobsModAttributes;
import tn.mbs.ascendantmobs.configuration.MobsLevelsMainConfigConfiguration;
import tn.mbs.ascendantmobs.AscendantMobsMod;

import org.checkerframework.checker.units.qual.s;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class MobsLevelSystemProcedure {
	@SubscribeEvent
	public static void onEntitySpawned(EntityJoinLevelEvent event) {
		execute(event, event.getLevel(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		boolean show = false;
		double maxLevel = 0;
		double level = 0;
		double maxValue = 0;
		double lockedLevel = 0;
		double baseLevel = 0;
		double value = 0;
		double currentValue = 0;
		double count = 0;
		String attribute = "";
		String mobRegistry = "";
		String line = "";
		if (entity instanceof LivingEntity && !(entity instanceof ServerPlayer || entity instanceof Player)) {
			if ((entity instanceof LivingEntity _livingEntity3 && _livingEntity3.getAttributes().hasAttribute(AscendantMobsModAttributes.AM_GOT_LEVEL.get())
					? _livingEntity3.getAttribute(AscendantMobsModAttributes.AM_GOT_LEVEL.get()).getValue()
					: 0) > 0 || CanGetLevelProcedureProcedure.execute(entity)) {
				return;
			}
			lockedLevel = LockedMobsProcedureProcedure.execute(entity);
			if (lockedLevel != -1) {
				level = lockedLevel;
			} else if ((entity instanceof LivingEntity _livingEntity4 && _livingEntity4.getAttributes().hasAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get())
					? _livingEntity4.getAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get()).getValue()
					: 0) > 0) {
				level = entity instanceof LivingEntity _livingEntity5 && _livingEntity5.getAttributes().hasAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get())
						? _livingEntity5.getAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get()).getValue()
						: 0;
			} else {
				baseLevel = GetBaseLevelProcedure.execute(world);
				maxLevel = GetMaxLevelProcedure.execute(world);
				level = Math.floor(baseLevel + CalculateSpawnDifferenceProcedure.execute(world, x, y, z, entity) * (double) MobsLevelsMainConfigConfiguration.SCALE_FACTOR.get());
				if (level >= maxLevel) {
					level = maxLevel;
				}
			}
			for (int index0 = 0; index0 < (int) JaumlConfigLib.getArrayLength("ascendant_mobs", "attributes_settings", "attributes_list"); index0++) {
				line = JaumlConfigLib.getArrayElement("ascendant_mobs", "attributes_settings", "attributes_list", ((int) count));
				if (line.contains("[attribute]") && line.contains("[attributeEnd]") && line.contains("[value]") && line.contains("[valueEnd]") && line.contains("[max]") && line.contains("[maxEnd]")) {
					value = new Object() {
						double convert(String s) {
							try {
								return Double.parseDouble(s.trim());
							} catch (Exception e) {
							}
							return 0;
						}
					}.convert(line.substring((int) (line.indexOf("[value]") + 7), (int) line.indexOf("[valueEnd]")));
					maxValue = new Object() {
						double convert(String s) {
							try {
								return Double.parseDouble(s.trim());
							} catch (Exception e) {
							}
							return 0;
						}
					}.convert(line.substring((int) (line.indexOf("[max]") + 5), (int) line.indexOf("[maxEnd]")));
					attribute = line.substring((int) (line.indexOf("[attribute]") + 11), (int) line.indexOf("[attributeEnd]"));
					if (line.contains("[mob]") && line.contains("[mobEnd]")) {
						mobRegistry = line.substring((int) (line.indexOf("[mob]") + 5), (int) line.indexOf("[mobEnd]"));
					} else {
						mobRegistry = "";
					}
					if (!((LivingEntity) entity).getAttributes()
							.hasAttribute(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation((attribute.substring(0, (int) attribute.indexOf(":"))), (attribute.substring((int) (attribute.indexOf(":") + 1))))))) {
						continue;
					}
					if (!(mobRegistry).isEmpty()) {
						if (!(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString()).equals(mobRegistry)) {
							continue;
						}
					}
					currentValue = ((LivingEntity) entity).getAttribute(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation((attribute.substring(0, (int) attribute.indexOf(":"))), (attribute.substring((int) (attribute.indexOf(":") + 1))))))
							.getBaseValue() + level * value;
					if (currentValue > maxValue) {
						currentValue = maxValue;
					}
					{
						Entity _ent = entity;
						if (!_ent.level().isClientSide() && _ent.getServer() != null) {
							_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
									_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), ("attribute @s " + attribute + " base set " + currentValue));
						}
					}
					count = count + 1;
				} else {
					AscendantMobsMod.LOGGER.error("Error in config files of attributes_list please check the syntaxt is correct");
				}
			}
			if (entity instanceof LivingEntity _entity)
				_entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1);
			if (MobsLevelsMainConfigConfiguration.RANDOM_EFFECTS.get() && IsMobCanAscendantProcedure.execute(entity) && level >= (double) MobsLevelsMainConfigConfiguration.RANDOM_EFFECTS_LEVEL.get()) {
				if (Mth.nextDouble(RandomSource.create(), 0, 100) <= (double) MobsLevelsMainConfigConfiguration.RANDOM_EFFECTS_CHANCE.get()) {
					RandomEffectsEntityProcedureProcedure.execute(entity);
				}
			}
			if (MobsLevelsMainConfigConfiguration.USE_LEGACY_HUD.get()) {
				show = true;
				for (String stringiterator : MobsLevelsMainConfigConfiguration.HIDE_HUD_FOR.get()) {
					if (stringiterator.contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString())) {
						show = false;
						break;
					}
				}
			}
			if (show) {
				entity.setCustomName(Component.literal(("\u00A72[Lvl." + new java.text.DecimalFormat("##").format(level) + "]\u00A7f " + entity.getDisplayName().getString())));
			}
			if (entity instanceof LivingEntity _livingEntity39 && _livingEntity39.getAttributes().hasAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get()))
				_livingEntity39.getAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get()).setBaseValue(level);
			if (entity instanceof LivingEntity _livingEntity40 && _livingEntity40.getAttributes().hasAttribute(AscendantMobsModAttributes.AM_GOT_LEVEL.get()))
				_livingEntity40.getAttribute(AscendantMobsModAttributes.AM_GOT_LEVEL.get()).setBaseValue(1);
		}
	}
}