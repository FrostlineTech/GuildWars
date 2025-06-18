package com.guildwars.listeners;

import com.guildwars.GuildWars;
import com.guildwars.utils.VisualEffectManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

/**
 * Listener for handling visual effects like damage numbers and health bars.
 */
public class VisualEffectListener implements Listener {
    
    private final GuildWars plugin;
    private final VisualEffectManager visualEffectManager;
    
    /**
     * Creates a new visual effect listener.
     *
     * @param plugin The plugin instance
     * @param visualEffectManager The visual effect manager
     */
    public VisualEffectListener(GuildWars plugin, VisualEffectManager visualEffectManager) {
        this.plugin = plugin;
        this.visualEffectManager = visualEffectManager;
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Handles entity damage events to show damage numbers.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        double damage = event.getFinalDamage();
        
        // Don't show damage for very small amounts (like suffocation ticks)
        if (damage < 0.1) {
            return;
        }
        
        // Determine if it's a critical hit
        boolean isCritical = false;
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
            // Consider player attacks with high damage relative to entity's max health as critical
            if (edbe.getDamager() instanceof Player) {
                double critThreshold = entity.getMaxHealth() * 0.2; // 20% of max health
                isCritical = damage >= critThreshold;
            }
        }
        
        // Show the damage number
        visualEffectManager.showDamageIndicator(entity, damage, isCritical);
        
        // Update health bar after damage if enabled
        if (visualEffectManager.areHealthBarsEnabled()) {
            visualEffectManager.updateHealthBar(entity);
        }
    }
    
    /**
     * Handles entity spawn events to initialize health bars.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        
        // Only show health bars for monsters and custom mobs if enabled
        if (visualEffectManager.areHealthBarsEnabled() && isMonsterOrCustomMob(entity)) {
            // Delay the health bar a bit to ensure entity is fully spawned
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                visualEffectManager.updateHealthBar(entity);
            }, 5L);
        }
    }
    
    /**
     * Handles health regeneration to update health bars.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        
        // Only update health bars for monsters and custom mobs if enabled
        if (visualEffectManager.areHealthBarsEnabled() && isMonsterOrCustomMob(entity)) {
            visualEffectManager.updateHealthBar(entity);
        }
    }
    
    /**
     * Determines if an entity is a monster or custom mob.
     * 
     * @param entity The entity to check
     * @return True if the entity is a monster or custom mob
     */
    private boolean isMonsterOrCustomMob(LivingEntity entity) {
        // Check if it's a player (never show health bars for players)
        if (entity instanceof Player) {
            return false;
        }
        
        // Check for custom name tag (custom mobs typically have name tags)
        if (entity.getCustomName() != null) {
            return true;
        }
        
        // Use the string name of the entity type to avoid compatibility issues
        String entityType = entity.getType().name();
        
        // Common hostile mobs
        return entityType.equals("ZOMBIE") ||
               entityType.equals("SKELETON") ||
               entityType.equals("CREEPER") ||
               entityType.equals("SPIDER") ||
               entityType.equals("ENDERMAN") ||
               entityType.equals("WITCH") ||
               entityType.equals("BLAZE") ||
               entityType.equals("GHAST") ||
               entityType.equals("SLIME") ||
               entityType.equals("MAGMA_CUBE") ||
               entityType.equals("WITHER_SKELETON") ||
               entityType.equals("GUARDIAN") ||
               entityType.equals("ELDER_GUARDIAN") ||
               entityType.equals("SHULKER") ||
               entityType.equals("EVOKER") ||
               entityType.equals("VEX") ||
               entityType.equals("VINDICATOR") ||
               entityType.equals("ILLUSIONER") ||
               entityType.equals("PHANTOM") ||
               entityType.equals("DROWNED") ||
               entityType.equals("PILLAGER") ||
               entityType.equals("RAVAGER") ||
               entityType.equals("HOGLIN") ||
               entityType.equals("PIGLIN") ||
               entityType.equals("PIGLIN_BRUTE") ||
               entityType.equals("ZOGLIN") ||
               entityType.equals("WITHER") ||
               // Added for Minecraft 1.21.6
               entityType.equals("WARDEN");
    }
}
