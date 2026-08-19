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
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WaterEffectService {

    private final JavaPlugin plugin;
    private final FreeLifeFlags flags;
    private final RegionAccess regions;
    private final Map<String, List<ResolvedEffect>> resolvedSpecs = new HashMap<>();

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

            List<ResolvedEffect> effects = resolvedSpecs.computeIfAbsent(
                    configured.get().value(),
                    this::resolve
            );
            for (ResolvedEffect effect : effects) {
                player.addPotionEffect(new PotionEffect(
                        effect.type(),
                        effect.durationTicks(),
                        effect.amplifier(),
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

    @SuppressWarnings("deprecation")
    private List<ResolvedEffect> resolve(String raw) {
        WaterEffectSpec spec = WaterEffectSpec.parse(raw);
        List<ResolvedEffect> resolved = new ArrayList<>();
        for (WaterEffectSpec.Entry entry : spec.entries()) {
            PotionEffectType type = PotionEffectType.getByName(entry.effectName());
            if (type == null) {
                continue;
            }
            resolved.add(new ResolvedEffect(type, entry.amplifier(), entry.durationTicks()));
        }
        return List.copyOf(resolved);
    }

    private static boolean waterAt(Location location) {
        Material material = location.getBlock().getType();
        if (material == Material.WATER || material == Material.BUBBLE_COLUMN) {
            return true;
        }
        BlockData data = location.getBlock().getBlockData();
        return data instanceof Waterlogged waterlogged && waterlogged.isWaterlogged();
    }

    private record ResolvedEffect(PotionEffectType type, int amplifier, int durationTicks) {
    }
}
