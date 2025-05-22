package com.guildwars.utils;

import com.guildwars.GuildWars;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages visual effects like damage numbers and health bars.
 */
public class VisualEffectManager {
    
    private final GuildWars plugin;
    
    // Store entity health displays by entity UUID
    private final Map<UUID, ArmorStand> healthDisplays = new HashMap<>();
    
    /**
     * Creates a new visual effect manager.
     *
     * @param plugin The plugin instance
     */
    public VisualEffectManager(GuildWars plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Shows a damage indicator above an entity when it takes damage.
     *
     * @param entity The entity that took damage
     * @param damage The amount of damage taken
     * @param isCritical Whether the damage was critical
     */
    public void showDamageIndicator(LivingEntity entity, double damage, boolean isCritical) {
        // Format the damage value (round to 1 decimal place)
        String damageText = String.format("%.1f", damage);
        
        // Color based on damage type (critical = red, normal = white)
        ChatColor color = isCritical ? ChatColor.RED : ChatColor.WHITE;
        String displayText = color + "✧ " + damageText + " ✧";
        
        // Create a location slightly above the entity
        Location location = entity.getEyeLocation().add(0, 0.5, 0);
        
        // Add slight random offset so multiple hits don't stack exactly
        double offsetX = (Math.random() - 0.5) * 0.5;
        double offsetZ = (Math.random() - 0.5) * 0.5;
        location.add(offsetX, 0, offsetZ);
        
        // Create temporary floating text using armor stand
        final ArmorStand stand = (ArmorStand) entity.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setMarker(true);
        stand.setCustomName(displayText);
        stand.setCustomNameVisible(true);
        
        // Define the animation path (float upward and fade out)
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 20; // Display for 1 second
            
            @Override
            public void run() {
                if (ticks >= maxTicks || stand.isDead()) {
                    stand.remove();
                    this.cancel();
                    return;
                }
                
                // Move upward slowly
                stand.teleport(stand.getLocation().add(0, 0.05, 0));
                
                // If past halfway, start to fade (by toggling visibility every other tick)
                if (ticks > maxTicks / 2 && ticks % 2 == 0) {
                    stand.setCustomNameVisible(!stand.isCustomNameVisible());
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Updates or creates a health bar display above an entity.
     *
     * @param entity The entity to display health for
     */
    public void updateHealthBar(LivingEntity entity) {
        if (entity == null || entity.isDead()) {
            return;
        }
        
        // Get current health values
        double health = entity.getHealth();
        double maxHealth = entity.getMaxHealth();
        double healthPercentage = health / maxHealth;
        
        // Create health bar representation with heart symbols
        String healthBar = createHealthBar(healthPercentage);
        
        // Create a location above the entity's head
        Location location = entity.getEyeLocation().add(0, 0.8, 0);
        
        // Check if entity already has a health display
        ArmorStand stand = healthDisplays.get(entity.getUniqueId());
        
        if (stand == null || stand.isDead()) {
            // Create new armor stand for health display
            final ArmorStand newStand = (ArmorStand) entity.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            newStand.setVisible(false);
            newStand.setGravity(false);
            newStand.setSmall(true);
            newStand.setMarker(true);
            newStand.setCustomNameVisible(true);
            
            // Store reference to the armor stand
            healthDisplays.put(entity.getUniqueId(), newStand);
            
            // Schedule removal when entity dies or after 60 seconds of no updates
            new BukkitRunnable() {
                private int ticksWithoutUpdate = 0;
                private final ArmorStand standRef = newStand;
                
                @Override
                public void run() {
                    ticksWithoutUpdate++;
                    
                    // Check if entity is dead or gone
                    if (entity.isDead() || !entity.isValid() || ticksWithoutUpdate > 1200) {
                        standRef.remove();
                        healthDisplays.remove(entity.getUniqueId());
                        this.cancel();
                        return;
                    }
                    
                    // Update position to follow entity
                    if (ticksWithoutUpdate % 5 == 0) { // Update position every 5 ticks
                        Location newLoc = entity.getEyeLocation().add(0, 0.8, 0);
                        standRef.teleport(newLoc);
                    }
                }
            }.runTaskTimer(plugin, 20L, 1L);
        } else {
            // Update existing armor stand position
            stand.teleport(location);
            
            // Update the health bar text
            stand.setCustomName(healthBar);
        }
    }
    
    /**
     * Creates a visual health bar using heart symbols.
     *
     * @param percentage The percentage of health (0.0 to 1.0)
     * @return A string representing the health bar with colored hearts
     */
    private String createHealthBar(double percentage) {
        // Maximum number of hearts to display
        int maxHearts = 10;
        
        // Calculate filled and empty hearts
        int filledHearts = (int) Math.ceil(percentage * maxHearts);
        if (filledHearts < 1 && percentage > 0) {
            filledHearts = 1; // Always show at least one heart if entity has any health
        }
        
        int emptyHearts = maxHearts - filledHearts;
        
        // Build the health bar string
        StringBuilder builder = new StringBuilder();
        
        // Add health percentage at the beginning
        builder.append(ChatColor.GOLD).append(String.format("%.0f%%", percentage * 100)).append(" ");
        
        // Color code for hearts based on health percentage
        ChatColor heartColor;
        if (percentage > 0.75) {
            heartColor = ChatColor.GREEN;
        } else if (percentage > 0.25) {
            heartColor = ChatColor.YELLOW;
        } else {
            heartColor = ChatColor.RED;
        }
        
        // Add filled hearts
        builder.append(heartColor);
        for (int i = 0; i < filledHearts; i++) {
            builder.append("❤");
        }
        
        // Add empty hearts
        if (emptyHearts > 0) {
            builder.append(ChatColor.GRAY);
            for (int i = 0; i < emptyHearts; i++) {
                builder.append("❤");
            }
        }
        
        return builder.toString();
    }
    
    /**
     * Removes all visual effects when the plugin is disabled.
     */
    public void cleanup() {
        // Remove all health displays
        for (ArmorStand stand : healthDisplays.values()) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        healthDisplays.clear();
    }
}
