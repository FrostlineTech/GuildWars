package com.guildwars;

import com.guildwars.commands.AdminCommand;
import com.guildwars.commands.EnchantmentListCommand;
import com.guildwars.commands.GuildCommand;
import com.guildwars.commands.GuildsCommand;
import com.guildwars.commands.HelpCommand;
import com.guildwars.commands.SupportCommand;
import com.guildwars.database.GuildService;
import com.guildwars.enchantments.CustomEnchantmentManager;
import com.guildwars.listeners.ChatListener;
import com.guildwars.listeners.TreeFellerListener;
import com.guildwars.listeners.VisualEffectListener;
import com.guildwars.mobs.CustomMobManager;
import com.guildwars.storage.YamlStorageService;
import com.guildwars.util.MessageUtil;
import com.guildwars.util.PlaceholderManager;
import com.guildwars.utils.ClearLagManager;
import com.guildwars.utils.MobMergeManager;
import com.guildwars.mobs.CustomMobSpawnManager;
import com.guildwars.utils.VisualEffectManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the GuildWars plugin.
 * Version: 1.5.0 for Minecraft 1.21.6
 */
public class GuildWars extends JavaPlugin {

    private static GuildWars instance;
    private YamlStorageService storageService;
    private GuildService guildService;
    private PlaceholderManager placeholderManager;
    private CustomEnchantmentManager enchantmentManager;
    private CustomMobManager mobManager;
    private CustomMobSpawnManager mobSpawnManager;
    private VisualEffectManager visualEffectManager;
    private MobMergeManager mobMergeManager;
    private ClearLagManager clearLagManager;

    @Override
    public void onEnable() {
        // Set instance
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize message utility
        MessageUtil.init(this);
        
        // Initialize data storage
        initializeDataStorage();
        
        // Initialize guild service
        initializeGuildService();
        
        // Initialize placeholder manager
        initializePlaceholderManager();
        
        // Initialize custom enchantments
        initializeEnchantments();
        
        // Initialize custom mobs
        initializeCustomMobs();
        
        // Initialize visual effects
        initializeVisualEffects();
        
        // Initialize performance features
        initializePerformanceFeatures();
        
        // Register commands
        registerCommands();
        
        // Register event listeners
        registerListeners();
        
        getLogger().info("GuildWars plugin has been enabled!");
        // Use MessageUtil for broadcasting
        MessageUtil.broadcastSuccess("GuildWars plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save data when plugin is disabled
        saveData();
        
        // No need to unregister custom enchantments with the new implementation
        
        // Clean up visual effects
        if (visualEffectManager != null) {
            visualEffectManager.cleanup();
            getLogger().info("Visual effects cleaned up.");
        }
        
        // Clean up performance managers
        if (mobMergeManager != null) {
            getLogger().info("Mob merge manager stopped.");
        }
        
        if (clearLagManager != null) {
            clearLagManager.cleanup();
            getLogger().info("Clear lag manager stopped.");
        }
        
        getLogger().info("GuildWars plugin has been disabled!");
        // Broadcast before closing MessageUtil
        MessageUtil.broadcastError("GuildWars plugin has been disabled!");
        
        // Close message utility
        MessageUtil.close();
    }
    
    /**
     * Register all commands for the plugin.
     */
    private void registerCommands() {
        // Get custom command alias from config
        String mainCommandAlias = getConfig().getString("commands.main-command-alias", "guild");
        
        // Register guild command
        PluginCommand guildCommand = getCommand("guild");
        if (guildCommand != null) {
            GuildCommand executor = new GuildCommand(this);
            guildCommand.setExecutor(executor);
            
            // Add custom alias if it's different from the default
            if (!mainCommandAlias.equalsIgnoreCase("guild") && !mainCommandAlias.isEmpty()) {
                guildCommand.getAliases().add(mainCommandAlias.toLowerCase());
                getLogger().info("Registered custom alias '" + mainCommandAlias + "' for the guild command");
            }
        }
        
        // Register guilds command
        PluginCommand guildsCommand = getCommand("guilds");
        if (guildsCommand != null) {
            guildsCommand.setExecutor(new GuildsCommand(this));
        }
        
        // Register help command
        PluginCommand helpCommand = getCommand("guildhelp");
        if (helpCommand != null) {
            helpCommand.setExecutor(new HelpCommand(this));
        }
        
        // Register support command
        PluginCommand supportCommand = getCommand("support");
        if (supportCommand != null) {
            supportCommand.setExecutor(new SupportCommand(this));
        }
        
        // Register admin command
        PluginCommand adminCommand = getCommand("guildadmin");
        if (adminCommand != null) {
            adminCommand.setExecutor(new AdminCommand(this, enchantmentManager, mobManager));
        }
        
        // Register enchantments command
        PluginCommand enchantmentsCommand = getCommand("enchantments");
        if (enchantmentsCommand != null) {
            // This registers both the executor and tab completer in the constructor
            new EnchantmentListCommand(this);
            getLogger().info("Enchantments command registered.");
        }
    }
    
    /**
     * Register all event listeners for the plugin.
     */
    private void registerListeners() {
        // Register chat listener for guild chat tags
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getLogger().info("Chat listener registered for guild chat tags.");
        
        // Register TreeFeller listener
        getServer().getPluginManager().registerEvents(new TreeFellerListener(this), this);
        getLogger().info("TreeFeller listener registered.");
        
        // Register Visual Effect listener
        if (visualEffectManager != null) {
            new VisualEffectListener(this, visualEffectManager);
            getLogger().info("Visual Effects listener registered.");
        }
        
        // Note: MobMergeManager registers its own event listeners if enabled
    }
    

    
    /**
     * Initialize data storage for guilds and players.
     */
    private void initializeDataStorage() {
        // Initialize YAML storage service
        storageService = new YamlStorageService(this);
        getLogger().info("Using YAML files for data storage.");
    }
    
    /**
     * Initialize the guild service.
     */
    private void initializeGuildService() {
        guildService = new GuildService(this, storageService);
        getLogger().info("Guild service initialized.");
    }
    
    /**
     * Save all plugin data.
     */
    private void saveData() {
        // Save data to YAML files
        if (storageService != null) {
            storageService.saveAllData();
            getLogger().info("All guild data saved to YAML files.");
        }
    }
    
    /**
     * Initialize the placeholder manager.
     */
    private void initializePlaceholderManager() {
        placeholderManager = new PlaceholderManager(this);
        getLogger().info("Placeholder manager initialized.");
    }
    
    /**
     * Reload the placeholder manager.
     */
    public void reloadPlaceholders() {
        if (placeholderManager != null) {
            placeholderManager.reloadTags();
            getLogger().info("Placeholders reloaded.");
        }
    }
    
    /**
     * Get the plugin instance.
     * 
     * @return The plugin instance
     */
    public static GuildWars getInstance() {
        return instance;
    }
    
    /**
     * Get the storage service.
     * 
     * @return The storage service
     */
    public YamlStorageService getStorageService() {
        return storageService;
    }
    
    /**
     * Get the guild service.
     * 
     * @return The guild service
     */
    public GuildService getGuildService() {
        return guildService;
    }
    
    /**
     * Get the placeholder manager.
     * 
     * @return The placeholder manager
     */
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
    
    /**
     * Initialize the custom enchantments.
     */
    private void initializeEnchantments() {
        enchantmentManager = new CustomEnchantmentManager(this);
        getLogger().info("Custom enchantments initialized.");
    }
    
    /**
     * Get the enchantment manager.
     * 
     * @return The enchantment manager
     */
    public CustomEnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }
    
    /**
     * Initialize the custom mobs system.
     */
    private void initializeCustomMobs() {
        // Initialize custom mob manager
        mobManager = new CustomMobManager(this);
        
        // Initialize custom mob spawn manager for biome-specific spawns
        mobSpawnManager = new CustomMobSpawnManager(this, mobManager);
        getLogger().info("Custom mobs system initialized.");
    }
    
    /**
     * Get the custom mob manager.
     * 
     * @return The custom mob manager
     */
    public CustomMobManager getMobManager() {
        return mobManager;
    }
    
    /**
     * Get the custom mob spawn manager.
     * 
     * @return The custom mob spawn manager
     */
    public CustomMobSpawnManager getCustomMobSpawnManager() {
        return mobSpawnManager;
    }
    
    /**
     * Initialize the visual effects system.
     */
    private void initializeVisualEffects() {
        visualEffectManager = new VisualEffectManager(this);
        getLogger().info("Visual effects system initialized.");
    }
    
    /**
     * Initialize performance optimization features like mob merging and clear lag.
     */
    private void initializePerformanceFeatures() {
        // Initialize mob merging
        mobMergeManager = new MobMergeManager(this);
        getLogger().info("Mob merging system initialized.");
        
        // Initialize clear lag system
        clearLagManager = new ClearLagManager(this);
        getLogger().info("Clear lag system initialized.");
    }
    
    /**
     * Get the visual effect manager.
     * 
     * @return The visual effect manager
     */
    public VisualEffectManager getVisualEffectManager() {
        return visualEffectManager;
    }
    
    /**
     * Get the mob merge manager.
     * 
     * @return The mob merge manager
     */
    public MobMergeManager getMobMergeManager() {
        return mobMergeManager;
    }
    
    /**
     * Get the clear lag manager.
     * 
     * @return The clear lag manager
     */
    public ClearLagManager getClearLagManager() {
        return clearLagManager;
    }
}
