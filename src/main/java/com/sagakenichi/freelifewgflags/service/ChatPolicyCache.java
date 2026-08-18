package com.sagakenichi.freelifewgflags.service;

import com.sagakenichi.freelifewgflags.FreeLifeFlags;
import com.sagakenichi.freelifewgflags.RegionAccess;
import com.sagakenichi.freelifewgflags.util.RuleList;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ChatPolicyCache {

    private final FreeLifeFlags flags;
    private final RegionAccess regions;
    private final ConcurrentMap<UUID, RuleList> policies = new ConcurrentHashMap<>();

    public ChatPolicyCache(FreeLifeFlags flags, RegionAccess regions) {
        this.flags = flags;
        this.regions = regions;
    }

    public void refresh(Player player) {
        refresh(player, player.getLocation());
    }

    public void refresh(Player player, Location location) {
        regions.configuredRegion(location, flags.chatAllowed)
                .ifPresentOrElse(
                        configured -> policies.put(player.getUniqueId(), RuleList.parse(configured.value())),
                        () -> policies.remove(player.getUniqueId())
                );
    }

    public boolean permits(Player player, String message) {
        RuleList policy = policies.get(player.getUniqueId());
        return policy == null || policy.permitsChat(message);
    }

    public void forget(Player player) {
        policies.remove(player.getUniqueId());
    }
}
