package com.sagakenichi.freelifewgflags.listener;

import com.sagakenichi.freelifewgflags.FreeLifeFlags;
import com.sagakenichi.freelifewgflags.MessageService;
import com.sagakenichi.freelifewgflags.RegionAccess;
import com.sagakenichi.freelifewgflags.service.ActivityTracker;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Set;

public final class InteractionListener implements Listener {

    private static final Set<String> FARMING_PLANT_ITEMS = Set.of(
            "WHEAT_SEEDS",
            "BEETROOT_SEEDS",
            "MELON_SEEDS",
            "PUMPKIN_SEEDS",
            "TORCHFLOWER_SEEDS",
            "PITCHER_POD",
            "CARROT",
            "POTATO",
            "NETHER_WART",
            "COCOA_BEANS",
            "SUGAR_CANE",
            "CACTUS",
            "BAMBOO",
            "KELP",
            "SWEET_BERRIES",
            "GLOW_BERRIES"
    );

    private final FreeLifeFlags flags;
    private final RegionAccess regions;
    private final MessageService messages;
    private final ActivityTracker activity;

    public InteractionListener(
            FreeLifeFlags flags,
            RegionAccess regions,
            MessageService messages,
            ActivityTracker activity
    ) {
        this.flags = flags;
        this.regions = regions;
        this.messages = messages;
        this.activity = activity;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVillagerInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        activity.touch(player);
        if (!(event.getRightClicked() instanceof AbstractVillager)) {
            return;
        }
        if (regions.isDenied(event.getRightClicked().getLocation(), player, flags.villagerTrade)) {
            event.setCancelled(true);
            messages.sendThrottled(player, "villager-trade-denied");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        activity.touch(player);
        ItemStack item = event.getItem();
        if (item != null && isUseAction(event.getAction())) {
            if (item.getType() == Material.WIND_CHARGE
                    && regions.isDenied(player.getLocation(), player, flags.windCharge)) {
                event.setCancelled(true);
                messages.sendThrottled(player, "wind-charge-denied");
                return;
            }
            if (item.getType() == Material.ENDER_PEARL
                    && regions.isDenied(player.getLocation(), player, flags.enderPearl)) {
                event.setCancelled(true);
                messages.sendThrottled(player, "ender-pearl-denied");
                return;
            }
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                    && event.getClickedBlock() != null
                    && regions.isAllowed(event.getClickedBlock().getLocation(), player, flags.onlyWheatSeeds)
                    && FARMING_PLANT_ITEMS.contains(item.getType().name())
                    && item.getType() != Material.WHEAT_SEEDS) {
                event.setCancelled(true);
                messages.sendThrottled(player, "planting-denied");
                return;
            }
        }

        Block clicked = event.getClickedBlock();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || clicked == null
                || !regions.isAllowed(clicked.getLocation(), player, flags.storageProtection)) {
            return;
        }
        if (isProtectedStorage(clicked.getType())) {
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setCancelled(true);
            messages.sendThrottled(player, "storage-denied");
        } else if (isPublicUtility(clicked.getType())) {
            event.setUseInteractedBlock(Event.Result.ALLOW);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != Material.CHORUS_FRUIT) {
            return;
        }
        Player player = event.getPlayer();
        activity.touch(player);
        if (regions.isDenied(player.getLocation(), player, flags.chorusFruit)) {
            event.setCancelled(true);
            messages.sendThrottled(player, "chorus-fruit-denied");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource shooter = projectile.getShooter();
        if (!(shooter instanceof Player player)) {
            return;
        }
        activity.touch(player);
        String type = projectile.getType().name();
        if (type.equals("WIND_CHARGE") && regions.isDenied(player.getLocation(), player, flags.windCharge)) {
            event.setCancelled(true);
            messages.sendThrottled(player, "wind-charge-denied");
        } else if (type.equals("ENDER_PEARL") && regions.isDenied(player.getLocation(), player, flags.enderPearl)) {
            event.setCancelled(true);
            messages.sendThrottled(player, "ender-pearl-denied");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpecialTeleport(PlayerTeleportEvent event) {
        String cause = event.getCause().name();
        Player player = event.getPlayer();
        Location source = event.getFrom();
        if (cause.equals("ENDER_PEARL") && regions.isDenied(source, player, flags.enderPearl)) {
            event.setCancelled(true);
            messages.sendThrottled(player, "ender-pearl-denied");
        } else if (cause.equals("CHORUS_FRUIT") && regions.isDenied(source, player, flags.chorusFruit)) {
            event.setCancelled(true);
            messages.sendThrottled(player, "chorus-fruit-denied");
        }
    }

    public static boolean isProtectedStorage(Material material) {
        String name = material.name();
        return material == Material.CHEST
                || material == Material.TRAPPED_CHEST
                || material == Material.BARREL
                || material == Material.HOPPER
                || name.equals("SHULKER_BOX")
                || name.endsWith("_SHULKER_BOX");
    }

    private static boolean isPublicUtility(Material material) {
        String name = material.name();
        return material == Material.LEVER
                || name.endsWith("_DOOR")
                || name.endsWith("_BUTTON")
                || name.endsWith("_BED");
    }

    private static boolean isUseAction(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }
}
