package com.sagakenichi.freelifewgflags;

import com.sagakenichi.freelifewgflags.listener.ActivityListener;
import com.sagakenichi.freelifewgflags.listener.BlockListener;
import com.sagakenichi.freelifewgflags.listener.BuildOverrideListener;
import com.sagakenichi.freelifewgflags.listener.ChatCommandListener;
import com.sagakenichi.freelifewgflags.listener.DamageListener;
import com.sagakenichi.freelifewgflags.listener.InteractionListener;
import com.sagakenichi.freelifewgflags.listener.InventoryListener;
import com.sagakenichi.freelifewgflags.listener.RegionMovementListener;
import com.sagakenichi.freelifewgflags.service.ActivityTracker;
import com.sagakenichi.freelifewgflags.service.BlockRollbackService;
import com.sagakenichi.freelifewgflags.service.ChatPolicyCache;
import com.sagakenichi.freelifewgflags.service.EffectService;
import com.sagakenichi.freelifewgflags.service.ItemBoundaryService;
import com.sagakenichi.freelifewgflags.service.PlayerStateService;
import com.sagakenichi.freelifewgflags.service.WaterEffectService;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.DateTimeException;
import java.time.ZoneId;

public final class FreeLifeWGFlagsPlugin extends JavaPlugin {

    private static final ZoneId DEFAULT_REAL_TIME_ZONE = ZoneId.of("Asia/Tokyo");

    private final FreeLifeFlags flags = new FreeLifeFlags();
    private PlayerStateService stateService;

    @Override
    public void onLoad() {
        flags.registerAll();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        MessageService messages = new MessageService(this);
        RegionAccess regions = new RegionAccess(flags, realTimeZone());
        ActivityTracker activity = new ActivityTracker();
        ItemBoundaryService itemBoundary = new ItemBoundaryService(flags, regions, messages);
        ChatPolicyCache chatPolicies = new ChatPolicyCache(flags, regions);
        EffectService effects = new EffectService(flags, regions);
        WaterEffectService waterEffects = new WaterEffectService(this, flags, regions);
        BlockRollbackService rollback = new BlockRollbackService(this);
        stateService = new PlayerStateService(
                this,
                flags,
                regions,
                messages,
                itemBoundary,
                activity,
                chatPolicies,
                effects
        );

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new BuildOverrideListener(flags, regions), this);
        pluginManager.registerEvents(
                new RegionMovementListener(regions, itemBoundary, activity, stateService, chatPolicies),
                this
        );
        pluginManager.registerEvents(new ActivityListener(activity), this);
        pluginManager.registerEvents(new InteractionListener(flags, regions, messages, activity), this);
        pluginManager.registerEvents(new InventoryListener(flags, regions, messages), this);
        pluginManager.registerEvents(new DamageListener(flags, regions), this);
        pluginManager.registerEvents(new BlockListener(flags, regions, messages, rollback), this);
        pluginManager.registerEvents(
                new ChatCommandListener(this, flags, regions, messages, activity, chatPolicies),
                this
        );

        for (Player player : getServer().getOnlinePlayers()) {
            stateService.joined(player);
        }
        getServer().getScheduler().runTaskTimer(this, stateService::tick, 20L, 20L);
        getServer().getScheduler().runTaskTimer(this, waterEffects::tick, 1L, 5L);
        getLogger().info("FreeLifeWGFlags 1.3.0 enabled with 26 custom WorldGuard flags.");
    }

    @Override
    public void onDisable() {
        if (stateService != null) {
            stateService.shutdown();
        }
    }

    private ZoneId realTimeZone() {
        String configured = getConfig().getString("schedule.real-time-zone", DEFAULT_REAL_TIME_ZONE.getId());
        if (configured == null || configured.isBlank()) {
            return DEFAULT_REAL_TIME_ZONE;
        }
        try {
            return ZoneId.of(configured.trim());
        } catch (DateTimeException exception) {
            getLogger().warning(
                    "Invalid schedule.real-time-zone '" + configured + "'; using "
                            + DEFAULT_REAL_TIME_ZONE.getId() + "."
            );
            return DEFAULT_REAL_TIME_ZONE;
        }
    }
}
