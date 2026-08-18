package com.sagakenichi.freelifewgflags.service;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ActivityTracker {

    private final ConcurrentMap<UUID, Long> lastActive = new ConcurrentHashMap<>();

    public void touch(Player player) {
        lastActive.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void forget(Player player) {
        lastActive.remove(player.getUniqueId());
    }

    public long idleMillis(Player player) {
        return System.currentTimeMillis() - lastActive.getOrDefault(player.getUniqueId(), System.currentTimeMillis());
    }

    public void touchIfMoved(Player player, Location from, Location to) {
        if (to == null) {
            return;
        }
        boolean moved = from.getWorld() != to.getWorld()
                || from.distanceSquared(to) > 0.0004D
                || Math.abs(from.getYaw() - to.getYaw()) > 2.0F
                || Math.abs(from.getPitch() - to.getPitch()) > 2.0F;
        if (moved) {
            touch(player);
        }
    }
}
