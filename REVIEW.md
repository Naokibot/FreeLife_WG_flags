# FreeLifeWGFlags 1.1.0 review

Target: Spigot 1.21.1 / Java 21 / WorldGuard 7.0.12 / WorldEdit 7.3.8

## Scope

Version 1.1.0 keeps the existing 24 flags and their behavior, adds `fl-real-time-switch`, and rewrites README.md in English. The new real-time scheduler is limited to FreeLife State flags, matching the semantics of the existing Minecraft-time switch.

## Real-time scheduler review

1. **`HH:mm` contains the same colon character used before the assignment list.**
   - A naive `split(":")` parser would break `09:30-17:45:flag=allow`.
   - The implementation uses an anchored regular expression that parses both time endpoints before the assignment section.

2. **Boundary semantics were ambiguous.**
   - Windows are start-inclusive and end-exclusive.
   - `09:00-17:00` is active from 09:00 through 16:59 and stops exactly at 17:00.

3. **Overnight schedules need different range logic.**
   - `22:00-06:00` matches both late evening and early morning.
   - Unit tests cover both sides of midnight and the exact end boundary.

4. **Administrators commonly write whole-hour schedules.**
   - Both `9-17` and `09:00-17:00` are accepted.
   - `24`/`24:00` is accepted only as the end of a window, allowing `17-24` without introducing an invalid `LocalTime` value.

5. **Zero-length and malformed windows could accidentally become always-on.**
   - Equal start/end ranges, invalid hours/minutes, malformed assignments, and malformed windows are ignored rather than treated as full-day rules.

6. **Time zone handling must not depend on the host machine's local zone.**
   - `schedule.real-time-zone` defaults to `Asia/Tokyo`.
   - An invalid configured ZoneId logs a warning and falls back to `Asia/Tokyo` instead of disabling the plugin.
   - Existing installations that do not yet have the new config key still use the default value.

7. **Minecraft-time and real-time rules need deterministic precedence.**
   - Resolution order is base State flag, then `fl-time-switch`, then `fl-real-time-switch`.
   - A real-time rule only overrides while a matching window exists; otherwise the previous resolved value is retained.

8. **A clock-minute change during one region query could produce inconsistent same-priority results.**
   - Each state resolution captures one `LocalTime` snapshot and uses it for all applicable regions in that resolution.

9. **Flag registration conflicts must still fail safely.**
   - `fl-real-time-switch` is included in the existing preflight type check before any flag registration proceeds.

## Existing hardening retained

- Effect exit cleanup preserves pre-existing effects when safe.
- Rollback checks the expected post-operation state before restoring a block.
- Multi-block placement records each replaced block state.
- Rollback-enabled block breaking suppresses drops and block XP to prevent duplication.
- Stay/AFK teleports honor item entry and exit restrictions.
- Direct movement between restricted regions compares effective region keys.
- Async chat never queries WorldGuard from the async event thread.
- Chat policy is refreshed immediately after successful region crossing and periodically as a fallback.
- AFK activity includes inventory interaction, held-slot changes, drops, and hand swaps.
- Item restrictions do not use escrow storage.
- `keepInventory` cannot be used to carry items out of an effective item-exit restriction through death.
- Storage protection is checked at both interaction and inventory-open layers.
- Custom flag names/types are preflighted before registration.

## Test coverage

Existing parser tests remain in place. Version 1.1.0 adds real-time rule tests for:

- exact start and end boundaries
- minute-precision schedules
- overnight schedules
- whole-hour shorthand
- `24:00` end-of-day handling
- malformed and zero-length windows

## Verification boundary

GitHub Actions builds the final source against the real Spigot/WorldGuard/WorldEdit dependencies, runs the JUnit suite, verifies the release JAR and Java class version, and checks that dependency classes are not shaded into the plugin.

A real Minecraft client connected to the user's live server is not available in CI, so live player-driven end-to-end testing is not claimed.
