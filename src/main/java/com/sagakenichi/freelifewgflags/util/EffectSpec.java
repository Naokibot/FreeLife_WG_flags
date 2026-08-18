package com.sagakenichi.freelifewgflags.util;

import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EffectSpec {

    private final List<Entry> entries;

    private EffectSpec(List<Entry> entries) {
        this.entries = List.copyOf(entries);
    }

    @SuppressWarnings("deprecation")
    public static EffectSpec parse(String input) {
        if (input == null || input.isBlank()) {
            return new EffectSpec(List.of());
        }
        List<Entry> entries = new ArrayList<>();
        for (String raw : input.split("[,;]")) {
            String[] parts = raw.trim().split("[:=]", 2);
            if (parts.length == 0 || parts[0].isBlank()) {
                continue;
            }
            PotionEffectType type = PotionEffectType.getByName(parts[0].trim().toUpperCase(Locale.ROOT));
            if (type == null) {
                continue;
            }
            int level = 1;
            if (parts.length == 2) {
                try {
                    level = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ignored) {
                    continue;
                }
            }
            if (level < 1 || level > 256) {
                continue;
            }
            entries.add(new Entry(type, level - 1));
        }
        return new EffectSpec(entries);
    }

    public List<Entry> entries() {
        return entries;
    }

    public record Entry(PotionEffectType type, int amplifier) {
    }
}
