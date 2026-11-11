package com.nightbeam.remnants.mobs.forge;

import com.nightbeam.remnants.mobs.RemnantMobs;
import com.nightbeam.remnants.mobs.RemnantsEvents;
import com.nightbeam.remnants.mobs.content.ModContent;
import com.nightbeam.remnants.mobs.network.ConfigRequestPayload;
import com.nightbeam.remnants.mobs.network.ConfigSync;
import com.nightbeam.remnants.mobs.network.ConfigUpdatePayload;
import com.nightbeam.remnants.mobs.network.ConfigValuesPayload;
import com.nightbeam.remnants.mobs.network.DifficultyPayload;
import com.nightbeam.remnants.mobs.network.MobInfoPayload;
import com.nightbeam.remnants.mobs.network.PlayerStatePayload;
import com.nightbeam.remnants.mobs.platform.Platform;
import com.nightbeam.remnants.mobs.platform.Registrar;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;

/** The Forge entry point (Forge 66, EventBus 7). */
@Mod(RemnantMobs.MOD_ID)
public final class RemnantMobsForge {
    /** Bumped when a payload changes shape; clients with another version, or without the mod, still join. */
    private static final int PROTOCOL = 3;
    private static Channel<CustomPacketPayload> channel;
    /** The settings screen's own optional channel, so clients and servers without it still get the main one. */
    private static Channel<CustomPacketPayload> configChannel;

    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new HashMap<>();

    public RemnantMobsForge(FMLJavaModLoadingContext context) {
        // Fabric and NeoForge refuse to load next to 2.x; Forge's mods.toml cannot say "incompatible".
        if (ModList.isLoaded("rpgmoblevelingsystem")) {
            throw new IllegalStateException("Remnants: RPG Mobs replaces RPG Mob Leveling System. Remove the rpgmoblevelingsystem jar from the mods folder.");
        }
        BusGroup modBus = context.getModBusGroup();
        RemnantMobs.init(new ForgePlatform(), new ForgeRegistrar());
        registers.values().forEach(deferred -> deferred.register(modBus));
        channel = buildChannel();
        configChannel = buildConfigChannel();

        BuildCreativeModeTabContentsEvent.BUS.addListener(RemnantMobsForge::buildTabs);
        // Last, and skipped when another mod cancels the death. Not a monitor: EventBus 7 never calls monitors on a
        // bus without ordinary listeners, which is the case for this event unless another mod listens too.
        LivingDeathEvent.BUS.addListener(Priority.LOWEST, (LivingDeathEvent event) -> RemnantsEvents.onLivingDeath(event.getEntity(), event.getSource()));
        ServerStartedEvent.BUS.addListener(event -> RemnantsEvents.onServerStarted(event.getServer()));
        ServerStoppingEvent.BUS.addListener(event -> RemnantsEvents.onServerStopping(event.getServer()));
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> RemnantsEvents.onServerTick(event.server()));
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                RemnantsEvents.onPlayerJoin(player);
            }
        });
        PlayerEvent.Clone.BUS.addListener(event -> {
            if (event.getOriginal() instanceof ServerPlayer from && event.getEntity() instanceof ServerPlayer to) {
                RemnantsEvents.onPlayerClone(from, to, !event.isWasDeath());
            }
        });
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                RemnantsEvents.onPlayerLeave(player);
            }
        });
        RegisterCommandsEvent.BUS.addListener(event -> RemnantsEvents.registerCommands(event.getDispatcher(), event.getBuildContext()));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RemnantMobsForgeClient.init(context);
        }
    }

    /** One optional channel for the three clientbound payloads; the handlers only run on clients. */
    private static Channel<CustomPacketPayload> buildChannel() {
        return ChannelBuilder.named(RemnantMobs.id("main"))
                .networkProtocolVersion(PROTOCOL)
                .optional()
                .payloadChannel()
                .play()
                .clientbound()
                .addMain(MobInfoPayload.TYPE, MobInfoPayload.CODEC.cast(), (payload, context) -> ClientHandlers.mobInfo(payload))
                .addMain(PlayerStatePayload.TYPE, PlayerStatePayload.CODEC.cast(), (payload, context) -> ClientHandlers.playerState(payload))
                .addMain(DifficultyPayload.TYPE, DifficultyPayload.CODEC.cast(), (payload, context) -> ClientHandlers.difficulty(payload))
                .build();
    }

    @SuppressWarnings("unchecked")
    private <R> DeferredRegister<R> register(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) registers.computeIfAbsent(registry, key -> DeferredRegister.create(registry, RemnantMobs.MOD_ID));
    }

    private static Channel<CustomPacketPayload> buildConfigChannel() {
        return ChannelBuilder.named(RemnantMobs.id("config"))
                .networkProtocolVersion(1)
                .optional()
                .payloadChannel()
                .play()
                .clientbound()
                .addMain(ConfigValuesPayload.TYPE, ConfigValuesPayload.CODEC.cast(), (payload, context) -> ClientHandlers.configValues(payload))
                .serverbound()
                .addMain(ConfigRequestPayload.TYPE, ConfigRequestPayload.CODEC.cast(), (payload, context) -> {
                    if (context.getSender() != null) {
                        ConfigSync.onRequest(context.getSender());
                    }
                })
                .addMain(ConfigUpdatePayload.TYPE, ConfigUpdatePayload.CODEC.cast(), (payload, context) -> {
                    if (context.getSender() != null) {
                        ConfigSync.onUpdate(context.getSender(), payload);
                    }
                })
                .build();
    }

    static Channel<CustomPacketPayload> configChannel() {
        return configChannel;
    }

    private static Channel<CustomPacketPayload> channelFor(CustomPacketPayload.Type<?> type) {
        return type == ConfigValuesPayload.TYPE || type == ConfigRequestPayload.TYPE || type == ConfigUpdatePayload.TYPE ? configChannel : channel;
    }

    private static void buildTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModContent.REMNANT_SHARD);
            event.accept(ModContent.VITAL_EMBER);
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModContent.HUNTERS_LENS);
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModContent.BRAND_OF_CHALLENGE);
        } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModContent.MENDING_SALVE);
            event.accept(ModContent.HUNTERS_DRAUGHT);
        }
    }

    private final class ForgeRegistrar implements Registrar {
        @Override
        public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String path, Function<Identifier, T> factory) {
            Identifier id = RemnantMobs.id(path);
            return RemnantMobsForge.this.register(registry).register(path, () -> factory.apply(id));
        }
    }

    private static final class ForgePlatform implements Platform {
        @Override
        public Path configDir() {
            return FMLPaths.CONFIGDIR.get();
        }

        @Override
        public boolean isModLoaded(String modId) {
            return ModList.isLoaded(modId);
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            Channel<CustomPacketPayload> target = channelFor(payload.type());
            if (target != null && target.isRemotePresent(player.connection.getConnection())) {
                target.send(payload, PacketDistributor.PLAYER.with(player));
            }
        }

        @Override
        public boolean hasChannel(ServerPlayer player, CustomPacketPayload.Type<?> type) {
            Channel<CustomPacketPayload> target = channelFor(type);
            return target != null && target.isRemotePresent(player.connection.getConnection());
        }

        @Override
        public void sendToTracking(Entity entity, CustomPacketPayload payload) {
            // Only players in tracking range that have the channel: unmodded clients must not get it.
            if (!(entity.level() instanceof ServerLevel level)) {
                return;
            }
            double range = entity.getType().clientTrackingRange() * 16.0;
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(entity) <= range * range) {
                    sendToPlayer(player, payload);
                }
            }
        }

        @Override
        public boolean isFakePlayer(ServerPlayer player) {
            // Forge 66 has no FakePlayer class; automation players of other mods subclass ServerPlayer.
            return player.getClass() != ServerPlayer.class;
        }

        @Override
        public boolean isClient() {
            return FMLEnvironment.dist == Dist.CLIENT;
        }
    }
}
