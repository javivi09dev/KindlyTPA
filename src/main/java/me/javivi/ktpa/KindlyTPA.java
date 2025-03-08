package me.javivi.ktpa;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class KindlyTPA extends JavaPlugin {
    private Map<UUID, UUID> tpaRequests = new HashMap<>();
    private Map<UUID, Long> tpaCooldowns = new HashMap<>();
    private Map<UUID, BukkitTask> tpaExpireTasks = new HashMap<>();
    private Set<UUID> protectedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Register commands
        getCommand("tpa").setExecutor(new TPACommand(this));
        getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
        getCommand("tpadeny").setExecutor(new TPADenyCommand(this));
        getCommand("tpacancel").setExecutor(new TPACancelCommand(this));
        getCommand("ktpa").setExecutor(new ReloadCommand(this));
        
        // Register events
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        
        getLogger().info("KindlyTPA has been enabled!");
    }

    @Override
    public void onDisable() {
        // Cancel all pending tasks
        tpaExpireTasks.values().forEach(BukkitTask::cancel);
        tpaRequests.clear();
        tpaCooldowns.clear();
        protectedPlayers.clear();
        
        getLogger().info("KindlyTPA has been disabled!");
    }
    
    /**
     * Reloads the plugin configuration
     */
    public void reloadPluginConfig() {
        // Reload config from disk
        reloadConfig();
        
        // Log the reload
        getLogger().info("Configuration reloaded successfully!");
    }
    
    // Utility methods for TPA management
    
    public void sendTpaRequest(Player sender, Player target) {
        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();
        
        // Check cooldown
        if (tpaCooldowns.containsKey(senderId)) {
            long timeLeft = (tpaCooldowns.get(senderId) - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                String cooldownMsg = getMessage("request.cooldown")
                        .replace("%time%", String.valueOf(timeLeft));
                sender.sendMessage(colorize(getPrefix() + cooldownMsg));
                return;
            }
        }
        
        // Send request
        tpaRequests.put(targetId, senderId);
        
        // Set cooldown
        tpaCooldowns.put(senderId, System.currentTimeMillis() + (getConfig().getInt("tpa-cooldown") * 1000L));
        
        // Set expiration task (60 seconds)
        BukkitTask task = Bukkit.getScheduler().runTaskLater(this, () -> {
            if (tpaRequests.containsKey(targetId) && tpaRequests.get(targetId).equals(senderId)) {
                tpaRequests.remove(targetId);
                tpaExpireTasks.remove(targetId);
                
                Player s = Bukkit.getPlayer(senderId);
                if (s != null && s.isOnline()) {
                    String expiredMsg = getMessage("request.expired")
                            .replace("%player%", target.getName());
                    s.sendMessage(colorize(getPrefix() + expiredMsg));
                }
            }
        }, 60 * 20L); // 60 seconds
        
        tpaExpireTasks.put(targetId, task);
        
        // Send messages
        String sentMsg = getMessage("request.sent")
                .replace("%player%", target.getName());
        sender.sendMessage(colorize(getPrefix() + sentMsg));
        
        String receivedMsg = getMessage("request.received")
                .replace("%player%", sender.getName());
        target.sendMessage(colorize(getPrefix() + receivedMsg));
    }
    
    public void acceptTpaRequest(Player target) {
        UUID targetId = target.getUniqueId();
        
        if (!tpaRequests.containsKey(targetId)) {
            String noRequestMsg = getMessage("accept.no-request");
            target.sendMessage(colorize(getPrefix() + noRequestMsg));
            return;
        }
        
        UUID senderId = tpaRequests.get(targetId);
        Player sender = Bukkit.getPlayer(senderId);
        
        if (sender == null || !sender.isOnline()) {
            tpaRequests.remove(targetId);
            if (tpaExpireTasks.containsKey(targetId)) {
                tpaExpireTasks.get(targetId).cancel();
                tpaExpireTasks.remove(targetId);
            }
            return;
        }
        
        // Send messages
        String acceptMsg = getMessage("accept.receiver");
        target.sendMessage(colorize(getPrefix() + acceptMsg));
        
        String senderAcceptMsg = getMessage("accept.sender");
        sender.sendMessage(colorize(getPrefix() + senderAcceptMsg));
        
        // Teleport after 3 seconds
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (sender.isOnline() && target.isOnline()) {
                sender.teleport(target);
                applyProtection(sender);
            }
        }, 3 * 20L); // 3 seconds
        
        // Clean up
        if (tpaExpireTasks.containsKey(targetId)) {
            tpaExpireTasks.get(targetId).cancel();
            tpaExpireTasks.remove(targetId);
        }
        tpaRequests.remove(targetId);
    }
    
    public void denyTpaRequest(Player target) {
        UUID targetId = target.getUniqueId();
        
        if (!tpaRequests.containsKey(targetId)) {
            String noRequestMsg = getMessage("deny.no-request");
            target.sendMessage(colorize(getPrefix() + noRequestMsg));
            return;
        }
        
        UUID senderId = tpaRequests.get(targetId);
        Player sender = Bukkit.getPlayer(senderId);
        
        // Send messages
        String denyMsg = getMessage("deny.receiver");
        target.sendMessage(colorize(getPrefix() + denyMsg));
        
        if (sender != null && sender.isOnline()) {
            String senderDenyMsg = getMessage("deny.sender");
            sender.sendMessage(colorize(getPrefix() + senderDenyMsg));
        }
        
        // Clean up
        if (tpaExpireTasks.containsKey(targetId)) {
            tpaExpireTasks.get(targetId).cancel();
            tpaExpireTasks.remove(targetId);
        }
        tpaRequests.remove(targetId);
    }
    
    public void cancelTpaRequest(Player sender) {
        UUID senderId = sender.getUniqueId();
        
        for (Map.Entry<UUID, UUID> entry : new HashMap<>(tpaRequests).entrySet()) {
            if (entry.getValue().equals(senderId)) {
                UUID targetId = entry.getKey();
                
                if (tpaExpireTasks.containsKey(targetId)) {
                    tpaExpireTasks.get(targetId).cancel();
                    tpaExpireTasks.remove(targetId);
                }
                
                tpaRequests.remove(targetId);
                
                sender.sendMessage(colorize(getPrefix() + "&cYou have cancelled your teleport request."));
                
                Player target = Bukkit.getPlayer(targetId);
                if (target != null && target.isOnline()) {
                    target.sendMessage(colorize(getPrefix() + "&e" + sender.getName() + " &chas cancelled their teleport request."));
                }
                
                return;
            }
        }
        
        sender.sendMessage(colorize(getPrefix() + getMessage("request.no-pending")));
    }
    
    public void applyProtection(Player player) {
        UUID playerId = player.getUniqueId();
        int duration = getConfig().getInt("protection-duration");
        
        protectedPlayers.add(playerId);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 255, false, false, true));
        String protMsg = getMessage("protection.enabled").replace("%seconds%", String.valueOf(duration));
        player.sendMessage(colorize(getPrefix() + protMsg));
        
        // Remove protection after duration
        Bukkit.getScheduler().runTaskLater(this, () -> {
            protectedPlayers.remove(playerId);
            if (player.isOnline()) {
                String disabledMsg = getMessage("protection.disabled");
                player.sendMessage(colorize(getPrefix() + disabledMsg));
            }
        }, duration * 20L);
    }
    
    // Utility methods
    
    public String getPrefix() {
        return getMessage("prefix");
    }
    
    public String getMessage(String path) {
        return getConfig().getString("messages." + path, "Message not found: " + path);
    }
    
    public String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public boolean isPlayerProtected(UUID playerId) {
        return protectedPlayers.contains(playerId);
    }
}
