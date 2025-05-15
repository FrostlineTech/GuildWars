package com.guildwars.util;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Utility class for sending messages to players.
 */
public class MessageUtil {
    
    private static Plugin plugin;
    
    /**
     * Initializes the message utility with a plugin instance.
     *
     * @param pluginInstance The plugin instance
     */
    public static void init(Plugin pluginInstance) {
        plugin = pluginInstance;
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
        // Currently just re-initializes with the existing plugin instance
        if (plugin != null) {
            Plugin currentPlugin = plugin;
            close();
            init(currentPlugin);
        }
    }
}
