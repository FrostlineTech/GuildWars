package com.guildwars.commands;

import com.guildwars.GuildWars;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for the /support command.
 */
public class SupportCommand implements CommandExecutor {

    private final GuildWars plugin;
    private final String discordLink = "https://discord.gg/FGUEEj6k7k";

    /**
     * Creates a new support command handler.
     *
     * @param plugin The GuildWars plugin instance
     */
    public SupportCommand(GuildWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Log that the command was executed
        plugin.getLogger().info("SupportCommand executed by " + sender.getName());

        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            // Use non-deprecated methods for modern Spigot versions
            player.sendMessage(ChatColor.YELLOW + "Join the Frostline Discord server for plugin support: " + 
                    ChatColor.AQUA + ChatColor.UNDERLINE + discordLink);
            
            // Also send a message to let them know they can click the link in chat
            player.sendMessage(ChatColor.GRAY + "Click the link in chat to open the Discord invite");
        } else {
            // For console or command blocks
            sender.sendMessage(ChatColor.YELLOW + "Join the Frostline Discord server for plugin support: " + 
                    ChatColor.AQUA + discordLink);
        }
        
        return true;
    }
}
