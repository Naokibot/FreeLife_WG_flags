package com.sagakenichi.freelifewgflags.util;

import com.sk89q.worldguard.protection.flags.StateFlag;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealTimeSwitchRulesTest {

    @Test
    void switchesAtMinuteBoundaries() {
        RealTimeSwitchRules rules = RealTimeSwitchRules.parse(
                "09:00-17:00:fl-wind-charge=allow|17:00-22:00:fl-wind-charge=deny"
        );

        assertEquals(StateFlag.State.ALLOW,
                rules.override("fl-wind-charge", LocalTime.of(9, 0)).orElseThrow());
        assertEquals(StateFlag.State.ALLOW,
                rules.override("fl-wind-charge", LocalTime.of(16, 59)).orElseThrow());
        assertEquals(StateFlag.State.DENY,
                rules.override("fl-wind-charge", LocalTime.of(17, 0)).orElseThrow());
        assertTrue(rules.override("fl-wind-charge", LocalTime.of(22, 0)).isEmpty());
    }

    @Test
    void supportsOvernightWindows() {
        RealTimeSwitchRules rules = RealTimeSwitchRules.parse("22:00-06:00:fl-invincible=allow");

        assertEquals(StateFlag.State.ALLOW,
                rules.override("fl-invincible", LocalTime.of(23, 30)).orElseThrow());
        assertEquals(StateFlag.State.ALLOW,
                rules.override("fl-invincible", LocalTime.of(5, 59)).orElseThrow());
        assertTrue(rules.override("fl-invincible", LocalTime.of(6, 0)).isEmpty());
    }

    @Test
    void acceptsHourOnlySyntaxAndEndOfDay() {
        RealTimeSwitchRules rules = RealTimeSwitchRules.parse(
                "9-17:fl-ender-pearl=deny|17-24:fl-ender-pearl=allow"
        );

        assertEquals(StateFlag.State.DENY,
                rules.override("fl-ender-pearl", LocalTime.of(12, 0)).orElseThrow());
        assertEquals(StateFlag.State.ALLOW,
                rules.override("fl-ender-pearl", LocalTime.of(23, 59)).orElseThrow());
    }

    @Test
    void ignoresMalformedAndZeroLengthWindows() {
        RealTimeSwitchRules rules = RealTimeSwitchRules.parse(
                "09:00-09:00:fl-wind-charge=allow|25-26:fl-wind-charge=deny|bad"
        );

        assertTrue(rules.override("fl-wind-charge", LocalTime.NOON).isEmpty());
    }
}
