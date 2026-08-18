package com.sagakenichi.freelifewgflags.util;

import com.sk89q.worldguard.protection.flags.StateFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class TimeSwitchRules {

    private final List<Window> windows;

    private TimeSwitchRules(List<Window> windows) {
        this.windows = List.copyOf(windows);
    }

    public static TimeSwitchRules parse(String input) {
        if (input == null || input.isBlank()) {
            return new TimeSwitchRules(List.of());
        }

        List<Window> windows = new ArrayList<>();
        for (String rawWindow : input.split("\\|")) {
            int separator = rawWindow.indexOf(':');
            if (separator <= 0 || separator == rawWindow.length() - 1) {
                continue;
            }

            String[] range = rawWindow.substring(0, separator).trim().split("-", 2);
            if (range.length != 2) {
                continue;
            }

            Integer start = parseTime(range[0]);
            Integer end = parseTime(range[1]);
            if (start == null || end == null) {
                continue;
            }

            List<Assignment> assignments = new ArrayList<>();
            for (String rawAssignment : rawWindow.substring(separator + 1).split(",")) {
                String[] parts = rawAssignment.trim().split("=", 2);
                if (parts.length != 2 || parts[0].isBlank()) {
                    continue;
                }
                StateFlag.State state = parseState(parts[1]);
                if (state != null) {
                    assignments.add(new Assignment(parts[0].trim().toLowerCase(Locale.ROOT), state));
                }
            }

            if (!assignments.isEmpty()) {
                windows.add(new Window(start, end, List.copyOf(assignments)));
            }
        }
        return new TimeSwitchRules(windows);
    }

    public Optional<StateFlag.State> override(String flagName, long worldTime) {
        if (flagName == null) {
            return Optional.empty();
        }
        int time = (int) Math.floorMod(worldTime, 24000L);
        String normalized = flagName.toLowerCase(Locale.ROOT);
        for (Window window : windows) {
            if (!window.contains(time)) {
                continue;
            }
            for (Assignment assignment : window.assignments()) {
                if (assignment.flagName().equals(normalized)) {
                    return Optional.of(assignment.state());
                }
            }
        }
        return Optional.empty();
    }

    private static Integer parseTime(String raw) {
        try {
            int value = Integer.parseInt(raw.trim());
            return value >= 0 && value <= 23999 ? value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static StateFlag.State parseState(String raw) {
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "allow" -> StateFlag.State.ALLOW;
            case "deny" -> StateFlag.State.DENY;
            default -> null;
        };
    }

    private record Assignment(String flagName, StateFlag.State state) {
    }

    private record Window(int start, int end, List<Assignment> assignments) {
        boolean contains(int time) {
            if (start <= end) {
                return time >= start && time <= end;
            }
            return time >= start || time <= end;
        }
    }
}
