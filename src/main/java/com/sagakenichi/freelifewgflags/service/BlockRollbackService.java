package com.sagakenichi.freelifewgflags.service;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlockRollbackService {

    private static final int MAX_SECONDS = 604_800;

    private final JavaPlugin plugin;

    public BlockRollbackService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void schedule(BlockState previous, Material expectedType, BlockData expectedData, Integer seconds) {
        if (seconds == null || seconds <= 0 || seconds > MAX_SECONDS) {
            return;
        }
        long delayTicks = seconds.longValue() * 20L;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> restore(previous, expectedType, expectedData), delayTicks);
    }

    private void restore(BlockState previous, Material expectedType, BlockData expectedData) {
        Block current = previous.getWorld().getBlockAt(previous.getX(), previous.getY(), previous.getZ());
        if (current.getType() != expectedType) {
            return;
        }
        if (expectedData != null
                && !current.getBlockData().getAsString().equals(expectedData.getAsString())) {
            return;
        }
        previous.update(true, false);
    }
}
