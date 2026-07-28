# Graph Report - Ascendant-Mobs  (2026-07-28)

## Corpus Check
- 139 files · ~43,522 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 856 nodes · 1240 edges · 95 communities (70 shown, 25 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `51e1c927`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ModConfig
- IPlatformHelper
- CalenderItem.java
- RpgMobLevelingSystemNeoForge.java
- RpgMobLevelingSystemMod.java
- LevelDebugBreakdown
- RpgMobLevelingSystemModAttributes.java
- .execute
- GetBestSpawnParamCmdProcedure.java
- RasCompatibilityLayer
- .calculate
- .calculate
- AttributeScalingService
- CalenderItem.java
- AttributeScalingService
- ForgeGameplayEvents
- 2.0.0
- MobLevelService
- MobLevelService
- MobLevelingLogic
- MobLevelingLogic
- RPG Mob Leveling System 2.0.0
- JadeCompat
- EntityClassification
- Test
- MobsLevelSystemProcedure.java
- WhenBossDiesProcedure.java
- JadeCompat
- EntityClassification
- Test
- FabricConfigBootstrap
- LivingEntityExperienceMixin.java
- ModInitializer
- OnJoinWorldProcedure.java
- UpdateBossBarProcedure.java
- LivingEntityExperienceMixin.java
- .render
- MobLevelStorage
- OutgoingDamageScaling
- CreateOrReplaceAttributesConfigProcedure.java
- CreateOrReplaceDimensionsConfigProcedure.java
- CreateOrReplaceGlobalConfigProcedure.java
- CreateOrReplaceMobListConfigProcedure.java
- CreateOrReplaceScaleConfigProcedure.java
- .render
- MobLevelStorage
- OutgoingDamageScaling
- ScalingMath
- ForgeMobLevelOverlay
- ScalingMath
- NeoForgeMobLevelOverlay.java
- MultiLoader Template
- RmlCommands.java
- AscendantService
- RPG Mob Leveling System 1.5.0 - Patch Notes
- MultiLoader Template
- RmlCommands.java
- AscendantService
- IPlatformHelper
- LevelDebugBreakdown
- RpgMobLevelingSystemFabricClient
- Public API (v2.0.0)
- RPG Mob Leveling System v2.0.1
- Constants
- RpgMobLevelingSystemCommon
- FabricMobLevelOverlay
- FabricMobLeveling
- RpgMobLevelingSystemFabricItems
- CanGetLevelProcedureProcedure.java
- IsMobCanAscendantProcedure.java
- LockedMobsProcedureProcedure.java
- RandomEffectsEntityProcedureProcedure.java
- gradlew
- Constants
- RpgMobLevelingSystemCommon
- FabricMobLevelOverlay
- FabricMobLeveling
- gradlew
- FEEDBACK_RESOLUTION.md

## God Nodes (most connected - your core abstractions)
1. `ModConfig` - 25 edges
2. `ModConfig` - 25 edges
3. `IPlatformHelper` - 17 edges
4. `LevelCalculationService` - 11 edges
5. `LevelCalculationService` - 11 edges
6. `RpgMobLevelingSystemNeoForge` - 10 edges
7. `LevelDebugBreakdown` - 9 edges
8. `RpgMobLevelingSystemMod` - 9 edges
9. `AttributeScalingService` - 9 edges
10. `DimensionsSettings` - 8 edges

## Surprising Connections (you probably didn't know these)
- `ModConfig` --references--> `GlobalSettings`  [EXTRACTED]
  1.21.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java → 1.20.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java
- `ModConfig` --references--> `ScaleSettings`  [EXTRACTED]
  1.21.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java → 1.20.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java
- `ModConfig` --references--> `DimensionsSettings`  [EXTRACTED]
  1.21.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java → 1.20.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java
- `DimensionsSettings` --references--> `DimensionRange`  [EXTRACTED]
  1.21.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java → 1.20.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java
- `ModConfig` --references--> `AttributesSettings`  [EXTRACTED]
  1.21.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java → 1.20.1/common/src/main/java/tn/nightbeam/rpgmoblevelingsystem/config/ModConfig.java

## Import Cycles
- None detected.

## Communities (95 total, 25 thin omitted)

### Community 0 - "ModConfig"
Cohesion: 0.06
Nodes (24): AttributeRule, AttributesSettings, DimensionRange, DimensionsSettings, GlobalSettings, Gson, ResourceLocation, LootBand (+16 more)

### Community 1 - "IPlatformHelper"
Cohesion: 0.07
Nodes (11): IPlatformHelper, Services, FabricPlatformHelper, Override, ForgePlatformHelper, Override, Services, FabricPlatformHelper (+3 more)

### Community 2 - "CalenderItem.java"
Cohesion: 0.10
Nodes (24): CalenderItem, Component, InteractionHand, InteractionResultHolder, ItemStack, Level, Override, Player (+16 more)

### Community 3 - "RpgMobLevelingSystemNeoForge.java"
Cohesion: 0.08
Nodes (19): Item, ModItems, Item, RpgMobLevelingSystemFabricItems, IEventBus, Item, RpgMobLevelingSystemNeoForgeItems, BuildCreativeModeTabContentsEvent (+11 more)

### Community 4 - "RpgMobLevelingSystemMod.java"
Cohesion: 0.11
Nodes (19): DeferredRegister, Item, RegistryObject, RpgMobLevelingSystemModItems, BuildCreativeModeTabContentsEvent, DeferredRegister, EventBusSubscriber, SubscribeEvent (+11 more)

### Community 5 - "LevelDebugBreakdown"
Cohesion: 0.13
Nodes (8): Entity, Level, RmlApi, Override, LevelDebugBreakdown, Entity, Level, RmlApi

### Community 6 - "RpgMobLevelingSystemModAttributes.java"
Cohesion: 0.15
Nodes (16): DeferredRegister, EventBusSubscriber, RegistryObject, SubscribeEvent, PlayerAttributesSync, RpgMobLevelingSystemModAttributes, ExperienceDropBasedOnLevelProcedure, Entity (+8 more)

### Community 7 - ".execute"
Cohesion: 0.13
Nodes (11): CalculateSpawnDifferenceProcedure, Entity, LevelAccessor, GetBaseLevelProcedure, LevelAccessor, GetLowestEntityLevelProcedure, LevelAccessor, GetMaxLevelProcedure (+3 more)

### Community 8 - "GetBestSpawnParamCmdProcedure.java"
Cohesion: 0.18
Nodes (12): GetDistanceFromSpawnCommand, EventBusSubscriber, RegisterCommandsEvent, SubscribeEvent, GetBestSpawnParamCmdProcedure, CommandSourceStack, Entity, LevelAccessor (+4 more)

### Community 9 - "RasCompatibilityLayer"
Cohesion: 0.17
Nodes (6): Player, PlayerCombatSnapshot, RasCompatibilityLayer, Player, PlayerCombatSnapshot, RasCompatibilityLayer

### Community 10 - ".calculate"
Cohesion: 0.35
Nodes (4): Entity, Level, LevelDebugBreakdown, LevelCalculationService

### Community 11 - ".calculate"
Cohesion: 0.35
Nodes (4): Entity, Level, LevelDebugBreakdown, LevelCalculationService

### Community 12 - "AttributeScalingService"
Cohesion: 0.27
Nodes (6): AttributeScalingService, AttributeInstance, AttributeRule, Entity, LivingEntity, ResourceLocation

### Community 13 - "CalenderItem.java"
Cohesion: 0.24
Nodes (10): CalenderItem, Component, InteractionHand, InteractionResultHolder, ItemStack, Level, Override, Player (+2 more)

### Community 14 - "AttributeScalingService"
Cohesion: 0.29
Nodes (5): AttributeScalingService, AttributeInstance, AttributeRule, Entity, LivingEntity

### Community 15 - "ForgeGameplayEvents"
Cohesion: 0.26
Nodes (7): ForgeGameplayEvents, EventBusSubscriber, LivingDeathEvent, LivingExperienceDropEvent, LivingHurtEvent, RegisterCommandsEvent, SubscribeEvent

### Community 16 - "2.0.0"
Cohesion: 0.15
Nodes (12): 1.5.0, 2.0.0, 2.0.1, Breaking changes, Bug Fixes, Bug fixes, Compatibility, Config changes (+4 more)

### Community 17 - "MobLevelService"
Cohesion: 0.35
Nodes (4): Entity, Level, LivingEntity, MobLevelService

### Community 18 - "MobLevelService"
Cohesion: 0.35
Nodes (4): Entity, Level, LivingEntity, MobLevelService

### Community 19 - "MobLevelingLogic"
Cohesion: 0.29
Nodes (5): Entity, Level, LivingEntity, Player, MobLevelingLogic

### Community 20 - "MobLevelingLogic"
Cohesion: 0.29
Nodes (5): Entity, Level, LivingEntity, Player, MobLevelingLogic

### Community 21 - "RPG Mob Leveling System 2.0.0"
Cohesion: 0.18
Nodes (10): Calendar item, Commands (op level 2), Config (`config/rpgmoblevelingsystem/`), Jade, Loaders & versions, Overview, Player scaling (RAS), Public API (+2 more)

### Community 22 - "JadeCompat"
Cohesion: 0.31
Nodes (4): JadeCompat, Component, LivingEntity, ResourceLocation

### Community 23 - "EntityClassification"
Cohesion: 0.40
Nodes (3): EntityClassification, Entity, LivingEntity

### Community 25 - "MobsLevelSystemProcedure.java"
Cohesion: 0.33
Nodes (7): Entity, EntityJoinLevelEvent, Event, EventBusSubscriber, LevelAccessor, SubscribeEvent, MobsLevelSystemProcedure

### Community 26 - "WhenBossDiesProcedure.java"
Cohesion: 0.33
Nodes (7): Entity, Event, EventBusSubscriber, LevelAccessor, LivingDeathEvent, SubscribeEvent, WhenBossDiesProcedure

### Community 27 - "JadeCompat"
Cohesion: 0.31
Nodes (4): JadeCompat, Component, LivingEntity, ResourceLocation

### Community 28 - "EntityClassification"
Cohesion: 0.40
Nodes (3): EntityClassification, Entity, LivingEntity

### Community 31 - "LivingEntityExperienceMixin.java"
Cohesion: 0.36
Nodes (6): CallbackInfoReturnable, DamageSource, Inject, Mixin, ModifyVariable, LivingEntityExperienceMixin

### Community 32 - "ModInitializer"
Cohesion: 0.28
Nodes (5): Override, RpgMobLevelingSystemFabric, Override, RpgMobLevelingSystemFabric, ModInitializer

### Community 33 - "OnJoinWorldProcedure.java"
Cohesion: 0.33
Nodes (6): Entity, Event, EventBusSubscriber, SubscribeEvent, OnJoinWorldProcedure, PlayerLoggedInEvent

### Community 34 - "UpdateBossBarProcedure.java"
Cohesion: 0.36
Nodes (6): Entity, Event, EventBusSubscriber, LivingHurtEvent, SubscribeEvent, UpdateBossBarProcedure

### Community 35 - "LivingEntityExperienceMixin.java"
Cohesion: 0.36
Nodes (6): CallbackInfoReturnable, DamageSource, Inject, Mixin, ModifyVariable, LivingEntityExperienceMixin

### Community 36 - ".render"
Cohesion: 0.39
Nodes (4): Camera, MultiBufferSource, PoseStack, MobLevelOverlayRenderer

### Community 38 - "OutgoingDamageScaling"
Cohesion: 0.46
Nodes (3): DamageSource, LivingEntity, OutgoingDamageScaling

### Community 39 - "CreateOrReplaceAttributesConfigProcedure.java"
Cohesion: 0.39
Nodes (5): CreateOrReplaceAttributesConfigProcedure, Event, EventBusSubscriber, FMLCommonSetupEvent, SubscribeEvent

### Community 40 - "CreateOrReplaceDimensionsConfigProcedure.java"
Cohesion: 0.39
Nodes (5): CreateOrReplaceDimensionsConfigProcedure, Event, EventBusSubscriber, FMLCommonSetupEvent, SubscribeEvent

### Community 41 - "CreateOrReplaceGlobalConfigProcedure.java"
Cohesion: 0.39
Nodes (5): CreateOrReplaceGlobalConfigProcedure, Event, EventBusSubscriber, FMLCommonSetupEvent, SubscribeEvent

### Community 42 - "CreateOrReplaceMobListConfigProcedure.java"
Cohesion: 0.39
Nodes (5): CreateOrReplaceMobListConfigProcedure, Event, EventBusSubscriber, FMLCommonSetupEvent, SubscribeEvent

### Community 43 - "CreateOrReplaceScaleConfigProcedure.java"
Cohesion: 0.39
Nodes (5): CreateOrReplaceScaleConfigProcedure, Event, EventBusSubscriber, FMLCommonSetupEvent, SubscribeEvent

### Community 44 - ".render"
Cohesion: 0.39
Nodes (4): Camera, MultiBufferSource, PoseStack, MobLevelOverlayRenderer

### Community 46 - "OutgoingDamageScaling"
Cohesion: 0.46
Nodes (3): DamageSource, LivingEntity, OutgoingDamageScaling

### Community 48 - "ForgeMobLevelOverlay"
Cohesion: 0.38
Nodes (4): ForgeMobLevelOverlay, EventBusSubscriber, RenderLevelStageEvent, SubscribeEvent

### Community 50 - "NeoForgeMobLevelOverlay.java"
Cohesion: 0.43
Nodes (4): EventBusSubscriber, RenderLevelStageEvent, SubscribeEvent, NeoForgeMobLevelOverlay

### Community 51 - "MultiLoader Template"
Cohesion: 0.29
Nodes (6): Development Guide, Eclipse, Getting Started, IntelliJ IDEA, MultiLoader Template, Removing Platforms and Loaders

### Community 52 - "RmlCommands.java"
Cohesion: 0.47
Nodes (3): CommandDispatcher, CommandSourceStack, RmlCommands

### Community 54 - "RPG Mob Leveling System 1.5.0 - Patch Notes"
Cohesion: 0.33
Nodes (5): 🛠️ Behavior changes, 💡 Notes for players, RPG Mob Leveling System 1.5.0 - Patch Notes, 🚀 Stability, ✅ What’s New

### Community 55 - "MultiLoader Template"
Cohesion: 0.33
Nodes (5): Development Guide, Eclipse, Getting Started, IntelliJ IDEA, MultiLoader Template

### Community 56 - "RmlCommands.java"
Cohesion: 0.47
Nodes (3): CommandDispatcher, CommandSourceStack, RmlCommands

### Community 60 - "RpgMobLevelingSystemFabricClient"
Cohesion: 0.50
Nodes (3): Override, RpgMobLevelingSystemFabricClient, ClientModInitializer

### Community 61 - "Public API (v2.0.0)"
Cohesion: 0.40
Nodes (4): LevelDebugBreakdown, Public API (v2.0.0), RmlApi, RPG Attribute System integration

### Community 62 - "RPG Mob Leveling System v2.0.1"
Cohesion: 0.40
Nodes (4): Bug Fixes, Compatibility, RPG Mob Leveling System v2.0.1, Upgrade Notes

### Community 72 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 77 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **36 isolated node(s):** `LootBand`, `✅ What’s New`, `🛠️ Behavior changes`, `🚀 Stability`, `💡 Notes for players` (+31 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **25 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LevelDebugBreakdown` connect `LevelDebugBreakdown` to `.calculate`, `.calculate`?**
  _High betweenness centrality (0.003) - this node is a cross-community bridge._
- **What connects `LootBand`, `✅ What’s New`, `🛠️ Behavior changes` to the rest of the system?**
  _36 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ModConfig` be split into smaller, more focused modules?**
  _Cohesion score 0.06233538191395961 - nodes in this community are weakly interconnected._
- **Should `IPlatformHelper` be split into smaller, more focused modules?**
  _Cohesion score 0.07396870554765292 - nodes in this community are weakly interconnected._
- **Should `CalenderItem.java` be split into smaller, more focused modules?**
  _Cohesion score 0.09682539682539683 - nodes in this community are weakly interconnected._
- **Should `RpgMobLevelingSystemNeoForge.java` be split into smaller, more focused modules?**
  _Cohesion score 0.07936507936507936 - nodes in this community are weakly interconnected._
- **Should `RpgMobLevelingSystemMod.java` be split into smaller, more focused modules?**
  _Cohesion score 0.1111111111111111 - nodes in this community are weakly interconnected._