package com.sagakenichi.freelifewgflags.service;

import com.sagakenichi.freelifewgflags.FreeLifeFlags;
import com.sagakenichi.freelifewgflags.MessageService;
import com.sagakenichi.freelifewgflags.RegionAccess;
import com.sagakenichi.freelifewgflags.util.TargetSpec;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class PlayerStateService {

    private static final int MAX_SECONDS = 31_536_000;

    private final JavaPlugin plugin;
    private final FreeLifeFlags flags;
    private final RegionAccess regions;
    private final MessageService messages;
    private final ItemBoundaryService itemBoundary;
    private final ActivityTracker activity;
    private final ChatPolicyCache chatPolicies;
    private final EffectService effects;
    private final Map<UUID, StaySession> staySessions = new HashMap<>();
    private final Set<String> warnedTargets = new HashSet<>();

    public PlayerStateService(
            JavaPlugin plugin,
            FreeLifeFlags flags,
            RegionAccess regions,
            MessageService messages,
            ItemBoundaryService itemBoundary,
            ActivityTracker activity,
            ChatPolicyCache chatPolicies,
            EffectService effects
    ) {
        this.plugin = plugin;
        this.flags = flags;
        this.regions = regions;
        this.messages = messages;
        this.itemBoundary = itemBoundary;
        this.activity = activity;
        this.chatPolicies = chatPolicies;
        this.effects = effects;
    }

    public void tick() {
        long now = System.currentTimeMillis();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            chatPolicies.refresh(player);
            effects.tick(player);
            handleStayTimer(player, now);
            handleAfk(player);
        }
    }

    public void joined(Player player) {
        activity.touch(player);
        chatPolicies.refresh(player);
    }

    public void quit(Player player) {
        staySessions.remove(player.getUniqueId());
        chatPolicies.forget(player);
        activity.forget(player);
        effects.forget(player);
    }

    public void shutdown() {
        effects.shutdown(plugin.getServer().getOnlinePlayers());
        staySessions.clear();
    }

    private void handleStayTimer(Player player, long now) {
        Optional<RegionAccess.ConfiguredPair<Integer, String>> policy =
                regions.configuredPair(player.getLocation(), flags.staySeconds, flags.stayTeleport);
        if (policy.isEmpty() || !validSeconds(policy.get().first())) {
            staySessions.remove(player.getUniqueId());
            return;
        }

        StaySession session = staySessions.get(player.getUniqueId());
        if (session == null || !session.regionKey().equals(policy.get().key())) {
            staySessions.put(player.getUniqueId(), new StaySession(policy.get().key(), now));
            return;
        }

        long requiredMillis = policy.get().first() * 1000L;
        if (now - session.enteredAt() < requiredMillis) {
            return;
        }

        if (teleport(player, policy.get().second())) {
            staySessions.remove(player.getUniqueId());
            activity.touch(player);
        }
    }

    private void handleAfk(Player player) {
        Optional<RegionAccess.ConfiguredPair<Integer, String>> policy =
                regions.configuredPair(player.getLocation(), flags.afkSeconds, flags.afkTeleport);
        if (policy.isEmpty() || !validSeconds(policy.get().first())) {
            return;
        }
        if (activity.idleMillis(player) < policy.get().first() * 1000L) {
            return;
        }
        if (teleport(player, policy.get().second())) {
            activity.touch(player);
            staySessions.remove(player.getUniqueId());
        }
    }

    private boolean teleport(Player player, String rawTarget) {
        Optional<TargetSpec> parsed = TargetSpec.parse(rawTarget);
        if (parsed.isEmpty()) {
            warnTarget(rawTarget, "invalid target format");
            return false;
        }
        Optional<Location> destination = parsed.get().resolve();
        if (destination.isEmpty()) {
            warnTarget(rawTarget, "target world is not loaded");
            return false;
        }
        if (!itemBoundary.canCross(player, player.getLocation(), destination.get(), true)) {
            messages.sendThrottled(player, "teleport-blocked-items");
            return false;
        }
        return player.teleport(destination.get());
    }

    private void warnTarget(String target, String reason) {
        String key = reason + "\u0000" + target;
        if (warnedTargets.add(key)) {
            plugin.getLogger().warning("Ignoring teleport target '" + target + "': " + reason + ".");
        }
    }

    private static boolean validSeconds(Integer seconds) {
        return seconds != null && seconds > 0 && seconds <= MAX_SECONDS;
    }

    private record StaySession(String regionKey, long enteredAt) {
    }
}
