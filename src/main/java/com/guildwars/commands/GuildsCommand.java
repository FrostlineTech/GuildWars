package com.guildwars.commands;

import com.guildwars.GuildWars;
import com.guildwars.database.GuildService;
import com.guildwars.model.Guild;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

// Using non-deprecated Bungee ChatColor instead of Adventure API
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Command handler for the /guilds command.
 */
public class GuildsCommand implements CommandExecutor {

    private final GuildWars plugin;
    private final GuildService guildService;

    /**
     * Creates a new guilds command handler.
     *
     * @param plugin The GuildWars plugin instance
     */
    public GuildsCommand(GuildWars plugin) {
        this.plugin = plugin;
        this.guildService = plugin.getGuildService();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Retrieve guilds from database
        Collection<Guild> guildCollection = guildService.getAllGuilds();
        List<Guild> guilds = new ArrayList<>(guildCollection);
        
        sender.sendMessage(ChatColor.GOLD + "=== Guilds on the Server ===");
        
        if (guilds.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No guilds have been created yet.");
            sender.sendMessage(ChatColor.GREEN + "Use /guild create <name> to create the first guild!");
        } else {
            // Sort guilds by member count (largest first)
            guilds.sort(Comparator.comparing(Guild::getMemberCount).reversed());
            
            sender.sendMessage(ChatColor.YELLOW + "Found " + guilds.size() + " guilds:");
            
            for (Guild guild : guilds) {
                String leaderName = Bukkit.getOfflinePlayer(guild.getLeader()).getName();
                
                // Format: GuildName [TAG] - Members: X/Y - Leader: PlayerName
                String guildInfo = ChatColor.GREEN + guild.getName() + 
                        ChatColor.AQUA + " [" + guild.getTag() + "]" +
                        ChatColor.GRAY + " - " +
                        ChatColor.YELLOW + "Members: " + guild.getMemberCount() + "/" + 
                                plugin.getConfig().getInt("guilds.max-members", 20) +
                        ChatColor.GRAY + " - " +
                        ChatColor.GOLD + "Leader: " + leaderName;
                
                sender.sendMessage(guildInfo);
            }
            
            sender.sendMessage(ChatColor.YELLOW + "Use /guild info <guild> to see detailed information about a guild.");
        }
        
        // Log that the command was executed
        plugin.getLogger().info("Player " + sender.getName() + " requested the guilds list.");
        
        return true;
    }
}
