package com.sagakenichi.freelifewgflags.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetSpecTest {

    @Test
    void parsesFourAndSixFieldTargets() {
        TargetSpec simple = TargetSpec.parse("world;0.5;64;10.5").orElseThrow();
        assertEquals("world", simple.world());
        assertEquals(0.0F, simple.yaw());

        TargetSpec full = TargetSpec.parse("hub;1;70;2;90;15").orElseThrow();
        assertEquals(90.0F, full.yaw());
        assertEquals(15.0F, full.pitch());
    }

    @Test
    void rejectsInvalidCoordinates() {
        assertTrue(TargetSpec.parse("world;NaN;64;0").isEmpty());
        assertTrue(TargetSpec.parse("world;0;64").isEmpty());
    }
}
