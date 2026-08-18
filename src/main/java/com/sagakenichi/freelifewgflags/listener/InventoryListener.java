package com.sagakenichi.freelifewgflags.listener;

import com.sagakenichi.freelifewgflags.FreeLifeFlags;
import com.sagakenichi.freelifewgflags.MessageService;
import com.sagakenichi.freelifewgflags.RegionAccess;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public final class InventoryListener implements Listener {

    private final FreeLifeFlags flags;
    private final RegionAccess regions;
    private final MessageService messages;

    public InventoryListener(FreeLifeFlags flags, RegionAccess regions, MessageService messages) {
        this.flags = flags;
        this.regions = regions;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (event.getInventory().getType() == InventoryType.MERCHANT
                && regions.isDenied(player.getLocation(), player, flags.villagerTrade)) {
            event.setCancelled(true);
            messages.sendThrottled(player, "villager-trade-denied");
            return;
        }
        if (!isStorage(event.getInventory().getType())) {
            return;
        }
        Location location = event.getInventory().getLocation();
        if (location == null || !regions.isAllowed(location, player, flags.storageProtection)) {
            return;
        }
        event.setCancelled(true);
        messages.sendThrottled(player, "storage-denied");
    }

    private static boolean isStorage(InventoryType type) {
        return type == InventoryType.CHEST
                || type == InventoryType.BARREL
                || type == InventoryType.HOPPER
                || type == InventoryType.SHULKER_BOX;
    }
}
