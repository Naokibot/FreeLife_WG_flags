package com.sagakenichi.freelifewgflags.listener;

import com.sagakenichi.freelifewgflags.service.ActivityTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public final class ActivityListener implements Listener {

    private final ActivityTracker activity;

    public ActivityListener(ActivityTracker activity) {
        this.activity = activity;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            activity.touch(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            activity.touch(player);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        activity.touch(event.getPlayer());
    }

    @EventHandler
    public void onHeldSlot(PlayerItemHeldEvent event) {
        activity.touch(event.getPlayer());
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        activity.touch(event.getPlayer());
    }
}
