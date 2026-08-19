package com.sagakenichi.freelifewgflags.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sagakenichi.freelifewgflags.FreeLifeFlags;
import com.sagakenichi.freelifewgflags.RegionAccess;
import com.sagakenichi.freelifewgflags.util.MaterialRules;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BuildOverrideListener implements Listener {

    private final FreeLifeFlags flags;
    private final RegionAccess regions;
    private final RegionQuery query;

    public BuildOverrideListener(FreeLifeFlags flags, RegionAccess regions) {
        this.flags = flags;
        this.regions = regions;
        this.query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onWorldGuardPlace(PlaceBlockEvent event) {
        if (!(event.getOriginalEvent() instanceof BlockPlaceEvent)) {
            return;
        }
        if (event.getExplicitResult() != Event.Result.DEFAULT) {
            return;
        }

        Player player = event.getCause().getFirstPlayer();
        if (player == null || event.getBlocks().isEmpty()) {
            return;
        }

        var localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        for (Block block : event.getBlocks()) {
            var configured = regions.configuredRegion(block.getLocation(), flags.placeBlocks);
            if (configured.isEmpty()
                    || !MaterialRules.parse(configured.get().value()).allows(event.getEffectiveMaterial())) {
                return;
            }
            StateFlag.State blockPlace = query.queryState(
                    BukkitAdapter.adapt(block.getLocation()),
                    localPlayer,
                    Flags.BLOCK_PLACE
            );
            if (blockPlace == StateFlag.State.DENY) {
                return;
            }
        }

        event.setAllowed(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onWorldGuardBreak(BreakBlockEvent event) {
        if (!(event.getOriginalEvent() instanceof BlockBreakEvent)) {
            return;
        }
        if (event.getExplicitResult() != Event.Result.DEFAULT) {
            return;
        }

        Player player = event.getCause().getFirstPlayer();
        if (player == null || event.getBlocks().isEmpty()) {
            return;
        }

        var localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        for (Block block : event.getBlocks()) {
            var configured = regions.configuredRegion(block.getLocation(), flags.breakBlocks);
            if (configured.isEmpty()
                    || !MaterialRules.parse(configured.get().value()).allows(block.getType())) {
                return;
            }
            StateFlag.State blockBreak = query.queryState(
                    BukkitAdapter.adapt(block.getLocation()),
                    localPlayer,
                    Flags.BLOCK_BREAK
            );
            if (blockBreak == StateFlag.State.DENY) {
                return;
            }
        }

        event.setAllowed(true);
    }
}
