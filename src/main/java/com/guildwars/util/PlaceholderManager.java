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
        
        return "";
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
        
        return result;
    }
}
