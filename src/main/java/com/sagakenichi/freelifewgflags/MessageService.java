package com.sagakenichi.freelifewgflags;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MessageService {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> lastNotice = new ConcurrentHashMap<>();

    public MessageService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void send(Player player, String key) {
        player.sendMessage(format(plugin.getConfig().getString("messages." + key, key)));
    }

    public void sendThrottled(Player player, String key) {
        long now = System.currentTimeMillis();
        Long previous = lastNotice.put(player.getUniqueId(), now);
        if (previous != null && now - previous < 1000L) {
            return;
        }
        send(player, key);
    }

    public String format(String message) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&6[FreeLife] &e");
        return ChatColor.translateAlternateColorCodes('&', (prefix == null ? "" : prefix) + message);
    }
}
