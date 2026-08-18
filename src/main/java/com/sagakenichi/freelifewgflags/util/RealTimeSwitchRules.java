package com.sagakenichi.freelifewgflags.util;

import com.sk89q.worldguard.protection.flags.StateFlag;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RealTimeSwitchRules {

    private static final Pattern WINDOW = Pattern.compile(
            "^\\s*([0-9]{1,2}(?::[0-9]{2})?)\\s*-\\s*([0-9]{1,2}(?::[0-9]{2})?)\\s*:(.+)$"
    );

    private final List<Window> windows;

    private RealTimeSwitchRules(List<Window> windows) {
        this.windows = List.copyOf(windows);
    }

    public static RealTimeSwitchRules parse(String input) {
        if (input == null || input.isBlank()) {
            return new RealTimeSwitchRules(List.of());
        }

        List<Window> windows = new ArrayList<>();
        for (String rawWindow : input.split("\\|")) {
            Matcher matcher = WINDOW.matcher(rawWindow);
            if (!matcher.matches()) {
                continue;
            }

            Integer start = parseMinute(matcher.group(1), false);
            Integer end = parseMinute(matcher.group(2), true);
            if (start == null || end == null || start.equals(end)) {
                continue;
            }

            List<Assignment> assignments = parseAssignments(matcher.group(3));
            if (!assignments.isEmpty()) {
                windows.add(new Window(start, end, List.copyOf(assignments)));
            }
        }
        return new RealTimeSwitchRules(windows);
    }

    public Optional<StateFlag.State> override(String flagName, LocalTime time) {
        if (flagName == null || time == null) {
            return Optional.empty();
        }

        int minute = time.getHour() * 60 + time.getMinute();
        String normalized = flagName.toLowerCase(Locale.ROOT);
        for (Window window : windows) {
            if (!window.contains(minute)) {
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

    private static List<Assignment> parseAssignments(String input) {
        List<Assignment> assignments = new ArrayList<>();
        for (String rawAssignment : input.split(",")) {
            String[] parts = rawAssignment.trim().split("=", 2);
            if (parts.length != 2 || parts[0].isBlank()) {
                continue;
            }
            StateFlag.State state = parseState(parts[1]);
            if (state != null) {
                assignments.add(new Assignment(parts[0].trim().toLowerCase(Locale.ROOT), state));
            }
        }
        return assignments;
    }

    private static Integer parseMinute(String raw, boolean end) {
        String value = raw.trim();
        String[] parts = value.split(":", -1);
        if (parts.length > 2) {
            return null;
        }

        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = parts.length == 2 ? Integer.parseInt(parts[1]) : 0;
            if (minute < 0 || minute > 59) {
                return null;
            }
            if (hour == 24 && minute == 0 && end) {
                return 1440;
            }
            if (hour < 0 || hour > 23) {
                return null;
            }
            return hour * 60 + minute;
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
        boolean contains(int minute) {
            if (end == 1440) {
                return minute >= start;
            }
            if (start < end) {
                return minute >= start && minute < end;
            }
            return minute >= start || minute < end;
        }
    }
}
