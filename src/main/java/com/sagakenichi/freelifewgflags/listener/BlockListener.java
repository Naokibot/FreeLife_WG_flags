package com.sagakenichi.freelifewgflags.listener;

import com.sagakenichi.freelifewgflags.FreeLifeFlags;
import com.sagakenichi.freelifewgflags.MessageService;
import com.sagakenichi.freelifewgflags.RegionAccess;
import com.sagakenichi.freelifewgflags.service.BlockRollbackService;
import com.sagakenichi.freelifewgflags.util.MaterialRules;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;
import java.util.Set;

public final class BlockListener implements Listener {

    private static final Set<String> FARM_PLANTS = Set.of(
            "WHEAT",
            "BEETROOTS",
            "CARROTS",
            "POTATOES",
            "MELON_STEM",
            "PUMPKIN_STEM",
            "TORCHFLOWER_CROP",
            "PITCHER_CROP",
            "NETHER_WART",
            "COCOA",
            "SUGAR_CANE",
            "CACTUS",
            "BAMBOO",
            "KELP",
            "SWEET_BERRY_BUSH",
            "CAVE_VINES",
            "CAVE_VINES_PLANT"
    );

    private final FreeLifeFlags flags;
    private final RegionAccess regions;
    private final MessageService messages;
    private final BlockRollbackService rollback;

    public BlockListener(
            FreeLifeFlags flags,
            RegionAccess regions,
            MessageService messages,
            BlockRollbackService rollback
    ) {
        this.flags = flags;
        this.regions = regions;
        this.messages = messages;
        this.rollback = rollback;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        List<BlockState> replaced = event instanceof BlockMultiPlaceEvent multi
                ? multi.getReplacedBlockStates()
                : List.of(event.getBlockReplacedState());

        for (BlockState previous : replaced) {
            Block placed = previous.getWorld().getBlockAt(previous.getX(), previous.getY(), previous.getZ());
            if (regions.isAllowed(placed.getLocation(), player, flags.onlyWheatSeeds)
                    && FARM_PLANTS.contains(placed.getType().name())
                    && placed.getType() != Material.WHEAT) {
                event.setCancelled(true);
                messages.send(player, "planting-denied");
                return;
            }

            var allowed = regions.configuredRegion(placed.getLocation(), flags.placeBlocks);
            if (allowed.isPresent() && !MaterialRules.parse(allowed.get().value()).allows(placed.getType())) {
                event.setCancelled(true);
                messages.send(player, "block-place-denied");
                return;
            }
        }

        for (BlockState previous : replaced) {
            Block placed = previous.getWorld().getBlockAt(previous.getX(), previous.getY(), previous.getZ());
            Integer seconds = regions.configuredRegion(placed.getLocation(), flags.blockRollbackSeconds)
                    .map(RegionAccess.ConfiguredRegion::value)
                    .orElse(null);
            BlockData expected = placed.getBlockData().clone();
            rollback.schedule(previous, placed.getType(), expected, seconds);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        var allowed = regions.configuredRegion(block.getLocation(), flags.breakBlocks);
        if (allowed.isPresent() && !MaterialRules.parse(allowed.get().value()).allows(block.getType())) {
            event.setCancelled(true);
            messages.send(player, "block-break-denied");
            return;
        }

        Integer seconds = regions.configuredRegion(block.getLocation(), flags.blockRollbackSeconds)
                .map(RegionAccess.ConfiguredRegion::value)
                .orElse(null);
        if (rollback.isEnabled(seconds)) {
            event.setDropItems(false);
            event.setExpToDrop(0);
        }
        BlockState previous = block.getState();
        rollback.schedule(previous, Material.AIR, null, seconds);
    }
}
