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
| `fl-remove-effects-on-exit` | State | `allow` | Removes effects applied by this plugin when leaving |
| `fl-time-switch` | String | see below | Switches FreeLife State flags using Minecraft world time |
| `fl-real-time-switch` | String | see below | Switches FreeLife State flags using real clock time |
| `fl-stay-seconds` | Integer | `300` | Seconds before the stay-time teleport triggers |
| `fl-stay-tp` | String | `hub;0.5;64;0.5;0;0` | Stay-time teleport destination |
| `fl-afk-seconds` | Integer | `300` | Seconds before a player is considered AFK |
| `fl-afk-tp` | String | `hub;0.5;64;0.5;0;0` | AFK teleport destination |
| `fl-item-entry` | State | `deny` | Blocks entering while carrying items |
| `fl-item-exit` | State | `deny` | Blocks leaving while carrying items |
| `fl-place-blocks` | String | `stone,cobblestone` | Limits blocks that may be placed |
| `fl-break-blocks` | String | `stone,cobblestone` | Limits blocks that may be broken |
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

When exit cleanup is enabled, the plugin removes only effects that it applied. If the player had an effect of the same type before entering, the previous effect is recorded and restored when it is safe to do so.

## Minecraft-time switching

`fl-time-switch` changes FreeLife State flags according to Minecraft world time.

Format:

```text
startTick-endTick:flag=allow,flag=deny|startTick-endTick:flag=allow
```

Example:

```text
/rg flag arena fl-time-switch 0-11999:fl-wind-charge=allow|12000-23999:fl-wind-charge=deny
```

Ranges that cross Minecraft midnight, such as `18000-1000`, are supported.

## Real-clock-time switching

`fl-real-time-switch` changes FreeLife State flags according to the real clock. The default time zone is `Asia/Tokyo`.

Configure the time zone in `plugins/FreeLifeWGFlags/config.yml`:

```yaml
schedule:
  real-time-zone: 'Asia/Tokyo'
```

Any Java `ZoneId` such as `UTC`, `Asia/Tokyo`, or `America/New_York` may be used. If the configured zone is invalid, the plugin logs a warning and falls back to `Asia/Tokyo`.

Format:

```text
start-end:flag=allow,flag=deny|start-end:flag=allow
```

Times may use either `H`/`HH` or `HH:mm` notation.

```text
/rg flag arena fl-real-time-switch 9-17:fl-wind-charge=allow|17-24:fl-wind-charge=deny
```

The same rule with minute precision:

```text
/rg flag arena fl-real-time-switch 09:30-17:45:fl-wind-charge=allow|17:45-24:fl-wind-charge=deny
```

Multiple State flags may be changed in one window:

```text
/rg flag arena fl-real-time-switch 09:00-18:00:fl-wind-charge=allow,fl-invincible=deny|18:00-24:fl-wind-charge=deny,fl-invincible=allow
```

Overnight windows are supported:

```text
/rg flag nightzone fl-real-time-switch 22:00-06:00:fl-invincible=allow
```

Time-window rules are **start-inclusive and end-exclusive**. `09:00-17:00` is active at 09:00 through 16:59 and stops at 17:00. `24` or `24:00` is accepted only as an end time. A zero-length range such as `09:00-09:00` is ignored.

If the same State flag is controlled by its normal region value, `fl-time-switch`, and `fl-real-time-switch`, the order is:

1. normal region State flag
2. Minecraft-time override from `fl-time-switch`
3. real-clock override from `fl-real-time-switch`

A real-clock rule only overrides the value while one of its matching windows is active. Outside all matching real-time windows, the result falls back to the Minecraft-time override or the normal region value.

## Stay-time teleport and AFK teleport

Teleport destinations use either `world;x;y;z` or `world;x;y;z;yaw;pitch`.

```text
/rg flag queue fl-stay-seconds 600
/rg flag queue fl-stay-tp world;0.5;70;0.5;90;0

/rg flag lobby fl-afk-seconds 300
/rg flag lobby fl-afk-tp afk;0.5;64;0.5
```

If an automatic teleport conflicts with `fl-item-entry` or `fl-item-exit`, the item boundary restriction wins and the teleport is not performed.

AFK activity includes movement, inventory click/drag, held-slot changes, item drops, and main/off-hand swaps.

## Item entry and exit restrictions

```text
/rg flag minigame fl-item-entry deny
/rg flag minigame fl-item-exit deny
```

The check includes storage inventory, armor, and off-hand items. Items are not silently removed, escrowed, or copied.

If a player dies inside an effective `fl-item-exit deny` region with `keepInventory` enabled while carrying items, keep-inventory is disabled for that death so the items remain as drops inside the restricted region rather than being carried out through respawn.

## Allowed block lists

```text
/rg flag build fl-place-blocks stone,cobblestone,oak_planks
/rg flag build fl-break-blocks stone,cobblestone,oak_planks
```

`*` allows every block and `none` allows no block. These flags do not bypass WorldGuard's own BUILD denial; they further restrict operations that WorldGuard already permits.

## Block rollback

```text
/rg flag arena fl-block-rollback-seconds 20
```

The previous `BlockState` is restored after the delay only if the current block still matches the expected post-operation state. This prevents a delayed rollback from overwriting a later edit.

When rollback is active for a block break, block drops and block XP are suppressed because the block is later restored. This prevents duplication through break-and-restore loops.

## Allowed chat and commands

```text
/rg flag lobby fl-chat-allowed hello,trade *
/rg flag lobby fl-command-allowed spawn,hub,msg *
```

Rule behavior:

- `*` allows everything.
- `none` allows nothing.
- A trailing `*` performs prefix matching.
- A command rule without arguments, such as `spawn`, matches the command label.

Chat is processed asynchronously by Spigot. The async handler never calls WorldGuard region APIs directly; it reads an immutable policy cached from the main thread. The cache is refreshed immediately after successful region movement/teleport and once per second as a fallback.

## Public utility use with protected storage

```text
/rg flag spawn fl-storage-protection allow
```

This mode explicitly permits block use for doors, buttons, levers, and beds while denying access to:

- chests and trapped chests
- barrels
- hoppers
- all shulker boxes

Storage is checked at both the block-interaction and inventory-open layers.

## Build

```bash
mvn -B verify
```

Output:

```text
target/FreeLifeWGFlags-1.1.0-Spigot-1.21.1.jar
```

## Verification scope

CI compiles against the real Spigot 1.21.1, WorldGuard 7.0.12, and WorldEdit 7.3.8 dependencies, runs unit tests, checks the release JAR, verifies Java 21 class version 65, and ensures Bukkit/WorldGuard/WorldEdit classes are not shaded into the plugin.

A real Minecraft client connected to a live production server is not part of CI, so client-driven end-to-end behavior should still be verified on a staging server before production rollout.
