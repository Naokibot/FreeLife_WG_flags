# FreeLifeWGFlags

FreeLifeWGFlags is a focused WorldGuard extension for Spigot 1.21.1. It adds FreeLife-specific region flags without bundling or modifying WorldGuard or WorldEdit.

## Requirements

- Java 21
- Spigot 1.21.1
- WorldEdit 7.3.8
- WorldGuard 7.0.12

Custom flags are registered during `onLoad()`. After replacing the JAR, fully stop the server and start it normally. Do not use `/reload` for plugin replacement.

## Custom flags

| Flag | Type | Example | Behavior |
|---|---|---|---|
| `fl-villager-trade` | State | `deny` | Blocks trading with villagers and wandering traders |
| `fl-only-wheat-seeds` | State | `allow` | Restricts crop planting to wheat seeds |
| `fl-wind-charge` | State | `allow` / `deny` | Allows or blocks wind charge use |
| `fl-ender-pearl` | State | `deny` | Blocks ender pearls |
| `fl-chorus-fruit` | State | `deny` | Blocks chorus fruit |
| `fl-invincible` | State | `allow` | Makes players invulnerable inside the region |
| `fl-entry-message` | String | `&6Welcome` | Sends a message when a player enters the region |
| `fl-animal-damage` | State | `deny` | Cancels damage to animals |
| `fl-named-animal-damage` | State | `deny` | Cancels damage to named animals |
| `fl-effects` | String | `speed:2,night_vision:1` | Applies potion effects to players in the region |
| `fl-water-effects` | String | `speed:2:10,water_breathing:1:30` | Applies timed effects while touching or swimming in water |
| `fl-remove-effects-on-exit` | State | `allow` | Removes effects applied by `fl-effects` when leaving |
| `fl-time-switch` | String | see below | Switches FreeLife State flags using Minecraft world time |
| `fl-real-time-switch` | String | see below | Switches FreeLife State flags using real clock time |
| `fl-stay-seconds` | Integer | `300` | Seconds before the stay-time teleport triggers |
| `fl-stay-tp` | String | `hub;0.5;64;0.5;0;0` | Stay-time teleport destination |
| `fl-afk-seconds` | Integer | `300` | Seconds before a player is considered AFK |
| `fl-afk-tp` | String | `hub;0.5;64;0.5;0;0` | AFK teleport destination |
| `fl-item-entry` | State | `deny` | Blocks entering while carrying items |
| `fl-item-exit` | State | `deny` | Blocks leaving while carrying items |
| `fl-place-blocks` | String | `stone,cobblestone` | Explicitly allows only listed blocks for direct player placement and can override BUILD deny |
| `fl-break-blocks` | String | `stone,cobblestone` | Explicitly allows only listed blocks for direct player breaking and can override BUILD deny |
| `fl-block-rollback-seconds` | Integer | `30` | Restores block changes after the configured delay |
| `fl-chat-allowed` | String | `hello,trade *` | Allows only matching chat messages |
| `fl-command-allowed` | String | `spawn,hub,warp shop*` | Allows only matching commands |
| `fl-storage-protection` | State | `allow` | Keeps doors/buttons/levers/beds public while blocking storage access |

Unless noted otherwise, a State flag uses `deny` to block and `allow` to permit. `fl-only-wheat-seeds`, `fl-invincible`, `fl-remove-effects-on-exit`, and `fl-storage-protection` are modes enabled with `allow`.

## Basic examples

```text
/rg flag farm fl-villager-trade deny
/rg flag farm fl-only-wheat-seeds allow
/rg flag farm fl-wind-charge allow
/rg flag farm fl-ender-pearl deny
/rg flag farm fl-chorus-fruit deny
/rg flag farm fl-invincible allow
/rg flag farm fl-entry-message &6Farm area
/rg flag farm fl-animal-damage deny
/rg flag farm fl-named-animal-damage deny
```

## Potion effects

`fl-effects` accepts comma-separated `effect:level` entries. Levels are one-based.

```text
/rg flag arena fl-effects speed:2,night_vision:1
/rg flag arena fl-remove-effects-on-exit allow
```

When exit cleanup is enabled, the plugin removes only effects that it applied through `fl-effects`. If the player had an effect of the same type before entering, the previous effect is recorded and restored when it is safe to do so.

## Water-triggered effects

`fl-water-effects` applies potion effects while a player is touching water or swimming in water inside the region.

```text
/rg flag pool fl-water-effects speed:2:10,water_breathing:1:30
```

Each entry is `effect:level:seconds`. The water check runs every 5 ticks. While water contact continues, effects are refreshed. After leaving water, each effect remains for the configured duration counted from the final refresh. Water detection includes Bukkit's in-water state, water at the feet or eyes, bubble columns, and waterlogged blocks at those positions.

## Minecraft-time switching

```text
/rg flag arena fl-time-switch 0-11999:fl-wind-charge=allow|12000-23999:fl-wind-charge=deny
```

Ranges that cross Minecraft midnight, such as `18000-1000`, are supported.

## Real-clock-time switching

The default time zone is `Asia/Tokyo`. It can be changed in `plugins/FreeLifeWGFlags/config.yml` with `schedule.real-time-zone`.

```text
/rg flag arena fl-real-time-switch 09:30-17:45:fl-wind-charge=allow|17:45-24:fl-wind-charge=deny
```

Overnight windows such as `22:00-06:00` are supported. Windows are start-inclusive and end-exclusive. Real-clock overrides have precedence over Minecraft-time overrides while a matching real-time window is active.

## Stay-time and AFK teleport

```text
/rg flag queue fl-stay-seconds 600
/rg flag queue fl-stay-tp world;0.5;70;0.5;90;0
/rg flag lobby fl-afk-seconds 300
/rg flag lobby fl-afk-tp afk;0.5;64;0.5
```

Destinations are `world;x;y;z` or `world;x;y;z;yaw;pitch`. Automatic teleports honor item entry/exit restrictions.

## Item entry and exit restrictions

```text
/rg flag minigame fl-item-entry deny
/rg flag minigame fl-item-exit deny
```

Storage inventory, armor, and off-hand items are checked. Items are not silently removed or escrowed.

## Explicit block allow lists and BUILD precedence

```text
/rg flag build fl-place-blocks stone,cobblestone,oak_planks
/rg flag build fl-break-blocks stone,cobblestone,oak_planks
```

`*` allows every block and `none` allows no block.

For a direct player `BlockPlaceEvent` or `BlockBreakEvent`, a material explicitly allowed by `fl-place-blocks` or `fl-break-blocks` is evaluated above WorldGuard's special `build` flag. This means an allow-listed material can be placed or broken even when `build` itself is denied.

This override is deliberately narrow:

- an explicit WorldGuard `block-place deny` still blocks placement;
- an explicit WorldGuard `block-break deny` still blocks breaking;
- the override applies only to direct player block place/break events, not pistons, explosions, fire spread, liquid flow, or other indirect world changes;
- it does not un-cancel an event denied by another Bukkit plugin.

The implementation uses WorldGuard's delegate block event result so WorldGuard's own build check sees an explicit ALLOW. It does not modify WorldGuard itself.

## Block rollback

```text
/rg flag arena fl-block-rollback-seconds 20
```

The previous `BlockState` is restored after the delay only if the current block still matches the expected post-operation state. Rollback-enabled breaking suppresses drops and block XP to prevent break-and-restore duplication.

## Allowed chat and commands

```text
/rg flag lobby fl-chat-allowed hello,trade *
/rg flag lobby fl-command-allowed spawn,hub,msg *
```

`*` allows everything, `none` allows nothing, and a trailing `*` performs prefix matching.

## Public utility use with protected storage

```text
/rg flag spawn fl-storage-protection allow
```

Doors, buttons, levers, and beds remain usable. Chests, trapped chests, barrels, hoppers, and all shulker boxes are blocked at both the interaction and inventory-open layers.

## Separate marine mobs plugin

`marine-mobs/` contains `FreeLifeMarineMobs`, a separate Spigot-only plugin for command-spawned rideable shark and orca entities. It is not loaded as part of FreeLifeWGFlags. See `marine-mobs/README.md` for commands, rendering limitations, and model references.

## Build

```bash
mvn -B verify
mvn -B -f marine-mobs/pom.xml verify
```

Outputs:

```text
target/FreeLifeWGFlags-1.3.0-Spigot-1.21.1.jar
marine-mobs/target/FreeLifeMarineMobs-1.0.0-Spigot-1.21.1.jar
```

## Verification scope

CI compiles both plugins against their real Spigot/WorldGuard/WorldEdit dependencies, runs unit tests, checks both release JARs, verifies Java 21 class version 65, and checks that API dependency classes are not shaded into the plugins. Live Minecraft-client E2E is still required before production rollout.
