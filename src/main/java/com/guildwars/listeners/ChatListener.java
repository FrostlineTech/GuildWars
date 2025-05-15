package com.guildwars.listeners;

import com.guildwars.GuildWars;
import com.guildwars.database.GuildService;
import com.guildwars.model.Guild;
import com.guildwars.util.MessageUtil;
import com.guildwars.util.PlaceholderManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener for player events to add guild tags and display information.
 */
public class ChatListener implements Listener {

    private final GuildWars plugin;
    private final PlaceholderManager placeholderManager;
    private final GuildService guildService;

    /**
     * Creates a new ChatListener.
     *
     * @param plugin The GuildWars plugin instance
     */
    public ChatListener(GuildWars plugin) {
        this.plugin = plugin;
        this.placeholderManager = plugin.getPlaceholderManager();
        this.guildService = plugin.getGuildService();
        
        // Register the chat handler command
        plugin.getCommand("gchat").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: /gchat <message>");
                return true;
            }
            
            Player player = (Player) sender;
            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            
            // Format and broadcast the guild chat message
            broadcastGuildChatMessage(player, message.toString().trim());
            return true;
        });
        
        plugin.getLogger().info("Guild chat system initialized.");
    }

    /**
     * Displays guild information when a player joins the server.
     * 
     * @param event The player join event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        
        if (guild != null) {
            // Display guild information to the player
            String roleTag = placeholderManager.getRoleTag(player);
            String roleColor = placeholderManager.getRoleColor(player);
            
            String groupTerm = MessageUtil.getCapitalizedGroupTerm();
            player.sendMessage(ChatColor.GRAY + "------------------------------");
            player.sendMessage(ChatColor.GOLD + "Welcome back to your " + groupTerm.toLowerCase() + ": " + 
                              ChatColor.WHITE + guild.getName());
            player.sendMessage(ChatColor.GOLD + "Your role: " + 
                              ChatColor.translateAlternateColorCodes('&', roleColor) + roleTag);
            player.sendMessage(ChatColor.GRAY + "------------------------------");
        }
    }
    
    /**
     * Broadcasts a formatted guild chat message to all online players.
     * 
     * @param player The player sending the message
     * @param message The message content
     */
    public void broadcastGuildChatMessage(Player player, String message) {
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild!");
            return;
        }
        
        // Format the message with guild information
        String formattedMessage = placeholderManager.formatChatMessage(player, message);
        if (formattedMessage == null) {
            return;
        }
        
        // Convert color codes
        formattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);
        
        // Broadcast to all online players
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            Guild playerGuild = guildService.getGuildByPlayer(onlinePlayer.getUniqueId());
            
            // Send to guild members or players with admin permission
            if (playerGuild != null && playerGuild.equals(guild) || 
                onlinePlayer.hasPermission("guildwars.admin")) {
                onlinePlayer.sendMessage(formattedMessage);
            }
        }
    }
}
