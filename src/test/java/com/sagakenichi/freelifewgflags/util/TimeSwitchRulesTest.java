package com.sagakenichi.freelifewgflags.util;

import com.sk89q.worldguard.protection.flags.StateFlag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeSwitchRulesTest {

    @Test
    void selectsStateForDayAndNight() {
        TimeSwitchRules rules = TimeSwitchRules.parse(
                "0-11999:fl-wind-charge=allow|12000-23999:fl-wind-charge=deny"
        );

        assertEquals(StateFlag.State.ALLOW, rules.override("fl-wind-charge", 6000).orElseThrow());
        assertEquals(StateFlag.State.DENY, rules.override("fl-wind-charge", 18000).orElseThrow());
        assertTrue(rules.override("fl-invincible", 18000).isEmpty());
    }

    @Test
    void supportsRangesThatWrapMidnight() {
        TimeSwitchRules rules = TimeSwitchRules.parse("18000-1000:fl-invincible=allow");

        assertEquals(StateFlag.State.ALLOW, rules.override("fl-invincible", 23000).orElseThrow());
        assertEquals(StateFlag.State.ALLOW, rules.override("fl-invincible", 500).orElseThrow());
        assertTrue(rules.override("fl-invincible", 6000).isEmpty());
    }
}
