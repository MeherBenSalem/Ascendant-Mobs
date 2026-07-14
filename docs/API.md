# Public API (v2.0.0)

Package: `tn.nightbeam.rpgmoblevelingsystem.api`

## RmlApi

```java
int getLevel(Entity entity);
void recalculate(Entity entity);
LevelDebugBreakdown getDebugBreakdown(Entity entity);
Integer getDisplayLevel(Level world, Entity entity);
```

All mutation calls must run server-side.

## LevelDebugBreakdown

Fields: `entityId`, `baseLevel`, `spawnDifference`, `playerContribution`, `structureMin`, `lockedLevel`, `beforeClamp`, `finalLevel`, `scaleType`, `playerScaleMode`, `rasLoaded`.

## RPG Attribute System integration

When `rpg_attribute_system` 4.1+ is present, configure `scale_settings.json`:

- `playerScaleMode`: `none`, `nearest`, `highest_nearby`, `average_nearby`
- `playerScaleRadius`, `playerScaleFactor`
- `playerBalanceEnabled`, `playerBalanceHealthRatio`, `playerBalanceDamageRatio`

Mob levels consume `tn.nightbeam.ras.api.RasApi` via reflection — no duplicated RAS formulas.
