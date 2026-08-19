package com.sagakenichi.freelifewgflags.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaterEffectSpecTest {

    @Test
    void parsesLevelAndDurationInSeconds() {
        WaterEffectSpec spec = WaterEffectSpec.parse("speed:2:10,water_breathing:1:30");

        assertEquals(2, spec.entries().size());
        assertEquals("SPEED", spec.entries().get(0).effectName());
        assertEquals(1, spec.entries().get(0).amplifier());
        assertEquals(200, spec.entries().get(0).durationTicks());
        assertEquals("WATER_BREATHING", spec.entries().get(1).effectName());
        assertEquals(0, spec.entries().get(1).amplifier());
        assertEquals(600, spec.entries().get(1).durationTicks());
    }

    @Test
    void ignoresMalformedUnsafeEntries() {
        WaterEffectSpec spec = WaterEffectSpec.parse(
                "speed:0:10,speed:1:0,speed:257:10,speed:1:86401,:1:10,speed:3:5"
        );

        assertEquals(1, spec.entries().size());
        assertEquals("SPEED", spec.entries().get(0).effectName());
        assertEquals(2, spec.entries().get(0).amplifier());
        assertEquals(100, spec.entries().get(0).durationTicks());
    }
}
