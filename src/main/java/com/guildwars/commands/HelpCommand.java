package com.guildwars.commands;

import com.guildwars.GuildWars;
import com.guildwars.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Command handler for the /guildhelp command.
 */
public class HelpCommand implements CommandExecutor {

    private final GuildWars plugin;

    /**
     * Constructs a new HelpCommand.
     *
     * @param plugin The plugin instance
     */
    public HelpCommand(GuildWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.getLogger().info("HelpCommand executed by " + sender.getName());

        if (args.length == 0) {
            sendMainHelp(sender);
            return true;
        }

        String topic = args[0].toLowerCase();
        switch (topic) {
            case "create":
                sendCreateHelp(sender);
                break;
            case "join":
                sendJoinHelp(sender);
                break;
            case "leave":
                sendLeaveHelp(sender);
                break;
            case "invite":
                sendInviteHelp(sender);
                break;
            case "kick":
                sendKickHelp(sender);
                break;
            case "promote":
                sendPromoteHelp(sender);
                break;
            case "demote":
                sendDemoteHelp(sender);
                break;
            case "claim":
                sendClaimHelp(sender);
                break;
            case "unclaim":
                sendUnclaimHelp(sender);
                break;
            case "home":
                sendHomeHelp(sender);
                break;
            case "sethome":
                sendSetHomeHelp(sender);
                break;
            case "ally":
                sendAllyHelp(sender);
                break;
            case "enemy":
                sendEnemyHelp(sender);
                break;
            case "war":
                sendWarHelp(sender);
                break;
            case "info":
                sendInfoHelp(sender);
                break;
            case "disband":
                sendDisbandHelp(sender);
                break;
            case "support":
                sendSupportHelp(sender);
                break;
            default:
                sendUnknownHelp(sender, topic);
                break;
        }

        return true;
    }

    /**
     * Sends the main help message.
     *
     * @param sender The command sender
     */
    private void sendMainHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== GuildWars Help ===");
        MessageUtil.sendInfo(sender, "GuildWars is a factions plugin for Minecraft. - Powered By Frostline Solutions LLC");
        MessageUtil.sendInfo(sender, "Developed by Dakota Fryberger");
        sender.sendMessage("§2Available Commands:");
        sender.sendMessage("§b/guild create <name> - Create a new guild");
        sender.sendMessage("§b/guild join <guild> - Join a guild");
        sender.sendMessage("§b/guild leave - Leave your current guild");
        sender.sendMessage("§b/guild invite <player> - Invite a player to your guild");
        sender.sendMessage("§b/guild kick <player> - Kick a player from your guild");
        sender.sendMessage("§b/guild promote <player> - Promote a player in your guild");
        sender.sendMessage("§b/guild demote <player> - Demote a player in your guild");
        sender.sendMessage("§b/guild claim - Claim the chunk you're standing in");
        sender.sendMessage("§b/guild unclaim - Unclaim the chunk you're standing in");
        sender.sendMessage("§b/guild home - Teleport to your guild's home");
        sender.sendMessage("§b/guild sethome - Set your guild's home");
        sender.sendMessage("§b/guild ally <guild> - Ally with another guild");
        sender.sendMessage("§b/guild enemy <guild> - Declare another guild as an enemy");
        sender.sendMessage("§b/guild war <guild> - Declare war on another guild");
        sender.sendMessage("§b/guild disband - Permanently delete your guild");
        sender.sendMessage("§b/guilds - List all guilds on the server");
        sender.sendMessage("§b/guildhelp [topic] - Show help for a specific topic");
        sender.sendMessage("§b/support - Get support for the GuildWars plugin");
        MessageUtil.sendInfo(sender, "For more detailed help, type /guildhelp <command>");
    }

    /**
     * Sends help for the create command.
     *
     * @param sender The command sender
     */
    private void sendCreateHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Creation Help ===");
        MessageUtil.sendInfo(sender, "Create a new guild with a unique name.");
        sender.sendMessage("§2Usage: /guild create <name>");
        sender.sendMessage("§bExample: /guild create MyGuild");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must not already be in a guild");
        sender.sendMessage("§d- Guild name must be between 3-16 characters");
        sender.sendMessage("§d- Guild name must not already be taken");
        sender.sendMessage("§d- Guild name must not contain special characters");
        sender.sendMessage("§d- You must have enough money to create a guild");
    }

    /**
     * Sends help for the join command.
     *
     * @param sender The command sender
     */
    private void sendJoinHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Join Help ===");
        MessageUtil.sendInfo(sender, "Join an existing guild.");
        sender.sendMessage("§2Usage: /guild join <guild>");
        sender.sendMessage("§bExample: /guild join MyGuild");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must not already be in a guild");
        sender.sendMessage("§d- You must have been invited to the guild");
    }

    /**
     * Sends help for the leave command.
     *
     * @param sender The command sender
     */
    private void sendLeaveHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Leave Help ===");
        MessageUtil.sendInfo(sender, "Leave your current guild.");
        sender.sendMessage("§2Usage: /guild leave");
        sender.sendMessage("§bExample: /guild leave");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be in a guild");
    }

    /**
     * Sends help for the invite command.
     *
     * @param sender The command sender
     */
    private void sendInviteHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Invite Help ===");
        MessageUtil.sendInfo(sender, "Invite a player to your guild.");
        sender.sendMessage("§2Usage: /guild invite <player>");
        sender.sendMessage("§bExample: /guild invite Steve");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be in a guild");
        sender.sendMessage("§d- You must have permission to invite players");
    }

    /**
     * Sends help for the kick command.
     *
     * @param sender The command sender
     */
    private void sendKickHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Kick Help ===");
        MessageUtil.sendInfo(sender, "Kick a player from your guild.");
        sender.sendMessage("§2Usage: /guild kick <player>");
        sender.sendMessage("§bExample: /guild kick Steve");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be in a guild");
        sender.sendMessage("§d- You must have permission to kick players");
        sender.sendMessage("§d- The player must be in your guild");
        sender.sendMessage("§d- You cannot kick players of higher rank");
    }

    /**
     * Sends help for the promote command.
     *
     * @param sender The command sender
     */
    private void sendPromoteHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Promote Help ===");
        MessageUtil.sendInfo(sender, "Promote a player in your guild.");
        sender.sendMessage("§2Usage: /guild promote <player>");
        sender.sendMessage("§bExample: /guild promote Steve");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be the leader of your guild");
        sender.sendMessage("§d- The player must be a member of your guild");
        sender.sendMessage("§d- The player must not already be at the highest rank");
    }

    /**
     * Sends help for the demote command.
     *
     * @param sender The command sender
     */
    private void sendDemoteHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Demote Help ===");
        MessageUtil.sendInfo(sender, "Demote a player in your guild.");
        sender.sendMessage("§2Usage: /guild demote <player>");
        sender.sendMessage("§bExample: /guild demote Steve");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be the leader of your guild");
        sender.sendMessage("§d- The player must be an officer in your guild");
        sender.sendMessage("§d- Officers are demoted to members");
    }

    /**
     * Sends help for the claim command.
     *
     * @param sender The command sender
     */
    private void sendClaimHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Claim Help ===");
        MessageUtil.sendInfo(sender, "Claim the chunk you're standing in for your guild.");
        sender.sendMessage("§2Usage: /guild claim");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be an officer or leader of the guild");
        sender.sendMessage("§d- The chunk must not already be claimed by another guild");
        sender.sendMessage("§d- Your guild must have fewer than " + plugin.getConfig().getInt("territory.max-claims", 10) + " claims");
        sender.sendMessage("§d- Claiming costs " + plugin.getConfig().getDouble("territory.claim-cost", 100) + " per chunk (if economy is enabled)");
    }

    /**
     * Sends help for the unclaim command.
     *
     * @param sender The command sender
     */
    private void sendUnclaimHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Unclaim Help ===");
        MessageUtil.sendInfo(sender, "Unclaim the chunk you're standing in.");
        sender.sendMessage("§2Usage: /guild unclaim");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be an officer or leader of the guild");
        sender.sendMessage("§d- The chunk must be claimed by your guild");
    }

    /**
     * Sends help for the home command.
     *
     * @param sender The command sender
     */
    private void sendHomeHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Home Help ===");
        MessageUtil.sendInfo(sender, "Teleport to your guild's home.");
        sender.sendMessage("§2Usage: /guild home");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be a member of a guild");
        sender.sendMessage("§d- Your guild must have set a home");
        sender.sendMessage("§d- There is a " + plugin.getConfig().getInt("guilds.home.teleport-delay", 3) + " second delay before teleporting");
        sender.sendMessage("§d- There is a " + plugin.getConfig().getInt("guilds.home.teleport-cooldown", 60) + " second cooldown between teleports");
    }

    /**
     * Sends help for the sethome command.
     *
     * @param sender The command sender
     */
    private void sendSetHomeHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Set Home Help ===");
        MessageUtil.sendInfo(sender, "Set your guild's home to your current location.");
        sender.sendMessage("§2Usage: /guild sethome");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be an officer or leader of the guild");
        sender.sendMessage("§d- You must be in a claimed chunk of your guild");
    }

    /**
     * Sends help for the ally command.
     *
     * @param sender The command sender
     */
    private void sendAllyHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Ally Help ===");
        MessageUtil.sendInfo(sender, "Form an alliance with another guild.");
        sender.sendMessage("§2Usage: /guild ally <guild>");
        sender.sendMessage("§bExample: /guild ally Defenders");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be the leader of your guild");
        sender.sendMessage("§d- The other guild must accept your alliance request");
        sender.sendMessage("§d- You cannot ally with a guild you are at war with");
    }

    /**
     * Sends help for the enemy command.
     *
     * @param sender The command sender
     */
    private void sendEnemyHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Enemy Help ===");
        MessageUtil.sendInfo(sender, "Declare another guild as an enemy.");
        sender.sendMessage("§2Usage: /guild enemy <guild>");
        sender.sendMessage("§bExample: /guild enemy Raiders");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be the leader of your guild");
        sender.sendMessage("§d- You cannot declare an ally as an enemy");
    }

    /**
     * Sends help for the war command.
     *
     * @param sender The command sender
     */
    private void sendWarHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild War Help ===");
        MessageUtil.sendInfo(sender, "Declare war on another guild.");
        sender.sendMessage("§2Usage: /guild war <guild> [duration]");
        sender.sendMessage("§bExample: /guild war Raiders 24");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be the leader of your guild");
        sender.sendMessage("§d- You cannot declare war on an ally");
        sender.sendMessage("§d- Duration is in hours (default: 24)");
        sender.sendMessage("§d- War declaration costs " + 
                plugin.getConfig().getDouble("guilds.war.cost", 500) + " (if economy is enabled)");
    }

    /**
     * Sends help for the info command.
     *
     * @param sender The command sender
     */
    private void sendInfoHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Info Help ===");
        MessageUtil.sendInfo(sender, "View information about a guild.");
        sender.sendMessage("§2Usage: /guild info [guild]");
        sender.sendMessage("§bExample: /guild info Defenders");
        sender.sendMessage("§cNotes:");
        sender.sendMessage("§d- If no guild is specified, shows info about your current guild");
        sender.sendMessage("§d- Shows members, claims, allies, enemies, and wars");
    }
    
    /**
     * Sends help for the disband command.
     *
     * @param sender The command sender
     */
    private void sendDisbandHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Guild Disband Help ===");
        MessageUtil.sendInfo(sender, "Permanently delete your guild.");
        sender.sendMessage("§2Usage: /guild disband");
        sender.sendMessage("§cRequirements:");
        sender.sendMessage("§d- You must be the leader of the guild");
        sender.sendMessage("§cWarning:");
        sender.sendMessage("§d- This action cannot be undone");
        sender.sendMessage("§d- All guild data, claims, and relationships will be permanently deleted");
    }
    
    /**
     * Sends help for an unknown topic.
     *
     * @param sender The command sender
     * @param topic The unknown topic
     */
    /**
     * Sends help for the support command.
     *
     * @param sender The command sender
     */
    private void sendSupportHelp(CommandSender sender) {
        MessageUtil.sendTitle(sender, "=== Support Help ===");
        MessageUtil.sendInfo(sender, "Get support for the GuildWars plugin.");
        sender.sendMessage("§2Usage: /support");
        sender.sendMessage("§cNotes:");
        sender.sendMessage("§d- Provides a link to the Frostline Discord server");
        sender.sendMessage("§d- You can click the link in chat to join the Discord server");
        sender.sendMessage("§d- Discord: https://discord.gg/FGUEEj6k7k");
    }
    
    /**
     * Sends help for an unknown topic.
     *
     * @param sender The command sender
     * @param topic The unknown topic
     */
    private void sendUnknownHelp(CommandSender sender, String topic) {
        MessageUtil.sendError(sender, "Unknown help topic: " + topic);
        MessageUtil.sendInfo(sender, "Type /guildhelp for a list of available topics.");
    }
}
