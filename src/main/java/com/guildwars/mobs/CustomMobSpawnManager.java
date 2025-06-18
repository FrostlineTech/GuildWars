package com.guildwars.mobs;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Manages the natural spawning of custom mobs in specific biomes.
 */
public class CustomMobSpawnManager implements Listener {
    
    private final JavaPlugin plugin;
    private final CustomMobManager customMobManager;
    private final Random random = new Random();
    
    // Biome categories for spawn rates
    private final Set<Biome> coldBiomes = new HashSet<>();
    private final Set<Biome> plainsBiomes = new HashSet<>();
    
    // Track spawn counts to avoid overloading the world with custom mobs
    private int corruptedWardenCount = 0;
    private int frostGiantCount = 0;
    
    // Config values
    private boolean naturalSpawningEnabled = true;
    private double corruptedWardenSpawnRate = 0.12; // 12% chance in plains
    private double frostGiantSpawnRate = 0.15;      // 15% chance in cold biomes
    private int maxCorruptedWardens = 8;            // Max per server
    private int maxFrostGiants = 6;                 // Max per server
    
    /**
     * Creates a new CustomMobSpawnManager.
     *
     * @param plugin The plugin instance
     * @param customMobManager The custom mob manager
     */
    public CustomMobSpawnManager(JavaPlugin plugin, CustomMobManager customMobManager) {
        this.plugin = plugin;
        this.customMobManager = customMobManager;
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Initialize biome sets
        initializeBiomeSets();
        
        // Load configuration
        loadConfig();
        
        // Start count checker task
        startCountCheckerTask();
    }
    
    /**
     * Initializes biome sets for spawn categories.
     */
    private void initializeBiomeSets() {
        // Cold biomes where Frost Giants spawn more frequently
        try {
            // Use enum values if they exist
            coldBiomes.add(Biome.valueOf("SNOWY_PLAINS"));
        } catch (IllegalArgumentException e) {
            // Use older biome names as fallback
            coldBiomes.add(Biome.valueOf("SNOWY_TUNDRA"));
        }
        
        // Add other cold biomes with safety checks
        addBiomeSafely(coldBiomes, "SNOWY_TAIGA", "SNOWY_TAIGA");
        addBiomeSafely(coldBiomes, "FROZEN_OCEAN", "FROZEN_OCEAN");
        addBiomeSafely(coldBiomes, "DEEP_FROZEN_OCEAN", "DEEP_FROZEN_OCEAN");
        addBiomeSafely(coldBiomes, "ICE_SPIKES", "ICE_SPIKES");
        addBiomeSafely(coldBiomes, "SNOWY_BEACH", "SNOWY_BEACH");
        // Newer biomes with fallbacks
        addBiomeSafely(coldBiomes, "FROZEN_PEAKS", "MOUNTAINS");
        addBiomeSafely(coldBiomes, "JAGGED_PEAKS", "MOUNTAINS");
        addBiomeSafely(coldBiomes, "SNOWY_SLOPES", "SNOWY_TAIGA");
        
        // Plains biomes where Corrupted Wardens spawn more frequently
        addBiomeSafely(plainsBiomes, "PLAINS", "PLAINS");
        addBiomeSafely(plainsBiomes, "SUNFLOWER_PLAINS", "SUNFLOWER_PLAINS");
        addBiomeSafely(plainsBiomes, "FOREST", "FOREST");
        addBiomeSafely(plainsBiomes, "BIRCH_FOREST", "BIRCH_FOREST");
        addBiomeSafely(plainsBiomes, "MEADOW", "PLAINS"); // Fallback for newer biome
    }
    
    /**
     * Safely adds a biome to a set, with fallback for version compatibility.
     * 
     * @param biomeSet The set to add the biome to
     * @param primaryName The primary biome name to try
     * @param fallbackName The fallback biome name if primary doesn't exist
     */
    private void addBiomeSafely(Set<Biome> biomeSet, String primaryName, String fallbackName) {
        try {
            biomeSet.add(Biome.valueOf(primaryName));
        } catch (IllegalArgumentException e) {
            try {
                biomeSet.add(Biome.valueOf(fallbackName));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Could not find biome: " + primaryName + " or fallback: " + fallbackName);
            }
        }
    }
    
    /**
     * Loads configuration values.
     */
    public void loadConfig() {
        // Set defaults if not present
        if (!plugin.getConfig().contains("custom-mobs.natural-spawning")) {
            plugin.getConfig().set("custom-mobs.natural-spawning", true);
            plugin.getConfig().set("custom-mobs.corrupted-warden-spawn-rate", corruptedWardenSpawnRate);
            plugin.getConfig().set("custom-mobs.frost-giant-spawn-rate", frostGiantSpawnRate);
            plugin.getConfig().set("custom-mobs.max-corrupted-wardens", maxCorruptedWardens);
            plugin.getConfig().set("custom-mobs.max-frost-giants", maxFrostGiants);
            plugin.saveConfig();
        }
        
        // Load settings
        naturalSpawningEnabled = plugin.getConfig().getBoolean("custom-mobs.natural-spawning", true);
        corruptedWardenSpawnRate = plugin.getConfig().getDouble("custom-mobs.corrupted-warden-spawn-rate", 0.12);
        frostGiantSpawnRate = plugin.getConfig().getDouble("custom-mobs.frost-giant-spawn-rate", 0.15);
        maxCorruptedWardens = plugin.getConfig().getInt("custom-mobs.max-corrupted-wardens", 8);
        maxFrostGiants = plugin.getConfig().getInt("custom-mobs.max-frost-giants", 6);
    }
    
    /**
     * Starts a task to periodically check and update the current mob counts.
     */
    private void startCountCheckerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Reset counts
                corruptedWardenCount = 0;
                frostGiantCount = 0;
                
                // Count all custom mobs across all worlds
                for (World world : plugin.getServer().getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity instanceof org.bukkit.entity.LivingEntity) {
                            org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) entity;
                            
                            if (CorruptedWarden.isCorruptedWarden(livingEntity)) {
                                corruptedWardenCount++;
                            } else if (FrostGiant.isFrostGiant(livingEntity)) {
                                frostGiantCount++;
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 60L); // Run every minute
    }
    
    /**
     * Handles creature spawn events to potentially replace with custom mobs.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Skip if natural spawning is disabled or if this isn't a natural spawn
        if (!naturalSpawningEnabled || event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }
        
        // Only replace certain hostile mobs
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }
        
        // Skip if custom mobs are disabled
        if (!customMobManager.areCustomMobsEnabled()) {
            return;
        }
        
        EntityType entityType = event.getEntityType();
        Location location = event.getLocation();
        Biome biome = location.getBlock().getBiome();
        
        // For Frost Giants - replace zombies in cold biomes
        if ((entityType == EntityType.ZOMBIE || entityType == EntityType.SKELETON) 
                && coldBiomes.contains(biome) 
                && frostGiantCount < maxFrostGiants
                && random.nextDouble() < frostGiantSpawnRate) {
            
            // Cancel original spawn
            event.setCancelled(true);
            
            // Spawn Frost Giant with a delay to avoid conflicts
            new BukkitRunnable() {
                @Override
                public void run() {
                    customMobManager.summonFrostGiant(location);
                    frostGiantCount++;
                }
            }.runTaskLater(plugin, 2L);
            
            return;
        }
        
        // For Corrupted Wardens - replace zombies/skeletons/creepers in plains biomes
        if ((entityType == EntityType.ZOMBIE || entityType == EntityType.SKELETON || entityType == EntityType.CREEPER) 
                && plainsBiomes.contains(biome)
                && corruptedWardenCount < maxCorruptedWardens
                && random.nextDouble() < corruptedWardenSpawnRate) {
            
            // Cancel original spawn
            event.setCancelled(true);
            
            // Spawn Corrupted Warden with a delay to avoid conflicts
            new BukkitRunnable() {
                @Override
                public void run() {
                    customMobManager.summonCorruptedWarden(location);
                    corruptedWardenCount++;
                }
            }.runTaskLater(plugin, 2L);
            
            return;
        }
    }
    
    /**
     * Handles entity death events to update custom mob counts.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        org.bukkit.entity.LivingEntity entity = event.getEntity();
        
        // Update counts when custom mobs die
        if (CorruptedWarden.isCorruptedWarden(entity)) {
            corruptedWardenCount = Math.max(0, corruptedWardenCount - 1);
        } else if (FrostGiant.isFrostGiant(entity)) {
            frostGiantCount = Math.max(0, frostGiantCount - 1);
        }
    }
    
    /**
     * Gets the current number of spawned Corrupted Wardens.
     *
     * @return The count of Corrupted Wardens
     */
    public int getCorruptedWardenCount() {
        return corruptedWardenCount;
    }
    
    /**
     * Gets the current number of spawned Frost Giants.
     *
     * @return The count of Frost Giants
     */
    public int getFrostGiantCount() {
        return frostGiantCount;
    }
    
    /**
     * Checks if natural spawning is enabled.
     *
     * @return True if natural spawning is enabled
     */
    public boolean isNaturalSpawningEnabled() {
        return naturalSpawningEnabled;
    }
    
    /**
     * Sets whether natural spawning is enabled.
     *
     * @param enabled True to enable natural spawning
     */
    public void setNaturalSpawningEnabled(boolean enabled) {
        naturalSpawningEnabled = enabled;
        plugin.getConfig().set("custom-mobs.natural-spawning", enabled);
        plugin.saveConfig();
    }
    
    /**
     * Sets the Corrupted Warden spawn rate.
     *
     * @param rate The spawn rate (0.0 - 1.0)
     */
    public void setCorruptedWardenSpawnRate(double rate) {
        corruptedWardenSpawnRate = Math.max(0.0, Math.min(1.0, rate));
        plugin.getConfig().set("custom-mobs.corrupted-warden-spawn-rate", corruptedWardenSpawnRate);
        plugin.saveConfig();
    }
    
    /**
     * Sets the Frost Giant spawn rate.
     *
     * @param rate The spawn rate (0.0 - 1.0)
     */
    public void setFrostGiantSpawnRate(double rate) {
        frostGiantSpawnRate = Math.max(0.0, Math.min(1.0, rate));
        plugin.getConfig().set("custom-mobs.frost-giant-spawn-rate", frostGiantSpawnRate);
        plugin.saveConfig();
    }
}
