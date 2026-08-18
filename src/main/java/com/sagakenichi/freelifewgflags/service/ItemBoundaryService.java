package com.sagakenichi.freelifewgflags.service;

import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sagakenichi.freelifewgflags.FreeLifeFlags;
import com.sagakenichi.freelifewgflags.MessageService;
import com.sagakenichi.freelifewgflags.RegionAccess;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;

public final class ItemBoundaryService {

    private final FreeLifeFlags flags;
    private final RegionAccess regions;
    private final MessageService messages;

    public ItemBoundaryService(FreeLifeFlags flags, RegionAccess regions, MessageService messages) {
        this.flags = flags;
        this.regions = regions;
        this.messages = messages;
    }

    public boolean canCross(Player player, Location from, Location to, boolean notify) {
        if (!hasCarriedItems(player)) {
            return true;
        }

        Set<String> fromExitDenied = regions.stateRegionKeys(from, flags.itemExit, StateFlag.State.DENY);
        Set<String> toExitDenied = regions.stateRegionKeys(to, flags.itemExit, StateFlag.State.DENY);
        Set<String> exited = difference(fromExitDenied, toExitDenied);
        if (!exited.isEmpty()) {
            if (notify) {
                messages.sendThrottled(player, "item-exit-denied");
            }
            return false;
        }

        Set<String> fromEntryDenied = regions.stateRegionKeys(from, flags.itemEntry, StateFlag.State.DENY);
        Set<String> toEntryDenied = regions.stateRegionKeys(to, flags.itemEntry, StateFlag.State.DENY);
        Set<String> entered = difference(toEntryDenied, fromEntryDenied);
        if (!entered.isEmpty()) {
            if (notify) {
                messages.sendThrottled(player, "item-entry-denied");
            }
            return false;
        }

        return true;
    }

    public boolean isExitRestricted(Location location) {
        return !regions.stateRegionKeys(location, flags.itemExit, StateFlag.State.DENY).isEmpty();
    }

    public boolean hasCarriedItems(Player player) {
        PlayerInventory inventory = player.getInventory();
        if (containsItem(inventory.getStorageContents())) {
            return true;
        }
        if (containsItem(inventory.getArmorContents())) {
            return true;
        }
        ItemStack offHand = inventory.getItemInOffHand();
        return offHand != null && !offHand.getType().isAir();
    }

    private static boolean containsItem(ItemStack[] items) {
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> difference(Set<String> left, Set<String> right) {
        Set<String> result = new HashSet<>(left);
        result.removeAll(right);
        return result;
    }
}
