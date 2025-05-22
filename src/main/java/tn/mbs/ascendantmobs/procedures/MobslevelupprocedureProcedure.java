package tn.mbs.ascendantmobs.procedures;

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
public class MobslevelupprocedureProcedure {
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
		String attribute = "";
		String mobRegistry = "";
		double level = 0;
		double baseLevel = 0;
		double maxLevel = 0;
		double lockedLevel = 0;
		double maxValue = 0;
		double value = 0;
		double currentValue = 0;
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
				baseLevel = GetBaseLevelFromConfigProcedure.execute(world);
				maxLevel = GetMaxLevelFromConfigProcedure.execute(world);
				level = Math.floor(baseLevel + CalculateSpawnDifferenceProcedure.execute(world, x, y, z, entity) * (double) MobsLevelsMainConfigConfiguration.SCALE_FACTOR.get());
				if (level >= maxLevel) {
					level = maxLevel;
				}
			}
			for (String stringiterator : MobsLevelsMainConfigConfiguration.ATTRIBUTES_LIST.get()) {
				if (stringiterator.contains("[attribute]") && stringiterator.contains("[attributeEnd]") && stringiterator.contains("[value]") && stringiterator.contains("[valueEnd]") && stringiterator.contains("[max]")
						&& stringiterator.contains("[maxEnd]")) {
					value = new Object() {
						double convert(String s) {
							try {
								return Double.parseDouble(s.trim());
							} catch (Exception e) {
							}
							return 0;
						}
					}.convert(stringiterator.substring((int) (stringiterator.indexOf("[value]") + 7), (int) stringiterator.indexOf("[valueEnd]")));
					maxValue = new Object() {
						double convert(String s) {
							try {
								return Double.parseDouble(s.trim());
							} catch (Exception e) {
							}
							return 0;
						}
					}.convert(stringiterator.substring((int) (stringiterator.indexOf("[max]") + 5), (int) stringiterator.indexOf("[maxEnd]")));
					attribute = stringiterator.substring((int) (stringiterator.indexOf("[attribute]") + 11), (int) stringiterator.indexOf("[attributeEnd]"));
					if (stringiterator.contains("[mob]") && stringiterator.contains("[mobEnd]")) {
						mobRegistry = stringiterator.substring((int) (stringiterator.indexOf("[mob]") + 5), (int) stringiterator.indexOf("[mobEnd]"));
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
				entity.setCustomName(Component.literal(("\u00A72[ Lvl." + level + "]\u00A7f " + entity.getDisplayName().getString())));
			}
			if (entity instanceof LivingEntity _livingEntity54 && _livingEntity54.getAttributes().hasAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get()))
				_livingEntity54.getAttribute(AscendantMobsModAttributes.AMLEVEL_ATTRIBUTE.get()).setBaseValue(level);
			if (entity instanceof LivingEntity _livingEntity55 && _livingEntity55.getAttributes().hasAttribute(AscendantMobsModAttributes.AM_GOT_LEVEL.get()))
				_livingEntity55.getAttribute(AscendantMobsModAttributes.AM_GOT_LEVEL.get()).setBaseValue(1);
		}
	}
}
