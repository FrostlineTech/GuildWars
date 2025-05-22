package com.guildwars;

import com.guildwars.commands.AdminCommand;
import com.guildwars.commands.GuildCommand;
import com.guildwars.commands.GuildsCommand;
import com.guildwars.commands.HelpCommand;
import com.guildwars.commands.SupportCommand;
import com.guildwars.database.GuildService;
import com.guildwars.listeners.ChatListener;
import com.guildwars.listeners.TreeFellerListener;
import com.guildwars.storage.YamlStorageService;
import com.guildwars.util.MessageUtil;
import com.guildwars.util.PlaceholderManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the GuildWars plugin.
 * Version: 1.1-SNAPSHOT
 */
public class GuildWars extends JavaPlugin {

    private static GuildWars instance;
    private YamlStorageService storageService;
    private GuildService guildService;
    private PlaceholderManager placeholderManager;

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
            adminCommand.setExecutor(new AdminCommand(this));
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
}
