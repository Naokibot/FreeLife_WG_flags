# FreeLifeWGFlags 1.0.0 review

Target: Spigot 1.21.1 / Java 21 / WorldGuard 7.0.12 / WorldEdit 7.3.8

## Design

- WorldGuard and WorldEdit are runtime dependencies and are not shaded into the plugin.
- Custom flags are registered from `onLoad()`, before WorldGuard closes its flag registry during enable.
- All custom names use the `fl-` prefix to avoid collisions with standard WorldGuard flags.
- Repository structure is deliberately small: one flag registry, one WorldGuard query layer, focused listeners, focused state services, and small parsers.
- There is no catch-all god class and no framework-like abstraction layer.

## Issues found during review and fixed

1. **Effect removal originally risked deleting effects that existed before region entry.**
   - Previous effects are now snapshotted per effect type.
   - Exit cleanup only removes a short-lived effect matching the amplifier applied by this plugin, then restores the recorded prior effect.
   - If another plugin replaces the effect while the player is inside, the newer effect is not overwritten during cleanup.

2. **A first rollback implementation compared a broken block with its pre-break material.**
   - A break event now records `AIR` as the expected post-operation state.
   - A placed block records its actual post-place BlockData.
   - Restoration is skipped if the current state no longer matches the expected state, preventing delayed rollback from overwriting later edits.

3. **Multi-block placement could have restored only one block.**
   - `BlockMultiPlaceEvent#getReplacedBlockStates()` is handled and every replaced block is snapshotted.

4. **Plugin-driven timeout/AFK teleports could bypass item boundary rules.**
   - Every plugin teleport is checked against both `fl-item-exit` at the source and `fl-item-entry` at the destination.

5. **Moving directly from one restricted region to another could bypass simple boolean boundary checks.**
   - Boundary enforcement compares the effective highest-priority region keys, not only a yes/no state.

6. **Lower-priority time rules could have overridden higher-priority state flags.**
   - Dynamic time state is now resolved at the highest active region priority and uses WorldGuard StateFlag conflict semantics (`DENY` wins within the same priority).

7. **Async chat must not call Bukkit/WorldGuard region APIs.**
   - The main thread refreshes the chat policy once per second.
   - `AsyncPlayerChatEvent` reads only a concurrent immutable rule object and schedules denial messages back to the main thread.

8. **Item escrow would introduce crash-loss and duplication risk.**
   - No inventory is automatically removed or stored.
   - Entry/exit is denied while inventory, armor or offhand contains an item.

9. **The storage requirement includes public utility use, not just chest denial.**
   - `fl-storage-protection allow` explicitly permits block use for doors, buttons, levers and beds.
   - Storage is denied at both interaction and inventory-open layers.

10. **Flag conflict could partially register the flag set.**
    - Flag names/types are preflighted before registration.
    - Existing flags are reused only when the type matches exactly; an incompatible conflict fails fast.

11. **Rollback-enabled breaking could duplicate drops when the block reappears.**
    - Drops and block XP are suppressed only when `fl-block-rollback-seconds` is active for that break.

12. **AFK detection originally ignored inventory-only activity.**
    - Inventory click/drag, held-slot changes, item drops and hand swaps now refresh activity.

13. **The async chat cache could remain stale for up to one second after crossing a region boundary.**
    - Successful move/teleport events refresh the destination chat policy at `MONITOR` priority immediately after boundary enforcement.

14. **`keepInventory` death could carry items out of an `fl-item-exit deny` region.**
    - If a player dies inside an effective item-exit restriction while carrying items and `keepInventory` is true, keep-inventory is disabled for that death so the items remain as drops inside the region.

15. **Border planting and entry-message rendering were hardened.**
    - The wheat-only rule checks the clicked farmland/block location, not only the player's feet.
    - `&` color codes are translated when entry messages are sent.

## Verification boundary

The repository includes parser regression tests and a GitHub Actions build using the real Spigot/WorldGuard/WorldEdit dependencies. A real Minecraft client connected to the user's live Spigot server is not available in this environment, so player-driven E2E behavior must not be claimed unless a server/client run is actually performed.
