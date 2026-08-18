package com.sagakenichi.freelifewgflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sagakenichi.freelifewgflags.util.TimeSwitchRules;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RegionAccess {

    private static final Comparator<ProtectedRegion> REGION_ORDER =
            Comparator.comparingInt(ProtectedRegion::getPriority).reversed()
                    .thenComparing(ProtectedRegion::getId);

    private final FreeLifeFlags flags;
    private final RegionQuery query;
    private final ConcurrentMap<String, TimeSwitchRules> timeRules = new ConcurrentHashMap<>();

    public RegionAccess(FreeLifeFlags flags) {
        this.flags = flags;
        this.query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
    }

    public StateFlag.State state(Location location, Player player, StateFlag flag) {
        int activePriority = Integer.MIN_VALUE;
        StateFlag.State combined = null;
        long time = location.getWorld().getTime();

        for (ProtectedRegion region : sortedRegions(location)) {
            StateFlag.State candidate = stateOnRegion(region, time, flag);
            if (candidate == null) {
                continue;
            }
            if (activePriority == Integer.MIN_VALUE) {
                activePriority = region.getPriority();
            } else if (region.getPriority() < activePriority) {
                break;
            }
            combined = StateFlag.combine(combined, candidate);
        }
        return combined;
    }

    public boolean isAllowed(Location location, Player player, StateFlag flag) {
        return state(location, player, flag) == StateFlag.State.ALLOW;
    }

    public boolean isDenied(Location location, Player player, StateFlag flag) {
        return state(location, player, flag) == StateFlag.State.DENY;
    }

    public <T> Optional<ConfiguredRegion<T>> configuredRegion(Location location, Flag<T> flag) {
        return sortedRegions(location).stream()
                .map(region -> new ConfiguredRegion<>(key(location.getWorld(), region), region, region.getFlag(flag)))
                .filter(configured -> configured.value() != null)
                .findFirst();
    }

    public <A, B> Optional<ConfiguredPair<A, B>> configuredPair(Location location, Flag<A> first, Flag<B> second) {
        for (ProtectedRegion region : sortedRegions(location)) {
            A firstValue = region.getFlag(first);
            B secondValue = region.getFlag(second);
            if (firstValue != null && secondValue != null) {
                return Optional.of(new ConfiguredPair<>(
                        key(location.getWorld(), region), region, firstValue, secondValue
                ));
            }
        }
        return Optional.empty();
    }

    public Set<String> stateRegionKeys(Location location, StateFlag flag, StateFlag.State expected) {
        int activePriority = Integer.MIN_VALUE;
        StateFlag.State combined = null;
        Set<String> matching = new HashSet<>();
        long time = location.getWorld().getTime();

        for (ProtectedRegion region : sortedRegions(location)) {
            StateFlag.State candidate = stateOnRegion(region, time, flag);
            if (candidate == null) {
                continue;
            }
            if (activePriority == Integer.MIN_VALUE) {
                activePriority = region.getPriority();
            } else if (region.getPriority() < activePriority) {
                break;
            }
            combined = StateFlag.combine(combined, candidate);
            if (candidate == expected) {
                matching.add(key(location.getWorld(), region));
            }
        }
        return combined == expected ? matching : Set.of();
    }

    public StateFlag.State stateOnRegion(ProtectedRegion region, long worldTime, StateFlag flag) {
        StateFlag.State base = region.getFlag(flag);
        String rawRules = region.getFlag(flags.timeSwitch);
        if (rawRules == null) {
            return base;
        }
        return timeRules.computeIfAbsent(rawRules, TimeSwitchRules::parse)
                .override(flag.getName(), worldTime)
                .orElse(base);
    }

    public List<String> entryMessages(Location from, Location to) {
        Set<String> before = new HashSet<>();
        for (ProtectedRegion region : sortedRegions(from)) {
            before.add(key(from.getWorld(), region));
        }

        List<String> messages = new ArrayList<>();
        for (ProtectedRegion region : sortedRegions(to)) {
            if (before.contains(key(to.getWorld(), region))) {
                continue;
            }
            String message = region.getFlag(flags.entryMessage);
            if (message != null && !message.isBlank()) {
                messages.add(message);
            }
        }
        return messages;
    }

    public Set<String> regionKeys(Location location) {
        Set<String> keys = new HashSet<>();
        for (ProtectedRegion region : sortedRegions(location)) {
            keys.add(key(location.getWorld(), region));
        }
        return keys;
    }

    private List<ProtectedRegion> sortedRegions(Location location) {
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        List<ProtectedRegion> regions = new ArrayList<>(set.getRegions());
        regions.sort(REGION_ORDER);
        return regions;
    }

    private static String key(World world, ProtectedRegion region) {
        return world.getUID() + ":" + region.getId();
    }

    public record ConfiguredRegion<T>(String key, ProtectedRegion region, T value) {
    }

    public record ConfiguredPair<A, B>(String key, ProtectedRegion region, A first, B second) {
    }
}
