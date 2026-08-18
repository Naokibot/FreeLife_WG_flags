package com.sagakenichi.freelifewgflags;

import com.sagakenichi.freelifewgflags.listener.BlockListener;
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
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FreeLifeWGFlagsPlugin extends JavaPlugin {

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
        RegionAccess regions = new RegionAccess(flags);
        ActivityTracker activity = new ActivityTracker();
        ItemBoundaryService itemBoundary = new ItemBoundaryService(flags, regions, messages);
        ChatPolicyCache chatPolicies = new ChatPolicyCache(flags, regions);
        EffectService effects = new EffectService(flags, regions);
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
        pluginManager.registerEvents(new RegionMovementListener(regions, itemBoundary, activity, stateService), this);
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
        getLogger().info("FreeLifeWGFlags 1.0.0 enabled with 24 custom WorldGuard flags.");
    }

    @Override
    public void onDisable() {
        if (stateService != null) {
            stateService.shutdown();
        }
    }
}
