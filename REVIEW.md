# FreeLifeWGFlags 1.2.0 review

Target: Spigot 1.21.1 / Java 21 / WorldGuard 7.0.12 / WorldEdit 7.3.8

## Scope

Version 1.2.0 keeps the existing 25 flags and their behavior and adds one String flag: `fl-water-effects`.

The new flag applies configurable potion effects when a player touches water or remains swimming in water inside a configured WorldGuard region.

## Water-effect review

1. **A movement-only trigger would miss players who remain swimming without changing blocks.**
   - Water contact is checked on a repeating main-thread task every 5 ticks.
   - Effects are therefore refreshed during continuous swimming rather than only on the first movement event.

2. **A one-second polling interval can let a one-second effect expire between refreshes.**
   - The water task runs every 5 ticks instead of sharing the existing one-second player-state task.
   - This also makes first contact respond within roughly a quarter second rather than up to one second later.

3. **Water contact is broader than a literal `WATER` block at the player's feet.**
   - The implementation first uses Bukkit's entity `isInWater()` state.
   - It also checks the blocks at the player's feet and eyes.
   - `WATER`, `BUBBLE_COLUMN`, and waterlogged block data are treated as water contact.

4. **Continuous refresh and configured duration need explicit semantics.**
   - Each specification uses `effect:level:seconds`.
   - While water contact continues, the same configured duration is refreshed every 5 ticks.
   - After leaving water, the effect naturally expires after the configured duration counted from the final refresh.

5. **Water effects must not conflict with region-wide effect cleanup.**
   - `fl-water-effects` is independent of `fl-effects` and `fl-remove-effects-on-exit`.
   - The water effect is not forcibly removed when the player leaves the region or water; its configured duration controls expiry.

6. **Unbounded values could create invalid or impractical potion effects.**
   - Levels are limited to 1 through 256.
   - Durations are limited to 1 through 86400 seconds.
   - Values are converted to ticks only after validation.

7. **Malformed effect entries must not disable the plugin.**
   - Invalid effect names, levels, durations, and malformed entries are ignored individually.
   - Valid entries in the same flag value continue to work.

8. **Repeated parsing every 5 ticks is unnecessary.**
   - Parsed specifications are cached by their raw WorldGuard flag value.
   - Editing the flag to a new value naturally creates a new parsed entry without requiring a restart.

9. **Water checks should not query WorldGuard for every dry player.**
   - Water contact is checked before querying the region flag.
   - Dry players therefore skip the WorldGuard lookup in the high-frequency task.

10. **Existing effects should not be forcibly stripped just to apply a water effect.**
    - The implementation uses Bukkit's normal `addPotionEffect(PotionEffect)` method rather than the deprecated force overload.

## Existing hardening retained

- Real-clock schedules use deterministic time-zone and boundary handling.
- Minecraft-time and real-time State overrides retain their existing precedence.
- Effect exit cleanup preserves pre-existing region effects when safe.
- Rollback checks the expected post-operation state before restoring a block.
- Rollback-enabled block breaking suppresses drops and block XP to prevent duplication.
- Stay/AFK teleports honor item entry and exit restrictions.
- Async chat never queries WorldGuard from the async event thread.
- AFK activity includes inventory interaction, held-slot changes, drops, and hand swaps.
- `keepInventory` cannot be used to carry items out of an effective item-exit restriction through death.
- Storage protection is checked at both interaction and inventory-open layers.
- Custom flag names/types are preflighted before registration.

## Tests

Version 1.2.0 adds parser regression tests covering:

- multiple effects in one flag
- one-based levels converted to potion amplifiers
- seconds converted to ticks
- zero values
- levels above the configured maximum
- durations above the configured maximum
- unknown effect names
- preservation of valid entries when invalid entries are present

## Verification boundary

GitHub Actions builds against the real Spigot 1.21.1, WorldGuard 7.0.12, and WorldEdit 7.3.8 dependencies, runs the JUnit suite, verifies the release JAR and Java class version, and checks that dependency classes are not shaded into the plugin.

A real Minecraft client connected to the user's live server is not available in CI, so live player-driven water-contact and swimming E2E testing is not claimed.
