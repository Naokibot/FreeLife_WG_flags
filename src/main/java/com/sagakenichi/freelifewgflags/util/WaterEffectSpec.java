package com.sagakenichi.freelifewgflags.util;

import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WaterEffectSpec {

    private static final int MAX_LEVEL = 256;
    private static final int MAX_DURATION_SECONDS = 86_400;

    private final List<Entry> entries;

    private WaterEffectSpec(List<Entry> entries) {
        this.entries = List.copyOf(entries);
    }

    @SuppressWarnings("deprecation")
    public static WaterEffectSpec parse(String input) {
        if (input == null || input.isBlank()) {
            return new WaterEffectSpec(List.of());
        }

        List<Entry> entries = new ArrayList<>();
        for (String raw : input.split("[,;]")) {
            String[] parts = raw.trim().split(":", -1);
            if (parts.length != 3 || parts[0].isBlank()) {
                continue;
            }

            PotionEffectType type = PotionEffectType.getByName(parts[0].trim().toUpperCase(Locale.ROOT));
            if (type == null) {
                continue;
            }

            Integer level = parsePositive(parts[1], MAX_LEVEL);
            Integer seconds = parsePositive(parts[2], MAX_DURATION_SECONDS);
            if (level == null || seconds == null) {
                continue;
            }

            entries.add(new Entry(type, level - 1, seconds * 20));
        }
        return new WaterEffectSpec(entries);
    }

    public List<Entry> entries() {
        return entries;
    }

    private static Integer parsePositive(String raw, int maximum) {
        try {
            int value = Integer.parseInt(raw.trim());
            return value >= 1 && value <= maximum ? value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public record Entry(PotionEffectType type, int amplifier, int durationTicks) {
    }
}
