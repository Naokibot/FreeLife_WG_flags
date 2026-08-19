package com.sagakenichi.freelifewgflags.service;

import com.sagakenichi.freelifewgflags.FreeLifeFlags;
import com.sagakenichi.freelifewgflags.RegionAccess;
import com.sagakenichi.freelifewgflags.util.WaterEffectSpec;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;

public final class WaterEffectService {

    private final JavaPlugin plugin;
    private final FreeLifeFlags flags;
    private final RegionAccess regions;
    private final Map<String, WaterEffectSpec> parsedSpecs = new HashMap<>();

    public WaterEffectService(JavaPlugin plugin, FreeLifeFlags flags, RegionAccess regions) {
        this.plugin = plugin;
        this.flags = flags;
        this.regions = regions;
    }

    public void tick() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!touchingWater(player)) {
                continue;
            }

            var configured = regions.configuredRegion(player.getLocation(), flags.waterEffects);
            if (configured.isEmpty()) {
                continue;
            }

            WaterEffectSpec spec = parsedSpecs.computeIfAbsent(
                    configured.get().value(),
                    WaterEffectSpec::parse
            );
            for (WaterEffectSpec.Entry entry : spec.entries()) {
                player.addPotionEffect(new PotionEffect(
                        entry.type(),
                        entry.durationTicks(),
                        entry.amplifier(),
                        true,
                        false,
                        true
                ));
            }
        }
    }

    static boolean touchingWater(Player player) {
        if (player.isInWater()) {
            return true;
        }
        return waterAt(player.getLocation()) || waterAt(player.getEyeLocation());
    }

    private static boolean waterAt(Location location) {
        Material material = location.getBlock().getType();
        if (material == Material.WATER || material == Material.BUBBLE_COLUMN) {
            return true;
        }
        BlockData data = location.getBlock().getBlockData();
        return data instanceof Waterlogged waterlogged && waterlogged.isWaterlogged();
    }
}
