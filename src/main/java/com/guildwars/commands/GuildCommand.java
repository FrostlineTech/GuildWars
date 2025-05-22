package com.guildwars.commands;

import com.guildwars.GuildWars;
import com.guildwars.database.GuildService;
import com.guildwars.model.ChunkPosition;
import com.guildwars.model.Guild;
import com.guildwars.model.Relation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

// Using non-deprecated Bungee ChatColor instead of Bukkit ChatColor
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Main command handler for the /guild command.
 */
public class GuildCommand implements CommandExecutor, TabCompleter {

    private final GuildWars plugin;
    private final GuildService guildService;
    
    // Cooldown tracking maps
    private final java.util.Map<UUID, Long> teleportCooldowns = new java.util.HashMap<>();
    private final java.util.Map<UUID, Long> creationCooldowns = new java.util.HashMap<>();
    private final java.util.Map<UUID, Long> warCooldowns = new java.util.HashMap<>();

    /**
     * Creates a new guild command handler.
     *
     * @param plugin The GuildWars plugin instance
     */
    public GuildCommand(GuildWars plugin) {
        this.plugin = plugin;
        this.guildService = plugin.getGuildService();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Show guild info
            showGuildInfo(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "create":
                handleCreate(player, subArgs);
                break;
            case "join":
                handleJoin(player, subArgs);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "info":
                handleInfo(player, subArgs);
                break;
            case "invite":
                handleInvite(player, subArgs);
                break;
            case "kick":
                handleKick(player, subArgs);
                break;
            case "promote":
                handlePromote(player, subArgs);
                break;
            case "demote":
                handleDemote(player, subArgs);
                break;
            case "claim":
                handleClaim(player);
                break;
            case "unclaim":
                handleUnclaim(player);
                break;
            case "home":
                handleHome(player);
                break;
            case "sethome":
                handleSetHome(player);
                break;
            case "ally":
                handleAlly(player, subArgs);
                break;
            case "enemy":
                handleEnemy(player, subArgs);
                break;
            case "war":
                handleWar(player, subArgs);
                break;
            case "disband":
                handleDisband(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown command. Use /guildhelp for help.");
                break;
        }

        return true;
    }

    /**
     * Shows guild info for a player.
     *
     * @param player The player
     */
    private void showGuildInfo(Player player) {
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        
        if (guild == null) {
            player.sendMessage(ChatColor.GOLD + "=== Your Guild Info ===");
            player.sendMessage(ChatColor.YELLOW + "You are not in a guild. Use /guild create <name> to create one.");
            return;
        }
        
        // Display guild information
        player.sendMessage(ChatColor.GOLD + "=== " + guild.getName() + " [" + guild.getTag() + "] ===");
        
        // Leader and creation date
        String leaderName = Bukkit.getOfflinePlayer(guild.getLeader()).getName();
        player.sendMessage(ChatColor.YELLOW + "Leader: " + leaderName);
        player.sendMessage(ChatColor.YELLOW + "Created: " + guild.getCreationDate());
        
        // Members count
        player.sendMessage(ChatColor.YELLOW + "Members: " + guild.getMemberCount() + "/" + 
                plugin.getConfig().getInt("guilds.max-members", 20));
        
        // Claims count
        player.sendMessage(ChatColor.YELLOW + "Claims: " + guild.getClaimCount() + "/" + 
                plugin.getConfig().getInt("territory.max-claims", 10));
        
        // Home location
        if (guild.getHome() != null) {
            Location home = guild.getHome();
            player.sendMessage(ChatColor.YELLOW + "Home: " + home.getWorld().getName() + 
                    " (" + home.getBlockX() + ", " + home.getBlockY() + ", " + home.getBlockZ() + ")");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Home: Not set");
        }
    }

    /**
     * Handles the create command.
     *
     * @param player The player
     * @param args The command arguments
     */
    private void handleCreate(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /guild create <name>");
            return;
        }

        String guildName = args[0];
        
        // Check if name meets length requirements
        int minLength = plugin.getConfig().getInt("guilds.min-name-length", 3);
        int maxLength = plugin.getConfig().getInt("guilds.max-name-length", 16);
        
        if (guildName.length() < minLength || guildName.length() > maxLength) {
            player.sendMessage(ChatColor.RED + "Guild name must be between " + minLength + " and " + maxLength + " characters.");
            return;
        }
        
        // Check if name contains only allowed characters
        if (!guildName.matches("^[a-zA-Z0-9_]+$")) {
            player.sendMessage(ChatColor.RED + "Guild name can only contain letters, numbers, and underscores.");
            return;
        }
        
        // Check if player is already in a guild
        Guild existingPlayerGuild = guildService.getGuildByPlayer(player.getUniqueId());
        if (existingPlayerGuild != null) {
            player.sendMessage(ChatColor.RED + "You are already in a guild: " + existingPlayerGuild.getName());
            return;
        }
        
        // Check if guild name is already taken
        Guild existingGuild = guildService.getGuildByName(guildName);
        if (existingGuild != null) {
            player.sendMessage(ChatColor.RED + "A guild with that name already exists.");
            return;
        }
        
        // Check creation cooldown
        long cooldownTime = plugin.getConfig().getLong("guilds.creation-cooldown", 86400) * 1000; // Convert to milliseconds
        long currentTime = System.currentTimeMillis();
        Long lastCreationTime = creationCooldowns.get(player.getUniqueId());
        
        if (lastCreationTime != null && currentTime - lastCreationTime < cooldownTime) {
            long remainingTime = (lastCreationTime + cooldownTime - currentTime) / 1000; // Convert to seconds
            player.sendMessage(ChatColor.RED + "You must wait " + formatTime(remainingTime) + " before creating another guild.");
            return;
        }
        
        // Check economy if enabled
        double cost = plugin.getConfig().getDouble("guilds.creation-cost", 0);
        if (cost > 0 && plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            // This is a placeholder for Vault integration
            // In a real implementation, you would check if the player has enough money
            // and withdraw it if they do
            player.sendMessage(ChatColor.YELLOW + "Creating a guild costs $" + cost + ".");
        }
        
        // Create the guild
        Guild newGuild = guildService.createGuild(guildName, "", player.getUniqueId());
        
        if (newGuild != null) {
            // Update cooldown
            creationCooldowns.put(player.getUniqueId(), currentTime);
            
            // Send success messages
            player.sendMessage(ChatColor.GREEN + "Guild " + guildName + " has been created!");
            player.sendMessage(ChatColor.GREEN + "You are now the leader of this guild.");
            
            // Broadcast to server
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(ChatColor.YELLOW + "[Server] " + player.getName() + " has created a new guild: " + guildName + "!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create guild. Please try again later.");
        }
    }
    
    /**
     * Formats a time in seconds to a readable string.
     *
     * @param seconds The time in seconds
     * @return A formatted time string
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " hours";
        } else {
            return (seconds / 86400) + " days";
        }
    }

    /**
     * Handles the join command.
     *
     * @param player The player
     * @param args The command arguments
     */
    private void handleJoin(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /guild join <guild>");
            return;
        }

        String guildName = args[0];
        
        // Check if player is already in a guild
        Guild playerGuild = guildService.getGuildByPlayer(player.getUniqueId());
        if (playerGuild != null) {
            player.sendMessage(ChatColor.RED + "You are already in a guild: " + playerGuild.getName());
            return;
        }
        
        // Check if guild exists
        Guild targetGuild = guildService.getGuildByName(guildName);
        if (targetGuild == null) {
            player.sendMessage(ChatColor.RED + "No guild with that name exists.");
            return;
        }
        
        // Check if player is invited to the guild
        if (!guildService.isPlayerInvited(targetGuild.getId(), player.getUniqueId()) && 
                !player.hasPermission("guildwars.admin")) {
            player.sendMessage(ChatColor.RED + "You have not been invited to join this guild.");
            return;
        }
        
        // Check if guild has reached max members
        int maxMembers = plugin.getConfig().getInt("guilds.max-members", 20);
        if (targetGuild.getMemberCount() >= maxMembers) {
            player.sendMessage(ChatColor.RED + "This guild has reached its maximum member capacity.");
            return;
        }
        
        // Add player to guild
        if (guildService.addGuildMember(targetGuild.getId(), player.getUniqueId())) {
            // Remove invite
            guildService.removeGuildInvite(targetGuild.getId(), player.getUniqueId());
            
            player.sendMessage(ChatColor.GREEN + "You have joined the guild " + targetGuild.getName() + "!");
            
            // Notify online guild members
            for (UUID memberId : targetGuild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline() && !member.equals(player)) {
                    member.sendMessage(ChatColor.GREEN + player.getName() + " has joined your guild!");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to join guild. Please try again later.");
        }
    }

    /**
     * Handles the leave command.
     *
     * @param player The player
     */
    private void handleLeave(Player player) {
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is the leader
        if (guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "As the leader, you cannot leave your guild. You must either disband it or transfer leadership first.");
            player.sendMessage(ChatColor.YELLOW + "Use /guild disband to disband your guild.");
            return;
        }
        
        // Remove player from guild
        if (guildService.removeGuildMember(guild.getId(), player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "You have left the guild " + guild.getName() + ".");
            
            // Notify online guild members
            for (UUID memberId : guild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage(ChatColor.YELLOW + player.getName() + " has left your guild.");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to leave guild. Please try again later.");
        }
    }
    
    /**
     * Handles the info command.
     *
     * @param player The player
     * @param args The command arguments
     */
    private void handleInfo(Player player, String[] args) {
        if (args.length == 0) {
            // Show info for player's own guild
            showGuildInfo(player);
            return;
        }

        String guildName = args[0];
        
        // Check if guild exists
        Guild guild = guildService.getGuildByName(guildName);
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "No guild with that name exists.");
            return;
        }
        
        // Display guild information
        player.sendMessage(ChatColor.GOLD + "=== " + guild.getName() + " [" + guild.getTag() + "] ===");
        
        // Leader and creation date
        String leaderName = Bukkit.getOfflinePlayer(guild.getLeader()).getName();
        player.sendMessage(ChatColor.YELLOW + "Leader: " + leaderName);
        player.sendMessage(ChatColor.YELLOW + "Created: " + guild.getCreationDate());
        
        // Members count
        player.sendMessage(ChatColor.YELLOW + "Members: " + guild.getMemberCount() + "/" + 
                plugin.getConfig().getInt("guilds.max-members", 20));
        
        // Claims count
        player.sendMessage(ChatColor.YELLOW + "Claims: " + guild.getClaimCount() + "/" + 
                plugin.getConfig().getInt("territory.max-claims", 10));
        
        // List a few members (up to 5)
        player.sendMessage(ChatColor.YELLOW + "Members: ");
        int count = 0;
        StringBuilder memberList = new StringBuilder();
        
        // First add leader
        memberList.append("§c").append(leaderName).append(" (Leader)");
        count++;
        
        // Then officers
        for (UUID officerId : guild.getOfficers()) {
            if (count >= 5) break;
            if (!officerId.equals(guild.getLeader())) {
                String officerName = Bukkit.getOfflinePlayer(officerId).getName();
                if (count > 0) memberList.append(", ");
                memberList.append("§b").append(officerName).append(" (Officer)");
                count++;
            }
        }
        
        // Then regular members
        for (UUID memberId : guild.getMembers()) {
            if (count >= 5) break;
            if (!guild.isLeader(memberId) && !guild.isOfficer(memberId)) {
                String memberName = Bukkit.getOfflinePlayer(memberId).getName();
                if (count > 0) memberList.append(", ");
                memberList.append("§a").append(memberName);
                count++;
            }
        }
        
        player.sendMessage(memberList.toString());
        
        if (guild.getMemberCount() > 5) {
            player.sendMessage(ChatColor.GRAY + "... and " + (guild.getMemberCount() - 5) + " more");
        }
    }
    
    /**
     * Handles the invite command.
     *
     * @param player The player
     * @param args The command arguments
     */
    private void handleInvite(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /guild invite <player>");
            return;
        }

        String targetPlayerName = args[0];
        
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is officer or leader
        if (!guild.isOfficer(player.getUniqueId()) && !guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be an officer or leader to invite players.");
            return;
        }
        
        // Check if target player exists and is online
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player " + targetPlayerName + " is not online.");
            return;
        }
        
        // Check if target player is already in the guild
        if (guild.isMember(targetPlayer.getUniqueId())) {
            player.sendMessage(ChatColor.RED + targetPlayer.getName() + " is already in your guild.");
            return;
        }
        
        // Check if target player is already in another guild
        Guild targetPlayerGuild = guildService.getGuildByPlayer(targetPlayer.getUniqueId());
        if (targetPlayerGuild != null) {
            player.sendMessage(ChatColor.RED + targetPlayer.getName() + " is already in another guild.");
            return;
        }
        
        // Check if guild has reached max members
        int maxMembers = plugin.getConfig().getInt("guilds.max-members", 20);
        if (guild.getMemberCount() >= maxMembers) {
            player.sendMessage(ChatColor.RED + "Your guild has reached its maximum member capacity.");
            return;
        }
        
        // Check if player is already invited
        if (guild.isInvited(targetPlayer.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + targetPlayer.getName() + " has already been invited to your guild.");
            return;
        }
        
        // Add invite
        if (guildService.addGuildInvite(guild.getId(), targetPlayer.getUniqueId())) {
            // Notify the inviter
            player.sendMessage(ChatColor.GREEN + "Invited " + targetPlayer.getName() + " to your guild!");
            
            // Notify the invited player
            targetPlayer.sendMessage(ChatColor.GREEN + "You have been invited to join " + guild.getName() + "!");
            targetPlayer.sendMessage(ChatColor.YELLOW + "Type /guild join " + guild.getName() + " to accept the invitation.");
            
            // Notify online guild officers and leader
            for (UUID memberId : guild.getOfficers()) {
                Player officer = Bukkit.getPlayer(memberId);
                if (officer != null && officer.isOnline() && !officer.equals(player)) {
                    officer.sendMessage(ChatColor.YELLOW + player.getName() + " has invited " + targetPlayer.getName() + " to the guild.");
                }
            }
            
            if (!guild.isLeader(player.getUniqueId())) {
                Player leader = Bukkit.getPlayer(guild.getLeader());
                if (leader != null && leader.isOnline()) {
                    leader.sendMessage(ChatColor.YELLOW + player.getName() + " has invited " + targetPlayer.getName() + " to the guild.");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to invite player. Please try again later.");
        }
    }
    
    /**
     * Handles the kick command.
     *
     * @param player The player
     * @param args The command arguments
     */
    private void handleKick(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /guild kick <player>");
            return;
        }

        String targetPlayerName = args[0];
        
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is officer or leader
        if (!guild.isOfficer(player.getUniqueId()) && !guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be an officer or leader to kick players.");
            return;
        }
        
        // Get target player UUID
        UUID targetPlayerId = null;
        String targetPlayerDisplayName = targetPlayerName;
        
        // Try to get online player first
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            targetPlayerId = targetPlayer.getUniqueId();
            targetPlayerDisplayName = targetPlayer.getName();
        } else {
            // Try to find from offline players
            for (UUID memberId : guild.getMembers()) {
                String memberName = Bukkit.getOfflinePlayer(memberId).getName();
                if (memberName != null && memberName.equalsIgnoreCase(targetPlayerName)) {
                    targetPlayerId = memberId;
                    targetPlayerDisplayName = memberName;
                    break;
                }
            }
        }
        
        if (targetPlayerId == null) {
            player.sendMessage(ChatColor.RED + "Player " + targetPlayerName + " not found in your guild.");
            return;
        }
        
        // Check if target player is in the guild
        if (!guild.isMember(targetPlayerId)) {
            player.sendMessage(ChatColor.RED + targetPlayerDisplayName + " is not in your guild.");
            return;
        }
        
        // Check if target player is of lower rank
        if (guild.isLeader(targetPlayerId)) {
            player.sendMessage(ChatColor.RED + "You cannot kick the guild leader.");
            return;
        }
        
        if (guild.isOfficer(targetPlayerId) && !guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the guild leader can kick officers.");
            return;
        }
        
        // Remove player from guild
        if (guildService.removeGuildMember(guild.getId(), targetPlayerId)) {
            // Also remove from officers if they were an officer
            if (guild.isOfficer(targetPlayerId)) {
                guildService.removeGuildOfficer(guild.getId(), targetPlayerId);
            }
            
            player.sendMessage(ChatColor.GREEN + "Kicked " + targetPlayerDisplayName + " from your guild!");
            
            // Notify the kicked player if online
            if (targetPlayer != null && targetPlayer.isOnline()) {
                targetPlayer.sendMessage(ChatColor.RED + "You have been kicked from " + guild.getName() + " by " + player.getName() + ".");
            }
            
            // Notify online guild members
            for (UUID memberId : guild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline() && !member.equals(player)) {
                    member.sendMessage(ChatColor.YELLOW + targetPlayerDisplayName + " has been kicked from the guild by " + player.getName() + ".");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to kick player. Please try again later.");
        }
    }

    /**
     * Handles the promote command.
     *
     * @param player The player
     * @param args The command arguments
     */
    private void handlePromote(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /guild promote <player>");
            return;
        }

        String targetPlayerName = args[0];
        
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is the leader
        if (!guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the guild leader can promote members to officers.");
            return;
        }
        
        // Get target player UUID
        UUID targetPlayerId = null;
        String targetPlayerDisplayName = targetPlayerName;
        
        // Try to get online player first
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            targetPlayerId = targetPlayer.getUniqueId();
            targetPlayerDisplayName = targetPlayer.getName();
        } else {
            // Try to find from offline players
            for (UUID memberId : guild.getMembers()) {
                String memberName = Bukkit.getOfflinePlayer(memberId).getName();
                if (memberName != null && memberName.equalsIgnoreCase(targetPlayerName)) {
                    targetPlayerId = memberId;
                    targetPlayerDisplayName = memberName;
                    break;
                }
            }
        }
        
        if (targetPlayerId == null) {
            player.sendMessage(ChatColor.RED + "Player " + targetPlayerName + " not found in your guild.");
            return;
        }
        
        // Check if target player is in the guild
        if (!guild.isMember(targetPlayerId)) {
            player.sendMessage(ChatColor.RED + targetPlayerDisplayName + " is not in your guild.");
            return;
        }
        
        // Check if target player is the leader (trying to promote themselves)
        if (targetPlayerId.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must transfer " + guild.getName() + " to another user to change from leader.");
            return;
        }
        
        // Check if target player is already an officer
        if (guild.isOfficer(targetPlayerId)) {
            player.sendMessage(ChatColor.RED + targetPlayerDisplayName + " is already an officer.");
            return;
        }
        
        // Promote player to officer
        if (guildService.addGuildOfficer(guild.getId(), targetPlayerId)) {
            guild.addOfficer(targetPlayerId); // Update the in-memory guild object
            player.sendMessage(ChatColor.GREEN + "You have promoted " + targetPlayerDisplayName + " to officer.");
            
            // Notify the promoted player if online
            if (targetPlayer != null && targetPlayer.isOnline()) {
                targetPlayer.sendMessage(ChatColor.GREEN + "You have been promoted to officer in " + guild.getName() + " by " + player.getName() + ".");
            }
            
            // Notify online guild members
            for (UUID memberId : guild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline() && !member.equals(player) && !member.equals(targetPlayer)) {
                    member.sendMessage(ChatColor.YELLOW + targetPlayerDisplayName + " has been promoted to officer by " + player.getName() + ".");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to promote player. Please try again later.");
        }
    }

    /**
     * Handles the demote command.
     *
     * @param player The player
     * @param args The command arguments
     */
    private void handleDemote(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /guild demote <player>");
            return;
        }

        String targetPlayerName = args[0];
        
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is the leader
        if (!guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the guild leader can demote officers.");
            return;
        }
        
        // Get target player UUID
        UUID targetPlayerId = null;
        String targetPlayerDisplayName = targetPlayerName;
        
        // Try to get online player first
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            targetPlayerId = targetPlayer.getUniqueId();
            targetPlayerDisplayName = targetPlayer.getName();
        } else {
            // Try to find from offline players
            for (UUID memberId : guild.getMembers()) {
                String memberName = Bukkit.getOfflinePlayer(memberId).getName();
                if (memberName != null && memberName.equalsIgnoreCase(targetPlayerName)) {
                    targetPlayerId = memberId;
                    targetPlayerDisplayName = memberName;
                    break;
                }
            }
        }
        
        if (targetPlayerId == null) {
            player.sendMessage(ChatColor.RED + "Player " + targetPlayerName + " not found in your guild.");
            return;
        }
        
        // Check if target player is in the guild
        if (!guild.isMember(targetPlayerId)) {
            player.sendMessage(ChatColor.RED + targetPlayerDisplayName + " is not in your guild.");
            return;
        }
        
        // Check if target player is an officer
        if (!guild.isOfficer(targetPlayerId)) {
            player.sendMessage(ChatColor.RED + targetPlayerDisplayName + " is not an officer.");
            return;
        }
        
        // Demote player from officer
        if (guildService.removeGuildOfficer(guild.getId(), targetPlayerId)) {
            guild.removeOfficer(targetPlayerId); // Update the in-memory guild object
            player.sendMessage(ChatColor.GREEN + "You have demoted " + targetPlayerDisplayName + " to member.");
            
            // Notify the demoted player if online
            if (targetPlayer != null && targetPlayer.isOnline()) {
                targetPlayer.sendMessage(ChatColor.RED + "You have been demoted to member in " + guild.getName() + " by " + player.getName() + ".");
            }
            
            // Notify online guild members
            for (UUID memberId : guild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline() && !member.equals(player) && !member.equals(targetPlayer)) {
                    member.sendMessage(ChatColor.YELLOW + targetPlayerDisplayName + " has been demoted to member by " + player.getName() + ".");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to demote player. Please try again later.");
        }
    }
    
    /**
     * Handles the claim command.
     *
     * @param player The player
     */
    private void handleClaim(Player player) {
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is officer or leader
        if (!guild.isOfficer(player.getUniqueId()) && !guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be an officer or leader to claim land.");
            return;
        }
        
        // Get the chunk the player is standing in
        org.bukkit.Chunk chunk = player.getLocation().getChunk();
        com.guildwars.model.ChunkPosition chunkPos = new com.guildwars.model.ChunkPosition(
                chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        
        // Check if chunk is already claimed by this guild
        if (guildService.isChunkClaimed(chunkPos, guild.getId())) {
            player.sendMessage(ChatColor.RED + "This chunk is already claimed by your guild.");
            return;
        }
        
        // Check if chunk is claimed by another guild
        UUID ownerGuildId = guildService.getChunkOwnerId(chunkPos);
        if (ownerGuildId != null) {
            Guild ownerGuild = guildService.getGuild(ownerGuildId);
            String ownerName = ownerGuild != null ? ownerGuild.getName() : "another guild";
            player.sendMessage(ChatColor.RED + "This chunk is already claimed by " + ownerName + ".");
            return;
        }
        
        // Check if guild has any claims yet
        boolean hasAnyClaims = !guild.getClaims().isEmpty();
        
        // If guild has claims, check if the new claim is adjacent to existing claims
        if (hasAnyClaims && !guildService.isChunkAdjacentToClaim(chunkPos, guild.getId())) {
            player.sendMessage(ChatColor.RED + "You can only claim chunks that are adjacent to your existing territory.");
            return;
        }
        
        // Check if guild has reached maximum claims
        int maxClaims = plugin.getConfig().getInt("guild.max-claims", 50);
        if (guild.getClaims().size() >= maxClaims) {
            player.sendMessage(ChatColor.RED + "Your guild has reached the maximum number of claims (" + maxClaims + ").");
            return;
        }
        
        // Claim the chunk
        if (guildService.claimChunk(guild.getId(), chunkPos)) {
            guild.claim(chunkPos); // Update the in-memory guild object
            player.sendMessage(ChatColor.GREEN + "Claimed this chunk for your guild!");
            
            // Notify online guild members
            for (UUID memberId : guild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline() && !member.equals(player)) {
                    member.sendMessage(ChatColor.YELLOW + player.getName() + " claimed a new chunk at " + 
                            player.getLocation().getBlockX() + ", " + player.getLocation().getBlockZ());
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to claim chunk. Please try again later.");
        }
    }
    
    /**
     * Handles the unclaim command.
     *
     * @param player The player
     */
    private void handleUnclaim(Player player) {
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is officer or leader
        if (!guild.isOfficer(player.getUniqueId()) && !guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be an officer or leader to unclaim land.");
            return;
        }
        
        // Get the chunk the player is standing in
        org.bukkit.Chunk chunk = player.getLocation().getChunk();
        com.guildwars.model.ChunkPosition chunkPos = new com.guildwars.model.ChunkPosition(
                chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        
        // Check if chunk is claimed by this guild
        if (!guildService.isChunkClaimed(chunkPos, guild.getId())) {
            player.sendMessage(ChatColor.RED + "This chunk is not claimed by your guild.");
            return;
        }
        
        // Check if guild home is in this chunk
        Location home = guild.getHome();
        if (home != null) {
            org.bukkit.Chunk homeChunk = home.getChunk();
            if (homeChunk.getX() == chunk.getX() && homeChunk.getZ() == chunk.getZ() && 
                    homeChunk.getWorld().getName().equals(chunk.getWorld().getName())) {
                player.sendMessage(ChatColor.RED + "You cannot unclaim the chunk containing your guild home. Use /guild sethome first to move your home.");
                return;
            }
        }
        
        // Unclaim the chunk
        if (guildService.unclaimChunk(guild.getId(), chunkPos)) {
            guild.unclaim(chunkPos); // Update the in-memory guild object
            player.sendMessage(ChatColor.GREEN + "Unclaimed this chunk for your guild.");
            
            // Notify online guild members
            for (UUID memberId : guild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline() && !member.equals(player)) {
                    member.sendMessage(ChatColor.YELLOW + player.getName() + " unclaimed a chunk at " + 
                            chunkPos.getX() + ", " + chunkPos.getZ() + " in " + chunkPos.getWorld() + ".");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to unclaim chunk. Please try again later.");
        }
    }

    /**
     * @param player The player
     */
    private void handleHome(Player player) {
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if guild has a home set
        Location home = guild.getHome();
        if (home == null) {
            player.sendMessage(ChatColor.RED + "Your guild does not have a home set.");
            return;
        }
        
        // Check if teleport cooldown is active
        if (teleportCooldowns.containsKey(player.getUniqueId())) {
            long cooldownEnd = teleportCooldowns.get(player.getUniqueId());
            if (System.currentTimeMillis() < cooldownEnd) {
                long remainingTime = (cooldownEnd - System.currentTimeMillis()) / 1000;
                player.sendMessage(ChatColor.RED + "You must wait " + formatTime(remainingTime) + " before teleporting again.");
                return;
            } else {
                // Cooldown has expired, remove it
                teleportCooldowns.remove(player.getUniqueId());
            }
        }
        
        // Teleport player to guild home
        player.teleport(home);
        
        // Set cooldown (60 seconds)
        teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (60 * 1000));
        
        player.sendMessage(ChatColor.GREEN + "Teleported to guild home!");
    }

    /**
     * Handles the sethome command.
     *
     * @param player The player
     */
    private void handleSetHome(Player player) {
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is officer or leader
        if (!guild.isOfficer(player.getUniqueId()) && !guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be an officer or leader to set the guild home.");
            return;
        }
        
        // Get player's current location
        Location location = player.getLocation();
        
        // Check if player is in a claimed chunk
        ChunkPosition chunkPos = new ChunkPosition(location.getChunk());
        if (!guildService.isChunkClaimed(chunkPos, guild.getId())) {
            player.sendMessage(ChatColor.RED + "You can only set your guild home in a chunk claimed by your guild.");
            return;
        }
        
        // Set guild home
        if (guildService.setGuildHome(guild.getId(), location)) {
            player.sendMessage(ChatColor.GREEN + "Guild home set to your current location.");
            
            // Notify online guild members
            for (UUID memberId : guild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline() && !member.equals(player)) {
                    member.sendMessage(ChatColor.YELLOW + player.getName() + " has set a new guild home.");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to set guild home. Please try again later.");
        }
    }

    /**
     * Handles the ally command.
     *
     * @param player The player
     * @param args The command arguments
     */
    private void handleAlly(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /guild ally <guild>");
            return;
        }

        String targetGuildName = args[0];
        
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is the leader
        if (!guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the guild leader can set alliances.");
            return;
        }
        
        // Check if target guild exists
        Guild targetGuild = guildService.getGuildByName(targetGuildName);
        if (targetGuild == null) {
            player.sendMessage(ChatColor.RED + "Guild " + targetGuildName + " does not exist.");
            return;
        }
        
        // Check if target guild is the same as player's guild
        if (guild.getId().equals(targetGuild.getId())) {
            player.sendMessage(ChatColor.RED + "You cannot ally with your own guild.");
            return;
        }
        
        // Check if guilds are already allied
        if (guild.getRelation(targetGuild.getId()) == Relation.ALLY) {
            player.sendMessage(ChatColor.RED + "Your guild is already allied with " + targetGuild.getName() + ".");
            return;
        }
        
        // Check if guild has reached max alliances
        int maxAlliances = plugin.getConfig().getInt("guild.max-alliances", 3);
        int currentAlliances = 0;
        
        for (String otherGuildId : guild.getRelations().keySet()) {
            if (guild.getRelation(otherGuildId) == Relation.ALLY) {
                currentAlliances++;
            }
        }
        
        if (currentAlliances >= maxAlliances) {
            player.sendMessage(ChatColor.RED + "Your guild has reached the maximum number of alliances (" + maxAlliances + ").");
            return;
        }
        
        // Set relation to ALLY
        if (guildService.setGuildRelation(guild.getId(), targetGuild.getId(), Relation.ALLY)) {
            player.sendMessage(ChatColor.GREEN + "Your guild is now allied with " + targetGuild.getName() + "!");
            
            // Notify online members of both guilds
            for (UUID memberId : guild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline() && !member.equals(player)) {
                    member.sendMessage(ChatColor.YELLOW + "Your guild is now allied with " + targetGuild.getName() + "!");
                }
            }
            
            for (UUID memberId : targetGuild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage(ChatColor.YELLOW + guild.getName() + " has formed an alliance with your guild!");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to set alliance. Please try again later.");
        }
    }

    /**
     * Handles the enemy command.
     *
     * @param player The player
     * @param args The command arguments
     */
    private void handleEnemy(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /guild enemy <guild>");
            return;
        }

        String targetGuildName = args[0];
        
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is the leader
        if (!guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the guild leader can declare enemies.");
            return;
        }
        
        // Check if target guild exists
        Guild targetGuild = guildService.getGuildByName(targetGuildName);
        if (targetGuild == null) {
            player.sendMessage(ChatColor.RED + "Guild " + targetGuildName + " does not exist.");
            return;
        }
        
        // Check if target guild is the same as player's guild
        if (guild.getId().equals(targetGuild.getId())) {
            player.sendMessage(ChatColor.RED + "You cannot declare your own guild as an enemy.");
            return;
        }
        
        // Check if guilds are already enemies
        if (guild.getRelation(targetGuild.getId()) == Relation.ENEMY) {
            player.sendMessage(ChatColor.RED + "Your guild already considers " + targetGuild.getName() + " an enemy.");
            return;
        }
        
        // Check if guild has reached max enemies
        int maxEnemies = plugin.getConfig().getInt("guild.max-enemies", 5);
        int currentEnemies = 0;
        
        for (String otherGuildId : guild.getRelations().keySet()) {
            if (guild.getRelation(otherGuildId) == Relation.ENEMY) {
                currentEnemies++;
            }
        }
        
        if (currentEnemies >= maxEnemies) {
            player.sendMessage(ChatColor.RED + "Your guild has reached the maximum number of enemies (" + maxEnemies + ").");
            return;
        }
        
        // Set relation to ENEMY
        if (guildService.setGuildRelation(guild.getId(), targetGuild.getId(), Relation.ENEMY)) {
            player.sendMessage(ChatColor.RED + "Your guild has declared " + targetGuild.getName() + " as an enemy!");
            
            // Notify online members of both guilds
            for (UUID memberId : guild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline() && !member.equals(player)) {
                    member.sendMessage(ChatColor.YELLOW + "Your guild has declared " + targetGuild.getName() + " as an enemy!");
                }
            }
            
            for (UUID memberId : targetGuild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage(ChatColor.RED + guild.getName() + " has declared your guild as an enemy!");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to declare enemy. Please try again later.");
        }
    }

    /**
     * Handles the war command.
     *
     * @param player The player
     * @param args The command arguments
     */
    private void handleWar(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /guild war <guild> [duration]");
            return;
        }

        String targetGuildName = args[0];
        int duration = plugin.getConfig().getInt("war.min-duration", 30);
        
        if (args.length > 1) {
            try {
                duration = Integer.parseInt(args[1]);
                
                // Check duration limits
                int minDuration = plugin.getConfig().getInt("war.min-duration", 30);
                int maxDuration = plugin.getConfig().getInt("war.max-duration", 120);
                
                if (duration < minDuration || duration > maxDuration) {
                    player.sendMessage(ChatColor.RED + "War duration must be between " + minDuration + " and " + maxDuration + " minutes.");
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid duration. Must be a number in minutes.");
                return;
            }
        }
        
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is officer or leader
        if (!guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the guild leader can declare war.");
            return;
        }
        
        // Check if target guild exists
        Guild targetGuild = guildService.getGuildByName(targetGuildName);
        if (targetGuild == null) {
            player.sendMessage(ChatColor.RED + "Guild " + targetGuildName + " does not exist.");
            return;
        }
        
        // Check if target guild is the same as player's guild
        if (guild.getId().equals(targetGuild.getId())) {
            player.sendMessage(ChatColor.RED + "You cannot declare war on your own guild.");
            return;
        }
        
        // Check if already at war
        if (guild.getRelation(targetGuild.getId()) == Relation.WAR) {
            player.sendMessage(ChatColor.RED + "Your guild is already at war with " + targetGuild.getName() + ".");
            return;
        }
        
        // Check if war cooldown is active
        if (warCooldowns.containsKey(player.getUniqueId())) {
            long cooldownEnd = warCooldowns.get(player.getUniqueId());
            if (System.currentTimeMillis() < cooldownEnd) {
                long remainingTime = (cooldownEnd - System.currentTimeMillis()) / 1000;
                player.sendMessage(ChatColor.RED + "You must wait " + formatTime(remainingTime) + " before declaring another war.");
                return;
            } else {
                // Cooldown has expired, remove it
                warCooldowns.remove(player.getUniqueId());
            }
        }
        
        // Set relation to WAR
        if (guildService.setGuildRelation(guild.getId(), targetGuild.getId(), Relation.WAR)) {
            // Set cooldown (30 minutes)
            warCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (30 * 60 * 1000));
            
            player.sendMessage(ChatColor.RED + "Your guild has declared war on " + targetGuild.getName() + " for " + duration + " minutes!");
            
            // Notify online members of both guilds
            for (UUID memberId : guild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline() && !member.equals(player)) {
                    member.sendMessage(ChatColor.YELLOW + "Your guild has declared war on " + targetGuild.getName() + " for " + duration + " minutes!");
                }
            }
            
            for (UUID memberId : targetGuild.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage(ChatColor.RED + guild.getName() + " has declared war on your guild for " + duration + " minutes!");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to declare war. Please try again later.");
        }
    }
    
    /**
     * Handles the disband command.
     *
     * @param player The player disbanding the guild
     */
    private void handleDisband(Player player) {
        // Check if player is in a guild
        Guild guild = guildService.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }
        
        // Check if player is the guild leader
        if (!guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the guild leader can disband the guild.");
            return;
        }
        
        // Store guild info before deletion for notifications
        String guildName = guild.getName();
        Set<UUID> guildMembers = new HashSet<>(guild.getMembers());
        
        // Delete the guild from the database
        try {
            if (guildService.deleteGuild(UUID.fromString(guild.getId()))) {
                // Notify all online guild members AFTER successful deletion
                for (UUID memberId : guildMembers) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage(ChatColor.RED + "Your guild has been disbanded by " + player.getName() + "!");
                    }
                }
                
                // Broadcast to server
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(ChatColor.YELLOW + "[Server] The guild " + guildName + " has been disbanded!");
                }
                
                // Log the action
                plugin.getLogger().info("Player " + player.getName() + " disbanded guild: " + guildName);
            } else {
                player.sendMessage(ChatColor.RED + "Failed to disband guild. Please try again later.");
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Failed to disband guild: Invalid guild ID format.");
            plugin.getLogger().warning("Failed to disband guild with ID " + guild.getId() + ": " + e.getMessage());
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player)) {
            return completions;
        }
        
        if (args.length == 1) {
            // First argument - subcommands
            String[] subCommands = {
                "create", "join", "leave", "info", "invite", "kick", 
                "promote", "demote", "claim", "unclaim", "home", "sethome", 
                "ally", "enemy", "war", "disband"
            };
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String input = args[1].toLowerCase();
            
            switch (subCommand) {
                case "info":
                case "join":
                case "ally":
                case "enemy":
                case "war":
                    // Complete with guild names
                    List<String> guildNames = guildService.getAllGuilds().stream()
                            .map(Guild::getName)
                            .filter(name -> name.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
                    completions.addAll(guildNames);
                    break;
                case "invite":
                case "kick":
                case "promote":
                case "demote":
                    // Complete with player names
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getName().toLowerCase().startsWith(input)) {
                            completions.add(onlinePlayer.getName());
                        }
                    }
                    break;
                case "create":
                    // Suggest a guild name if they haven't typed anything yet
                    if (input.isEmpty()) {
                        completions.add("<guildname>");
                    }
                    break;
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            String input = args[2].toLowerCase();
            
            if (subCommand.equals("create")) {
                // Suggest a tag if they haven't typed anything yet
                if (input.isEmpty()) {
                    completions.add("<tag>");
                }
            } else if (subCommand.equals("war")) {
                // Suggest durations for war
                String[] durations = {"30", "60", "90", "120"};
                for (String duration : durations) {
                    if (duration.startsWith(input)) {
                        completions.add(duration);
                    }
                }
            }
        }
        
        return completions;
    }
}
