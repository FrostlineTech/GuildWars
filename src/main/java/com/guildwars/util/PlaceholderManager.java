package com.guildwars.util;

import com.guildwars.GuildWars;
import com.guildwars.database.GuildService;
import com.guildwars.model.Guild;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manages placeholders for the GuildWars plugin.
 * This class handles retrieving and formatting guild-related placeholders.
 */
public class PlaceholderManager {

    private final GuildWars plugin;
    private final GuildService guildService;
    private String guildTag;
    private String leaderTag;
    private String officerTag;
    private String memberTag;
    
    // Color codes for different roles
    private String guildColor;
    private String leaderColor;
    private String officerColor;
    private String memberColor;
    
    // Chat format
    private String chatFormat;

    /**
     * Creates a new PlaceholderManager.
     *
     * @param plugin The GuildWars plugin instance
     */
    public PlaceholderManager(GuildWars plugin) {
        this.plugin = plugin;
        this.guildService = plugin.getGuildService();
        loadTags();
    }

    /**
     * Loads placeholder tags from the configuration.
     */
    public void loadTags() {
        FileConfiguration config = plugin.getConfig();
        guildTag = config.getString("placeholders.tags.guild", "[G]");
        leaderTag = config.getString("placeholders.tags.leader", "[Leader]");
        officerTag = config.getString("placeholders.tags.officer", "[Officer]");
        memberTag = config.getString("placeholders.tags.member", "[Member]");
        
        // Load color codes
        guildColor = config.getString("placeholders.colors.guild", "&7").replace('&', '§');
        leaderColor = config.getString("placeholders.colors.leader", "&c").replace('&', '§');
        officerColor = config.getString("placeholders.colors.officer", "&9").replace('&', '§');
        memberColor = config.getString("placeholders.colors.member", "&a").replace('&', '§');
        
        // Load chat format
        chatFormat = config.getString("placeholders.chat-format", 
                "&8[&r%guild_name%&8] %role_color%%guild_role% &r%player_name%&8: &r%message%").replace('&', '§');
    }

    /**
     * Reloads placeholder tags from the configuration.
     */
    public void reloadTags() {
        loadTags();
    }

    /**
     * Gets the guild tag for a player.
     *
     * @param player The player
     * @return The guild tag, or empty string if player is not in a guild
     */
    public String getGuildTag(Player player) {
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        return guild != null ? guildTag : "";
    }

    /**
     * Gets the guild name for a player.
     *
     * @param player The player
     * @return The guild name, or empty string if player is not in a guild
     */
    public String getGuildName(Player player) {
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        return guild != null ? guild.getName() : "";
    }

    /**
     * Gets the role tag for a player.
     *
     * @param player The player
     * @return The role tag, or empty string if player is not in a guild
     */
    public String getRoleTag(Player player) {
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            return "";
        }
        
        if (guild.isLeader(player.getUniqueId())) {
            return leaderTag;
        } else if (guild.isOfficer(player.getUniqueId())) {
            return officerTag;
        }
        
        return memberTag; // Return member tag for regular members
    }

    /**
     * Gets the guild leader's name.
     *
     * @param player The player
     * @return The guild leader's name, or empty string if player is not in a guild
     */
    public String getGuildLeader(Player player) {
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            return "";
        }
        
        return plugin.getServer().getOfflinePlayer(guild.getLeader()).getName();
    }
    
    /**
     * Gets the role color for a player.
     *
     * @param player The player
     * @return The role color code, or empty string if player is not in a guild
     */
    public String getRoleColor(Player player) {
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            return "";
        }
        
        if (guild.isLeader(player.getUniqueId())) {
            return leaderColor;
        } else if (guild.isOfficer(player.getUniqueId())) {
            return officerColor;
        }
        
        return memberColor;
    }
    
    /**
     * Formats a chat message with guild information.
     *
     * @param player The player sending the message
     * @param message The message content
     * @return The formatted chat message with guild information
     */
    public String formatChatMessage(Player player, String message) {
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            return null; // Return null to indicate no guild formatting should be applied
        }
        
        String formatted = chatFormat;
        formatted = formatted.replace("%message%", message);
        formatted = replacePlaceholders(player, formatted);
        
        return formatted;
    }
    
    /**
     * Gets the guild color code.
     *
     * @return The guild color code
     */
    public String getGuildColor() {
        return guildColor;
    }

    /**
     * Replaces placeholders in a string.
     *
     * @param player The player
     * @param input The input string
     * @return The string with placeholders replaced
     */
    public String replacePlaceholders(Player player, String input) {
        if (input == null) {
            return "";
        }
        
        String result = input;
        result = result.replace("%guild_name%", getGuildName(player));
        result = result.replace("%guild_tag%", getGuildTag(player));
        result = result.replace("%guild_role%", getRoleTag(player));
        result = result.replace("%guild_leader%", getGuildLeader(player));
        result = result.replace("%player_name%", player.getName());
        result = result.replace("%role_color%", getRoleColor(player));
        
        return result;
    }
}
