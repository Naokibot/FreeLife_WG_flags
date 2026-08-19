package com.sagakenichi.freelifewgflags;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import java.util.Objects;

public final class FreeLifeFlags {

    public StateFlag villagerTrade;
    public StateFlag onlyWheatSeeds;
    public StateFlag windCharge;
    public StateFlag enderPearl;
    public StateFlag chorusFruit;
    public StateFlag invincible;
    public StringFlag entryMessage;
    public StateFlag animalDamage;
    public StateFlag namedAnimalDamage;
    public StateFlag removeEffectsOnExit;
    public StringFlag effects;
    public StringFlag waterEffects;
    public StringFlag timeSwitch;
    public StringFlag realTimeSwitch;
    public IntegerFlag staySeconds;
    public StringFlag stayTeleport;
    public IntegerFlag afkSeconds;
    public StringFlag afkTeleport;
    public StateFlag itemEntry;
    public StateFlag itemExit;
    public StringFlag placeBlocks;
    public StringFlag breakBlocks;
    public IntegerFlag blockRollbackSeconds;
    public StringFlag chatAllowed;
    public StringFlag commandAllowed;
    public StateFlag storageProtection;

    public void registerAll() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        preflight(registry);

        villagerTrade = register(registry, new StateFlag("fl-villager-trade", false), StateFlag.class);
        onlyWheatSeeds = register(registry, new StateFlag("fl-only-wheat-seeds", false), StateFlag.class);
        windCharge = register(registry, new StateFlag("fl-wind-charge", false), StateFlag.class);
        enderPearl = register(registry, new StateFlag("fl-ender-pearl", false), StateFlag.class);
        chorusFruit = register(registry, new StateFlag("fl-chorus-fruit", false), StateFlag.class);
        invincible = register(registry, new StateFlag("fl-invincible", false), StateFlag.class);
        entryMessage = register(registry, new StringFlag("fl-entry-message"), StringFlag.class);
        animalDamage = register(registry, new StateFlag("fl-animal-damage", false), StateFlag.class);
        namedAnimalDamage = register(registry, new StateFlag("fl-named-animal-damage", false), StateFlag.class);
        removeEffectsOnExit = register(registry, new StateFlag("fl-remove-effects-on-exit", false), StateFlag.class);
        effects = register(registry, new StringFlag("fl-effects"), StringFlag.class);
        waterEffects = register(registry, new StringFlag("fl-water-effects"), StringFlag.class);
        timeSwitch = register(registry, new StringFlag("fl-time-switch"), StringFlag.class);
        realTimeSwitch = register(registry, new StringFlag("fl-real-time-switch"), StringFlag.class);
        staySeconds = register(registry, new IntegerFlag("fl-stay-seconds"), IntegerFlag.class);
        stayTeleport = register(registry, new StringFlag("fl-stay-tp"), StringFlag.class);
        afkSeconds = register(registry, new IntegerFlag("fl-afk-seconds"), IntegerFlag.class);
        afkTeleport = register(registry, new StringFlag("fl-afk-tp"), StringFlag.class);
        itemEntry = register(registry, new StateFlag("fl-item-entry", false), StateFlag.class);
        itemExit = register(registry, new StateFlag("fl-item-exit", false), StateFlag.class);
        placeBlocks = register(registry, new StringFlag("fl-place-blocks"), StringFlag.class);
        breakBlocks = register(registry, new StringFlag("fl-break-blocks"), StringFlag.class);
        blockRollbackSeconds = register(registry, new IntegerFlag("fl-block-rollback-seconds"), IntegerFlag.class);
        chatAllowed = register(registry, new StringFlag("fl-chat-allowed"), StringFlag.class);
        commandAllowed = register(registry, new StringFlag("fl-command-allowed"), StringFlag.class);
        storageProtection = register(registry, new StateFlag("fl-storage-protection", false), StateFlag.class);
    }

    private static void preflight(FlagRegistry registry) {
        requireCompatible(registry, "fl-villager-trade", StateFlag.class);
        requireCompatible(registry, "fl-only-wheat-seeds", StateFlag.class);
        requireCompatible(registry, "fl-wind-charge", StateFlag.class);
        requireCompatible(registry, "fl-ender-pearl", StateFlag.class);
        requireCompatible(registry, "fl-chorus-fruit", StateFlag.class);
        requireCompatible(registry, "fl-invincible", StateFlag.class);
        requireCompatible(registry, "fl-entry-message", StringFlag.class);
        requireCompatible(registry, "fl-animal-damage", StateFlag.class);
        requireCompatible(registry, "fl-named-animal-damage", StateFlag.class);
        requireCompatible(registry, "fl-remove-effects-on-exit", StateFlag.class);
        requireCompatible(registry, "fl-effects", StringFlag.class);
        requireCompatible(registry, "fl-water-effects", StringFlag.class);
        requireCompatible(registry, "fl-time-switch", StringFlag.class);
        requireCompatible(registry, "fl-real-time-switch", StringFlag.class);
        requireCompatible(registry, "fl-stay-seconds", IntegerFlag.class);
        requireCompatible(registry, "fl-stay-tp", StringFlag.class);
        requireCompatible(registry, "fl-afk-seconds", IntegerFlag.class);
        requireCompatible(registry, "fl-afk-tp", StringFlag.class);
        requireCompatible(registry, "fl-item-entry", StateFlag.class);
        requireCompatible(registry, "fl-item-exit", StateFlag.class);
        requireCompatible(registry, "fl-place-blocks", StringFlag.class);
        requireCompatible(registry, "fl-break-blocks", StringFlag.class);
        requireCompatible(registry, "fl-block-rollback-seconds", IntegerFlag.class);
        requireCompatible(registry, "fl-chat-allowed", StringFlag.class);
        requireCompatible(registry, "fl-command-allowed", StringFlag.class);
        requireCompatible(registry, "fl-storage-protection", StateFlag.class);
    }

    private static void requireCompatible(FlagRegistry registry, String name, Class<? extends Flag<?>> type) {
        Flag<?> existing = registry.get(name);
        if (existing != null && !type.isInstance(existing)) {
            throw new IllegalStateException(
                    "WorldGuard flag conflict for '" + name + "': existing flag has another type"
            );
        }
    }

    private static <T extends Flag<?>> T register(FlagRegistry registry, T flag, Class<T> type) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(flag, "flag");
        try {
            registry.register(flag);
            return flag;
        } catch (FlagConflictException conflict) {
            Flag<?> existing = registry.get(flag.getName());
            if (type.isInstance(existing)) {
                return type.cast(existing);
            }
            throw new IllegalStateException(
                    "WorldGuard flag conflict for '" + flag.getName() + "': existing flag has another type",
                    conflict
            );
        }
    }
}
