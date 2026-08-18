package com.sagakenichi.freelifewgflags.listener;

import com.sagakenichi.freelifewgflags.FreeLifeFlags;
import com.sagakenichi.freelifewgflags.RegionAccess;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class DamageListener implements Listener {

    private final FreeLifeFlags flags;
    private final RegionAccess regions;

    public DamageListener(FreeLifeFlags flags, RegionAccess regions) {
        this.flags = flags;
        this.regions = regions;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            if (regions.isAllowed(player.getLocation(), player, flags.invincible)) {
                event.setCancelled(true);
            }
            return;
        }

        if (!isAnimal(entity)) {
            return;
        }
        if (regions.isDenied(entity.getLocation(), null, flags.animalDamage)) {
            event.setCancelled(true);
            return;
        }
        if (entity.getCustomName() != null
                && regions.isDenied(entity.getLocation(), null, flags.namedAnimalDamage)) {
            event.setCancelled(true);
        }
    }

    private static boolean isAnimal(Entity entity) {
        return entity instanceof Animals || entity instanceof WaterMob || entity instanceof Ambient;
    }
}
