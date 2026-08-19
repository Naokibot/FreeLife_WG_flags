# FreeLifeWGFlags 1.3.0 review

Target: Spigot 1.21.1 / Java 21 / WorldGuard 7.0.12 / WorldEdit 7.3.8

## Scope

Version 1.3.0 keeps all 26 custom flags. The behavior change is limited to `fl-place-blocks` and `fl-break-blocks`: an explicitly allow-listed material now takes precedence over WorldGuard's special `build` denial for direct player placement/breaking.

## Build override review

1. **Blindly un-cancelling Bukkit BlockPlaceEvent/BlockBreakEvent would bypass unrelated protection plugins.**
   - The implementation does not un-cancel the Bukkit event.
   - It sets WorldGuard's delegate block-event result to ALLOW before WorldGuard's build protection listener evaluates the event.

2. **The override must be higher than BUILD, not higher than every WorldGuard protection flag.**
   - An explicit WorldGuard `block-place deny` remains authoritative for placement.
   - An explicit WorldGuard `block-break deny` remains authoritative for breaking.
   - This matches WorldGuard's own build-related precedence model where protection-specific flags can override BUILD.

3. **Indirect block changes should not inherit a player allow-list accidentally.**
   - Override eligibility is limited to delegate events whose original event is Bukkit `BlockPlaceEvent` or `BlockBreakEvent`.
   - Pistons, explosions, fire spread, falling blocks, liquids, and other indirect changes are not pre-allowed.

4. **Multi-block direct placement needs all affected locations/materials to qualify.**
   - Every WorldGuard delegate block in the operation must have an applicable `fl-place-blocks` configuration that permits the effective material.
   - If any block fails, the delegate is left at DEFAULT and WorldGuard performs its normal protection check.

5. **WorldGuard's delegate events are documented as internal API.**
   - This plugin deliberately targets WorldGuard 7.0.12 and CI compiles against that exact version.
   - The internal hook is isolated in `BuildOverrideListener` so a future WorldGuard upgrade has one compatibility surface to review.

## Existing hardening retained

- All 26 custom flags retain their registration names and value formats.
- Real-clock schedules keep deterministic time-zone and boundary handling.
- Water-triggered effects continue to use Bukkit-independent parsing plus live registry resolution.
- Effect exit cleanup preserves pre-existing region effects when safe.
- Rollback restores only when the expected post-operation block still matches and suppresses break drops/XP while rollback is active.
- Stay/AFK teleports honor item entry/exit restrictions.
- Async chat never queries WorldGuard from the async event thread.
- Storage protection is checked at both interaction and inventory-open layers.

## Verification boundary

GitHub Actions builds against the real Spigot 1.21.1, WorldGuard 7.0.12, and WorldEdit 7.3.8 APIs, runs the test suite, validates the release JAR, checks Java 21 class version 65, and verifies that API dependencies are not shaded into the plugin. A real Minecraft client/live staging server is not available in CI, so final interaction feel and cross-plugin ordering still require staging E2E testing.
