package com.guildwars.util;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Utility class for sending messages to players.
 */
public class MessageUtil {
    
    private static Plugin plugin;
    private static String groupTerm = "guild";
    
    /**
     * Initializes the message utility with a plugin instance.
     *
     * @param pluginInstance The plugin instance
     */
    public static void init(Plugin pluginInstance) {
        plugin = pluginInstance;
        
        // Load custom group term from config
        if (plugin != null) {
            groupTerm = plugin.getConfig().getString("general.group-term", "guild");
        }
    }
    
    /**
     * Sends a message to a command sender.
     *
     * @param sender The command sender
     * @param message The message
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }
    
    /**
     * Sends a success message to a command sender.
     *
     * @param sender The command sender
     * @param message The message
     */
    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage("§a" + message);
    }
    
    /**
     * Sends an error message to a command sender.
     *
     * @param sender The command sender
     * @param message The message
     */
    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage("§c" + message);
    }
    
    /**
     * Sends an info message to a command sender.
     *
     * @param sender The command sender
     * @param message The message
     */
    public static void sendInfo(CommandSender sender, String message) {
        sender.sendMessage("§e" + message);
    }
    
    /**
     * Sends a title message to a command sender.
     *
     * @param sender The command sender
     * @param message The message
     */
    public static void sendTitle(CommandSender sender, String message) {
        sender.sendMessage("§6§l" + message);
    }
    
    /**
     * Broadcasts a message to all online players.
     *
     * @param message The message to broadcast
     */
    public static void broadcast(String message) {
        if (plugin != null) {
            plugin.getServer().getOnlinePlayers().forEach(player -> 
                player.sendMessage(message));
        }
    }
    
    /**
     * Broadcasts a success message to all online players.
     *
     * @param message The message to broadcast
     */
    public static void broadcastSuccess(String message) {
        broadcast("§a" + message);
    }
    
    /**
     * Broadcasts an error message to all online players.
     *
     * @param message The message to broadcast
     */
    public static void broadcastError(String message) {
        broadcast("§c" + message);
    }
    
    /**
     * Closes the message utility.
     */
    public static void close() {
        plugin = null;
    }
    
    /**
     * Reloads the message utility.
     * This is used when the plugin configuration is reloaded.
     */
    public static void reload() {
        // Re-initializes with the existing plugin instance
        if (plugin != null) {
            Plugin currentPlugin = plugin;
            close();
            init(currentPlugin);
        }
    }
    
    /**
     * Gets the configured group term (guild, faction, clan, etc.)
     * 
     * @return The configured group term
     */
    public static String getGroupTerm() {
        return groupTerm;
    }
    
    /**
     * Gets the configured group term with first letter capitalized
     * 
     * @return The configured group term, capitalized
     */
    public static String getCapitalizedGroupTerm() {
        if (groupTerm == null || groupTerm.isEmpty()) {
            return "Guild";
        }
        return groupTerm.substring(0, 1).toUpperCase() + groupTerm.substring(1);
    }
    
    /**
     * Replaces all occurrences of the word "guild" in a message with the configured group term
     * 
     * @param message The message to process
     * @return The message with the group term replaced
     */
    public static String replaceGroupTerm(String message) {
        if (message == null || groupTerm.equals("guild")) {
            return message;
        }
        
        // Replace various forms of the word "guild"
        String result = message;
        result = result.replaceAll("(?i)\\bguild\\b", groupTerm);
        result = result.replaceAll("(?i)\\bGuild\\b", getCapitalizedGroupTerm());
        result = result.replaceAll("(?i)\\bGUILD\\b", groupTerm.toUpperCase());
        
        return result;
    }
}
