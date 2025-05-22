package com.guildwars.mobs;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages custom mobs in the GuildWars plugin.
 */
public class CustomMobManager {
    
    private final JavaPlugin plugin;
    private boolean customMobsEnabled;
    
    // Mob instances
    private DebugMob debugMob;
    private FrostGiant frostGiant;
    private ShadowAssassin shadowAssassin;
    private CorruptedWarden corruptedWarden;
    
    /**
     * Creates a new CustomMobManager instance.
     *
     * @param plugin The plugin instance
     */
    public CustomMobManager(JavaPlugin plugin) {
        this.plugin = plugin;
        
        // Initialize all mob types
        this.debugMob = new DebugMob(plugin);
        this.frostGiant = new FrostGiant(plugin);
        this.shadowAssassin = new ShadowAssassin(plugin);
        this.corruptedWarden = new CorruptedWarden(plugin);
        
        loadConfig();
    }
    
    /**
     * Loads the configuration settings for custom mobs.
     */
    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Set default value if not present
        if (!config.contains("custom-mobs.enabled")) {
            config.set("custom-mobs.enabled", true);
            plugin.saveConfig();
        }
        
        // Load setting
        customMobsEnabled = config.getBoolean("custom-mobs.enabled", true);
    }
    
    /**
     * Checks if custom mobs are enabled.
     *
     * @return True if custom mobs are enabled
     */
    public boolean areCustomMobsEnabled() {
        return customMobsEnabled;
    }
    
    /**
     * Sets whether custom mobs are enabled.
     *
     * @param enabled True to enable custom mobs, false to disable
     */
    public void setCustomMobsEnabled(boolean enabled) {
        customMobsEnabled = enabled;
        plugin.getConfig().set("custom-mobs.enabled", enabled);
        plugin.saveConfig();
    }
    
    /**
     * Gets the Debug mob instance.
     *
     * @return The Debug mob
     */
    public DebugMob getDebugMob() {
        return debugMob;
    }
    
    /**
     * Gets the Frost Giant mob instance.
     *
     * @return The Frost Giant mob
     */
    public FrostGiant getFrostGiant() {
        return frostGiant;
    }
    
    /**
     * Gets the Shadow Assassin mob instance.
     *
     * @return The Shadow Assassin mob
     */
    public ShadowAssassin getShadowAssassin() {
        return shadowAssassin;
    }
    
    /**
     * Gets the Corrupted Warden mob instance.
     *
     * @return The Corrupted Warden mob
     */
    public CorruptedWarden getCorruptedWarden() {
        return corruptedWarden;
    }
    
    /**
     * Summons a Debug mob at the specified location.
     *
     * @param location The location to summon the mob
     * @return The summoned entity
     */
    public LivingEntity summonDebugMob(Location location) {
        if (!customMobsEnabled) {
            return null;
        }
        
        return debugMob.spawn(location);
    }
    
    /**
     * Summons a Frost Giant at the specified location.
     *
     * @param location The location to summon the mob
     * @return The summoned entity
     */
    public LivingEntity summonFrostGiant(Location location) {
        if (!customMobsEnabled) {
            return null;
        }
        
        return frostGiant.spawn(location);
    }
    
    /**
     * Summons a Shadow Assassin at the specified location.
     *
     * @param location The location to summon the mob
     * @return The summoned entity
     */
    public LivingEntity summonShadowAssassin(Location location) {
        if (!customMobsEnabled) {
            return null;
        }
        
        return shadowAssassin.spawn(location);
    }
    
    /**
     * Summons a Corrupted Warden at the specified location.
     *
     * @param location The location to summon the mob
     * @return The summoned entity
     */
    public LivingEntity summonCorruptedWarden(Location location) {
        if (!customMobsEnabled) {
            return null;
        }
        
        return corruptedWarden.spawn(location);
    }
}
