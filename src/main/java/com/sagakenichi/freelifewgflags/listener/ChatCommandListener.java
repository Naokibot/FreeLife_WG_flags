package com.sagakenichi.freelifewgflags.listener;

import com.sagakenichi.freelifewgflags.FreeLifeFlags;
import com.sagakenichi.freelifewgflags.MessageService;
import com.sagakenichi.freelifewgflags.RegionAccess;
import com.sagakenichi.freelifewgflags.service.ActivityTracker;
import com.sagakenichi.freelifewgflags.service.ChatPolicyCache;
import com.sagakenichi.freelifewgflags.util.RuleList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatCommandListener implements Listener {

    private final JavaPlugin plugin;
    private final FreeLifeFlags flags;
    private final RegionAccess regions;
    private final MessageService messages;
    private final ActivityTracker activity;
    private final ChatPolicyCache chatPolicies;

    public ChatCommandListener(
            JavaPlugin plugin,
            FreeLifeFlags flags,
            RegionAccess regions,
            MessageService messages,
            ActivityTracker activity,
            ChatPolicyCache chatPolicies
    ) {
        this.plugin = plugin;
        this.flags = flags;
        this.regions = regions;
        this.messages = messages;
        this.activity = activity;
        this.chatPolicies = chatPolicies;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        activity.touch(player);
        if (chatPolicies.permits(player, event.getMessage())) {
            return;
        }
        event.setCancelled(true);
        plugin.getServer().getScheduler().runTask(plugin, () -> messages.send(player, "chat-denied"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        activity.touch(player);
        var policy = regions.configuredRegion(player.getLocation(), flags.commandAllowed);
        if (policy.isEmpty() || RuleList.parse(policy.get().value()).permitsCommand(event.getMessage())) {
            return;
        }
        event.setCancelled(true);
        messages.send(player, "command-denied");
    }
}
