package com.guildwars;

import com.guildwars.commands.GuildCommand;
import com.guildwars.commands.GuildsCommand;
import com.guildwars.commands.HelpCommand;
import com.guildwars.commands.SupportCommand;
import com.guildwars.database.GuildService;
import com.guildwars.storage.YamlStorageService;
import com.guildwars.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the GuildWars plugin.
 */
public class GuildWars extends JavaPlugin {

    private static GuildWars instance;
    private YamlStorageService storageService;
    private GuildService guildService;

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
        // Register guild command
        getCommand("guild").setExecutor(new GuildCommand(this));
        
        // Register guilds command
        getCommand("guilds").setExecutor(new GuildsCommand(this));
        
        // Register help command
        getCommand("guildhelp").setExecutor(new HelpCommand(this));
        
        // Register support command
        getCommand("support").setExecutor(new SupportCommand(this));
    }
    
    /**
     * Register all event listeners for the plugin.
     */
    private void registerListeners() {
        // Register event listeners here when needed
        getLogger().info("Registering event listeners...");
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
}
