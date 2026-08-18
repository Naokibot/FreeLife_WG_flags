package com.sagakenichi.freelifewgflags.listener;

import com.sagakenichi.freelifewgflags.RegionAccess;
import com.sagakenichi.freelifewgflags.service.ActivityTracker;
import com.sagakenichi.freelifewgflags.service.ItemBoundaryService;
import com.sagakenichi.freelifewgflags.service.PlayerStateService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class RegionMovementListener implements Listener {

    private final RegionAccess regions;
    private final ItemBoundaryService itemBoundary;
    private final ActivityTracker activity;
    private final PlayerStateService stateService;

    public RegionMovementListener(
            RegionAccess regions,
            ItemBoundaryService itemBoundary,
            ActivityTracker activity,
            PlayerStateService stateService
    ) {
        this.regions = regions;
        this.itemBoundary = itemBoundary;
        this.activity = activity;
        this.stateService = stateService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event instanceof PlayerTeleportEvent) {
            return;
        }
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        Player player = event.getPlayer();
        activity.touchIfMoved(player, event.getFrom(), to);
        if (sameBlock(event.getFrom(), to)) {
            return;
        }
        if (!itemBoundary.canCross(player, event.getFrom(), to, true)) {
            event.setCancelled(true);
            return;
        }
        sendEntryMessages(player, event.getFrom(), to);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        Player player = event.getPlayer();
        activity.touch(player);
        if (!itemBoundary.canCross(player, event.getFrom(), to, true)) {
            event.setCancelled(true);
            return;
        }
        sendEntryMessages(player, event.getFrom(), to);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        stateService.joined(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stateService.quit(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        activity.touch(event.getPlayer());
    }

    private void sendEntryMessages(Player player, Location from, Location to) {
        for (String message : regions.entryMessages(from, to)) {
            player.sendMessage(message);
        }
    }

    private static boolean sameBlock(Location first, Location second) {
        return first.getWorld() == second.getWorld()
                && first.getBlockX() == second.getBlockX()
                && first.getBlockY() == second.getBlockY()
                && first.getBlockZ() == second.getBlockZ();
    }
}
