package com.sagakenichi.freelifewgflags.service;

import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sagakenichi.freelifewgflags.FreeLifeFlags;
import com.sagakenichi.freelifewgflags.RegionAccess;
import com.sagakenichi.freelifewgflags.util.EffectSpec;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class EffectService {

    private static final int REFRESH_DURATION_TICKS = 60;

    private final FreeLifeFlags flags;
    private final RegionAccess regions;
    private final Map<UUID, Session> sessions = new HashMap<>();

    public EffectService(FreeLifeFlags flags, RegionAccess regions) {
        this.flags = flags;
        this.regions = regions;
    }

    public void tick(Player player) {
        var configured = regions.configuredRegion(player.getLocation(), flags.effects);
        Session previous = sessions.get(player.getUniqueId());

        if (configured.isEmpty()) {
            finish(player, previous, previous != null && previous.removeOnExit());
            return;
        }

        String rawSpec = configured.get().value();
        boolean removeOnExit = regions.stateOnRegion(
                configured.get().region(),
                player.getWorld().getTime(),
                flags.removeEffectsOnExit
        ) == StateFlag.State.ALLOW;

        if (previous == null
                || !previous.regionKey().equals(configured.get().key())
                || !previous.rawSpec().equals(rawSpec)) {
            Session old = previous;
            finish(player, old, old != null && old.removeOnExit());
            previous = createSession(player, configured.get().key(), rawSpec, removeOnExit, old);
            sessions.put(player.getUniqueId(), previous);
        } else if (previous.removeOnExit() != removeOnExit) {
            previous = previous.withRemoveOnExit(removeOnExit);
            sessions.put(player.getUniqueId(), previous);
        }

        refresh(player, previous);
    }

    public void forget(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public void shutdown(Iterable<? extends Player> players) {
        for (Player player : players) {
            Session session = sessions.get(player.getUniqueId());
            if (session != null && session.removeOnExit()) {
                cleanup(player, session);
            }
        }
        sessions.clear();
    }

    private Session createSession(Player player, String regionKey, String rawSpec, boolean removeOnExit, Session old) {
        EffectSpec spec = EffectSpec.parse(rawSpec);
        Map<PotionEffectType, PotionEffect> previousEffects = new HashMap<>();
        Map<PotionEffectType, Integer> amplifiers = new HashMap<>();

        for (EffectSpec.Entry entry : spec.entries()) {
            PotionEffect current = player.getPotionEffect(entry.type());
            if (current != null && !wasAppliedBy(old, entry.type(), current)) {
                previousEffects.put(entry.type(), current);
            }
            amplifiers.put(entry.type(), entry.amplifier());
        }
        return new Session(regionKey, rawSpec, removeOnExit, previousEffects, amplifiers);
    }

    private void refresh(Player player, Session session) {
        for (Map.Entry<PotionEffectType, Integer> entry : session.amplifiers().entrySet()) {
            player.addPotionEffect(new PotionEffect(
                    entry.getKey(),
                    REFRESH_DURATION_TICKS,
                    entry.getValue(),
                    true,
                    false,
                    true
            ));
        }
    }

    private void finish(Player player, Session session, boolean cleanup) {
        if (session == null) {
            return;
        }
        sessions.remove(player.getUniqueId());
        if (cleanup) {
            cleanup(player, session);
        }
    }

    private void cleanup(Player player, Session session) {
        for (Map.Entry<PotionEffectType, Integer> entry : session.amplifiers().entrySet()) {
            PotionEffect current = player.getPotionEffect(entry.getKey());
            if (current == null || current.getAmplifier() != entry.getValue() || current.getDuration() > 90) {
                continue;
            }
            player.removePotionEffect(entry.getKey());
            PotionEffect previous = session.previousEffects().get(entry.getKey());
            if (previous != null) {
                player.addPotionEffect(previous);
            }
        }
    }

    private static boolean wasAppliedBy(Session old, PotionEffectType type, PotionEffect current) {
        if (old == null) {
            return false;
        }
        Integer amplifier = old.amplifiers().get(type);
        return amplifier != null && amplifier == current.getAmplifier() && current.getDuration() <= 90;
    }

    private record Session(
            String regionKey,
            String rawSpec,
            boolean removeOnExit,
            Map<PotionEffectType, PotionEffect> previousEffects,
            Map<PotionEffectType, Integer> amplifiers
    ) {
        Session {
            previousEffects = Map.copyOf(previousEffects);
            amplifiers = Map.copyOf(amplifiers);
        }

        Session withRemoveOnExit(boolean value) {
            return new Session(regionKey, rawSpec, value, previousEffects, amplifiers);
        }
    }
}
