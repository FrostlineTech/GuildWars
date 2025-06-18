package com.guildwars.utils;

import com.guildwars.GuildWars;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages periodic clearing of ground items to reduce server lag
 */
public class ClearLagManager {

    private final GuildWars plugin;
    private final Set<EntityType> excludedTypes;
    private BukkitTask clearTask;
    private BukkitTask warningTask;
    private boolean enabled;
    private int interval; // in minutes
    private int warningTime; // in seconds

    /**
     * Creates a new clear lag manager
     *
     * @param plugin The plugin instance
     */
    public ClearLagManager(GuildWars plugin) {
        this.plugin = plugin;
        this.excludedTypes = loadExcludedTypes();
        loadConfig();
        
        // Start the lag clear task if enabled
        if (enabled) {
            startClearTask();
        }
    }
    
    /**
     * Loads configuration settings
     */
    public void loadConfig() {
        enabled = plugin.getConfig().getBoolean("general.performance.clear-lag.enabled", true);
        interval = plugin.getConfig().getInt("general.performance.clear-lag.interval", 5);
        warningTime = plugin.getConfig().getInt("general.performance.clear-lag.warning-time", 60);
        
        plugin.getLogger().info("Clear lag enabled: " + enabled);
        plugin.getLogger().info("Clear lag interval: " + interval + " minutes");
        plugin.getLogger().info("Clear lag warning time: " + warningTime + " seconds");
    }
    
    /**
     * Loads the entity types to exclude from clearing
     * 
     * @return Set of excluded entity types
     */
    private Set<EntityType> loadExcludedTypes() {
        List<String> excludedTypeNames = plugin.getConfig().getStringList("general.performance.clear-lag.excluded-types");
        Set<EntityType> types = new HashSet<>();
        
        for (String typeName : excludedTypeNames) {
            try {
                EntityType type = EntityType.valueOf(typeName);
                types.add(type);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type in excluded-types: " + typeName);
            }
        }
        
        return types;
    }
    
    /**
     * Starts the periodic clear lag task
     */
    public void startClearTask() {
        // Cancel any existing tasks
        stopTasks();
        
        // Convert interval to ticks (1 minute = 20 ticks * 60 seconds)
        long intervalTicks = interval * 20L * 60L;
        
        // Start the main clear lag task
        clearTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Schedule the warning task to run before clearing
                warningTask = new BukkitRunnable() {
                    private int secondsLeft = warningTime;
                    
                    @Override
                    public void run() {
                        if (secondsLeft <= 0) {
                            // Time's up, clear the items
                            int cleared = clearItems();
                            
                            // Broadcast the completion message
                            String message = ChatColor.RED + "Cleared " + ChatColor.YELLOW + cleared + 
                                    ChatColor.RED + " items from the ground.";
                            plugin.getServer().broadcastMessage(ChatColor.GOLD + "[GuildWars] " + message);
                            
                            // Cancel this warning task
                            this.cancel();
                            warningTask = null;
                            return;
                        }
                        
                        // Determine if we should send a warning message
                        // Send at: 60, 30, 15, 10, 5, 4, 3, 2, 1 seconds
                        if (secondsLeft == 60 || secondsLeft == 30 || secondsLeft == 15 || 
                                secondsLeft == 10 || secondsLeft <= 5) {
                            String timeString = secondsLeft == 1 ? "second" : "seconds";
                            String message = ChatColor.YELLOW + "Ground items will be cleared in " + 
                                    ChatColor.RED + secondsLeft + ChatColor.YELLOW + " " + timeString + "!";
                            plugin.getServer().broadcastMessage(ChatColor.GOLD + "[GuildWars] " + message);
                        }
                        
                        // Decrement counter
                        secondsLeft--;
                    }
                }.runTaskTimer(plugin, 0L, 20L); // Run every second
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
        
        plugin.getLogger().info("Clear lag task started with interval of " + interval + " minutes");
    }
    
    /**
     * Clears items from all worlds
     * 
     * @return Number of items cleared
     */
    public int clearItems() {
        int count = 0;
        
        // Process all worlds
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                // Skip excluded entity types
                if (excludedTypes.contains(entity.getType())) {
                    continue;
                }
                
                // Check if it's an item on the ground
                if (entity instanceof Item) {
                    entity.remove();
                    count++;
                }
            }
        }
        
        plugin.getLogger().info("Cleared " + count + " items from all worlds");
        return count;
    }
    
    /**
     * Forces an immediate item clear with warning
     */
    public void forceClear() {
        // Cancel existing warning task if any
        if (warningTask != null) {
            warningTask.cancel();
        }
        
        // Start a new warning
        warningTask = new BukkitRunnable() {
            private int secondsLeft = warningTime;
            
            @Override
            public void run() {
                if (secondsLeft <= 0) {
                    // Time's up, clear the items
                    int cleared = clearItems();
                    
                    // Broadcast the completion message
                    String message = ChatColor.RED + "Cleared " + ChatColor.YELLOW + cleared + 
                            ChatColor.RED + " items from the ground.";
                    plugin.getServer().broadcastMessage(ChatColor.GOLD + "[GuildWars] " + message);
                    
                    // Cancel this warning task
                    this.cancel();
                    warningTask = null;
                    return;
                }
                
                // Determine if we should send a warning message
                // Send at: 60, 30, 15, 10, 5, 4, 3, 2, 1 seconds
                if (secondsLeft == 60 || secondsLeft == 30 || secondsLeft == 15 || 
                        secondsLeft == 10 || secondsLeft <= 5) {
                    String timeString = secondsLeft == 1 ? "second" : "seconds";
                    String message = ChatColor.YELLOW + "Ground items will be cleared in " + 
                            ChatColor.RED + secondsLeft + ChatColor.YELLOW + " " + timeString + "!";
                    plugin.getServer().broadcastMessage(ChatColor.GOLD + "[GuildWars] " + message);
                }
                
                // Decrement counter
                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    /**
     * Stops all clear lag tasks
     */
    private void stopTasks() {
        if (clearTask != null) {
            clearTask.cancel();
            clearTask = null;
        }
        
        if (warningTask != null) {
            warningTask.cancel();
            warningTask = null;
        }
    }
    
    /**
     * Set whether clear lag is enabled
     * 
     * @param enabled True to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        
        if (enabled) {
            startClearTask();
            plugin.getLogger().info("Clear lag enabled");
        } else {
            stopTasks();
            plugin.getLogger().info("Clear lag disabled");
        }
    }
    
    /**
     * Check if clear lag is enabled
     * 
     * @return True if enabled, false if disabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Set the interval between clear lag operations
     * 
     * @param minutes The interval in minutes
     */
    public void setInterval(int minutes) {
        if (minutes < 1) {
            minutes = 1;
        }
        
        this.interval = minutes;
        plugin.getLogger().info("Clear lag interval set to " + minutes + " minutes");
        
        // Restart the task if enabled
        if (enabled) {
            startClearTask();
        }
    }
    
    /**
     * Clean up and cancel any scheduled tasks
     */
    public void cleanup() {
        stopTasks();
    }
}
