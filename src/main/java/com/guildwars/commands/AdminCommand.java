package com.guildwars.commands;

import com.guildwars.GuildWars;
import com.guildwars.database.GuildService;
import com.guildwars.enchantments.CustomEnchantmentManager;
import com.guildwars.enchantments.CustomEnchantmentType;
import com.guildwars.model.Guild;
import com.guildwars.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command handler for the /guildadmin command.
 * Provides administrative functions for managing guilds.
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final GuildWars plugin;
    private final GuildService guildService;

    /**
     * Creates a new admin command handler.
     *
     * @param plugin The GuildWars plugin instance
     */
    public AdminCommand(GuildWars plugin) {
        this.plugin = plugin;
        this.guildService = plugin.getGuildService();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has admin permission
        if (!sender.hasPermission("guildwars.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "delete":
                handleDelete(sender, subArgs);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "about":
                handleAbout(sender);
                break;
            case "enchant":
                handleEnchant(sender, subArgs);
                break;
            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    /**
     * Shows help information for the admin command.
     *
     * @param sender The command sender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== GuildWars Admin Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/guildadmin delete <guild> " + ChatColor.WHITE + "- Delete a guild");
        sender.sendMessage(ChatColor.YELLOW + "/guildadmin reload " + ChatColor.WHITE + "- Reload the plugin configuration");
        sender.sendMessage(ChatColor.YELLOW + "/guildadmin about " + ChatColor.WHITE + "- Display plugin information");
        sender.sendMessage(ChatColor.YELLOW + "/guildadmin enchant <type> [level] " + ChatColor.WHITE + "- Add a custom enchantment to the held item");
    }

    /**
     * Handles the delete command.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /guildadmin delete <guild>");
            return;
        }

        String guildName = args[0];
        Guild guild = guildService.getGuildByName(guildName);

        if (guild == null) {
            sender.sendMessage(ChatColor.RED + "Guild not found: " + guildName);
            return;
        }

        // Store guild info before deletion for notifications
        String deletedGuildName = guild.getName();
        Set<UUID> guildMembers = new HashSet<>(guild.getMembers());

        // Delete the guild from the database
        try {
            if (guildService.deleteGuild(UUID.fromString(guild.getId()))) {
                // Notify all online guild members
                for (UUID memberId : guildMembers) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage(ChatColor.RED + "Your guild has been deleted by an administrator!");
                    }
                }

                // Broadcast to server
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(ChatColor.YELLOW + "[Server] The guild " + deletedGuildName + " has been deleted by an administrator.");
                }

                // Log the action
                plugin.getLogger().info("Admin " + sender.getName() + " deleted guild: " + deletedGuildName);
                
                // Notify the admin
                sender.sendMessage(ChatColor.GREEN + "Successfully deleted guild: " + deletedGuildName);
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to delete guild. Please try again later.");
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Failed to delete guild: Invalid guild ID format.");
            plugin.getLogger().warning("Failed to delete guild with ID " + guild.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Handles the reload command.
     *
     * @param sender The command sender
     */
    private void handleReload(CommandSender sender) {
        // Reload the configuration
        plugin.reloadConfig();
        
        // Reload message utility
        MessageUtil.reload();
        
        // Reload placeholders
        plugin.reloadPlaceholders();
        
        sender.sendMessage(ChatColor.GREEN + "GuildWars configuration reloaded successfully!");
        plugin.getLogger().info("Plugin configuration reloaded by " + sender.getName());
    }
    
    /**
     * Handles the about command.
     * Displays information about the plugin and server.
     *
     * @param sender The command sender
     */
    private void handleAbout(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== GuildWars Plugin Information ===");
        // Get plugin version using the plugin's description file
        // Note: We're using the plugin's description file directly to avoid the deprecated method
        String version = "1.1-SNAPSHOT";
        try {
            // This approach avoids using the deprecated getDescription() method
            java.io.InputStream resource = plugin.getResource("plugin.yml");
            if (resource != null) {
                org.bukkit.configuration.file.YamlConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                        new java.io.InputStreamReader(resource));
                version = config.getString("version", version);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not read plugin version from plugin.yml: " + e.getMessage());
        }
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + version);
        sender.sendMessage(ChatColor.YELLOW + "Developer: " + ChatColor.WHITE + "DakotaFryberger");
        
        // Database information
        // The plugin is currently using YAML files for storage
        sender.sendMessage(ChatColor.YELLOW + "Database: " + ChatColor.WHITE + "YAML");
        
        // Server information
        String serverVersion = Bukkit.getVersion();
        sender.sendMessage(ChatColor.YELLOW + "Server Version: " + ChatColor.WHITE + serverVersion);
        
        // Determine server type (Paper/Spigot)
        String serverType = "Unknown";
        try {
            // Check for Paper
            Class.forName("com.destroystokyo.paper.PaperConfig");
            serverType = "Paper";
        } catch (ClassNotFoundException e) {
            // Check for Spigot
            try {
                Class.forName("org.spigotmc.SpigotConfig");
                serverType = "Spigot";
            } catch (ClassNotFoundException ex) {
                // Default to Bukkit if neither is found
                serverType = "Bukkit";
            }
        }
        sender.sendMessage(ChatColor.YELLOW + "Server Type: " + ChatColor.WHITE + serverType);
        
        // Additional information
        sender.sendMessage(ChatColor.YELLOW + "Java Version: " + ChatColor.WHITE + System.getProperty("java.version"));
        sender.sendMessage(ChatColor.YELLOW + "Support: " + ChatColor.WHITE + "https://discord.gg/FGUEEj6k7k");
    }
    
    /**
     * Handles the enchant command.
     * Adds a custom enchantment to the item the player is holding.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void handleEnchant(CommandSender sender, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }
        
        Player player = (Player) sender;
        
        // Check arguments
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /guildadmin enchant <type> [level]");
            sender.sendMessage(ChatColor.YELLOW + "Available enchantment types: HARVESTER, TUNNELING");
            return;
        }
        
        // Get the enchantment type
        String typeArg = args[0].toUpperCase();
        CustomEnchantmentType type;
        try {
            type = CustomEnchantmentType.valueOf(typeArg);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid enchantment type: " + args[0]);
            sender.sendMessage(ChatColor.YELLOW + "Available enchantment types: HARVESTER, TUNNELING");
            return;
        }
        
        // Get the enchantment level
        int level = 1;
        if (args.length > 1) {
            try {
                level = Integer.parseInt(args[1]);
                if (level < 1 || level > type.getMaxLevel()) {
                    sender.sendMessage(ChatColor.RED + "Invalid level. Must be between 1 and " + type.getMaxLevel() + ".");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid level: " + args[1] + ". Must be a number.");
                return;
            }
        }
        
        // Get the item in the player's hand
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            sender.sendMessage(ChatColor.RED + "You must be holding an item to enchant it.");
            return;
        }
        
        // Check if the enchantment can be applied to this item
        if (!type.canEnchantItem(item)) {
            sender.sendMessage(ChatColor.RED + "This enchantment cannot be applied to this item.");
            if (type == CustomEnchantmentType.HARVESTER) {
                sender.sendMessage(ChatColor.YELLOW + "Harvester can only be applied to hoes.");
            } else if (type == CustomEnchantmentType.TUNNELING) {
                sender.sendMessage(ChatColor.YELLOW + "Tunneling can only be applied to pickaxes.");
            }
            return;
        }
        
        // Add the enchantment to the item
        CustomEnchantmentManager enchantmentManager = plugin.getEnchantmentManager();
        ItemStack enchantedItem = enchantmentManager.addEnchantment(item, type, level);
        
        // Update the player's inventory
        player.getInventory().setItemInMainHand(enchantedItem);
        
        // Send success message
        sender.sendMessage(ChatColor.GREEN + "Added " + type.getFormattedName(level) + ChatColor.GREEN + " to your item.");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // Check if sender has admin permission
        if (!sender.hasPermission("guildwars.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            // First argument - subcommands
            String[] subCommands = {"delete", "reload", "about", "enchant"};
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete")) {
                // Second argument for delete - guild names
                String input = args[1].toLowerCase();
                
                // Get all guild names from the guild service
                List<String> guildNames = guildService.getAllGuilds().stream()
                        .map(Guild::getName)
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());
                
                completions.addAll(guildNames);
            } else if (args[0].equalsIgnoreCase("enchant")) {
                // Second argument for enchant - enchantment types
                String input = args[1].toLowerCase();
                
                for (CustomEnchantmentType type : CustomEnchantmentType.values()) {
                    if (type.name().toLowerCase().startsWith(input)) {
                        completions.add(type.name().toLowerCase());
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("enchant")) {
            // Third argument for enchant - level
            String input = args[2].toLowerCase();
            
            try {
                CustomEnchantmentType type = CustomEnchantmentType.valueOf(args[1].toUpperCase());
                for (int i = 1; i <= type.getMaxLevel(); i++) {
                    if (String.valueOf(i).startsWith(input)) {
                        completions.add(String.valueOf(i));
                    }
                }
            } catch (IllegalArgumentException e) {
                // Invalid enchantment type, no completions
            }
        }
        
        return completions;
    }
}
