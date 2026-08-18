package com.sagakenichi.freelifewgflags.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

public record TargetSpec(String world, double x, double y, double z, float yaw, float pitch) {

    public static Optional<TargetSpec> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String[] parts = input.trim().split(";", -1);
        if (parts.length != 4 && parts.length != 6) {
            return Optional.empty();
        }
        try {
            String world = parts[0].trim();
            if (world.isEmpty()) {
                return Optional.empty();
            }
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length == 6 ? Float.parseFloat(parts[4]) : 0.0F;
            float pitch = parts.length == 6 ? Float.parseFloat(parts[5]) : 0.0F;
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                    || !Float.isFinite(yaw) || !Float.isFinite(pitch)) {
                return Optional.empty();
            }
            return Optional.of(new TargetSpec(world, x, y, z, yaw, pitch));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Location> resolve() {
        World targetWorld = Bukkit.getWorld(world);
        if (targetWorld == null) {
            return Optional.empty();
        }
        return Optional.of(new Location(targetWorld, x, y, z, yaw, pitch));
    }
}
