package me.javivi.ktpa;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class ProtectionListener implements Listener {
    private final KindlyTPA plugin;

    public ProtectionListener(KindlyTPA plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        
        if (entity instanceof Player) {
            Player player = (Player) entity;
            
            if (plugin.isPlayerProtected(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        
        if (damager instanceof Player) {
            Player player = (Player) damager;
            
            if (plugin.isPlayerProtected(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}