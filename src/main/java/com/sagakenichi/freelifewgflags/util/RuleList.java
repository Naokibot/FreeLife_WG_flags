package com.sagakenichi.freelifewgflags.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RuleList {

    private final List<String> rules;

    private RuleList(List<String> rules) {
        this.rules = List.copyOf(rules);
    }

    public static RuleList parse(String input) {
        if (input == null) {
            return new RuleList(List.of());
        }
        List<String> rules = new ArrayList<>();
        for (String raw : input.split("[,;]")) {
            String rule = raw.trim().toLowerCase(Locale.ROOT);
            if (!rule.isEmpty()) {
                rules.add(rule);
            }
        }
        return new RuleList(rules);
    }

    public boolean permitsChat(String message) {
        return permits(message == null ? "" : message.toLowerCase(Locale.ROOT), false);
    }

    public boolean permitsCommand(String commandLine) {
        String normalized = commandLine == null ? "" : commandLine.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return permits(normalized, true);
    }

    private boolean permits(String value, boolean command) {
        for (String rule : rules) {
            if (rule.equals("*")) {
                return true;
            }
            if (rule.equals("none")) {
                continue;
            }
            if (rule.endsWith("*")) {
                if (value.startsWith(rule.substring(0, rule.length() - 1))) {
                    return true;
                }
                continue;
            }
            if (command && !rule.contains(" ")) {
                int firstSpace = value.indexOf(' ');
                String label = firstSpace < 0 ? value : value.substring(0, firstSpace);
                if (label.equals(rule)) {
                    return true;
                }
            } else if (value.equals(rule)) {
                return true;
            }
        }
        return false;
    }
}
