package com.sagakenichi.freelifewgflags.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleListTest {

    @Test
    void commandRuleWithoutArgumentsMatchesCommandLabel() {
        RuleList rules = RuleList.parse("spawn,home,warp shop*");

        assertTrue(rules.permitsCommand("/spawn"));
        assertTrue(rules.permitsCommand("/home bed"));
        assertTrue(rules.permitsCommand("/warp shop"));
        assertTrue(rules.permitsCommand("/warp shop2"));
        assertFalse(rules.permitsCommand("/warp mine"));
    }

    @Test
    void chatSupportsExactAndPrefixRules() {
        RuleList rules = RuleList.parse("hello,trade *");

        assertTrue(rules.permitsChat("hello"));
        assertTrue(rules.permitsChat("trade diamond"));
        assertFalse(rules.permitsChat("hello there"));
    }
}
