package com.guildwars.commands;

import com.guildwars.GuildWars;
import com.guildwars.enchantments.CustomEnchantmentManager;
import com.guildwars.enchantments.CustomEnchantmentType;
import com.guildwars.mobs.CustomMobManager;
import com.guildwars.model.Guild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final CustomEnchantmentManager enchantmentManager;
    private final CustomMobManager mobManager;

    /**
     * Creates a new admin command handler.
     *
     * @param plugin The GuildWars plugin instance
     */
    public AdminCommand(GuildWars plugin, CustomEnchantmentManager enchantmentManager, CustomMobManager mobManager) {
        this.plugin = plugin;
        this.enchantmentManager = enchantmentManager;
        this.mobManager = mobManager;
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
            case "give":
                handleGive(sender, subArgs);
                break;
            case "summon":
                handleSummon(sender, subArgs);
                break;
            case "godmode":
                handleGodMode(sender);
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
        sender.sendMessage(ChatColor.YELLOW + "/guildadmin summon <mobType> " + ChatColor.WHITE + "- Summon a custom mob at your location");
        sender.sendMessage(ChatColor.YELLOW + "/guildadmin give <book_type> [level] " + ChatColor.WHITE + "- Give yourself an enchanted book");
        sender.sendMessage(ChatColor.YELLOW + "/guildadmin godmode " + ChatColor.WHITE + "- Toggle godmode (full immunity and invisibility to mobs)");
        sender.sendMessage(ChatColor.YELLOW + "/guildadmin summon <mob_type> " + ChatColor.WHITE + "- Summon a mob");
    }

    /**
     * Handles the delete command.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void handleDelete(CommandSender sender, String[] args) {
        // Check arguments
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /guildadmin delete <guild>");
            return;
        }

        String guildName = args[0];
        Guild guild = plugin.getGuildService().getGuildByName(guildName);

        if (guild == null) {
            sender.sendMessage(ChatColor.RED + "Guild not found: " + guildName);
            return;
        }

        // Get all guild members for notification
        Set<UUID> guildMembers = guild.getMembers();

        // Delete the guild from the database
        try {
            if (plugin.getGuildService().deleteGuild(UUID.fromString(guild.getId()))) {
                // Notify all online guild members
                for (UUID memberId : guildMembers) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage(ChatColor.RED + "Your guild '" + guild.getName() + "' has been deleted by an administrator.");
                    }
                }
                
                sender.sendMessage(ChatColor.GREEN + "Guild '" + guild.getName() + "' has been deleted.");
                
                // Log the action
                plugin.getLogger().info("Admin " + sender.getName() + " deleted guild: " + guild.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to delete guild: " + guild.getName());
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error occurred while deleting the guild: " + e.getMessage());
            plugin.getLogger().severe("Error deleting guild: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the reload command.
     *
     * @param sender The command sender
     */
    private void handleReload(CommandSender sender) {
        try {
            // Reload plugin configuration
            plugin.reloadConfig();
            
            // Reload placeholders
            plugin.reloadPlaceholders();
            
            sender.sendMessage(ChatColor.GREEN + "GuildWars configuration reloaded successfully.");
            plugin.getLogger().info("Admin " + sender.getName() + " reloaded the plugin configuration.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error occurred while reloading the configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the about command.
     * Displays information about the plugin and server.
     *
     * @param sender The command sender
     */
    private void handleAbout(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== GuildWars Plugin Information ===");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + String.join(", ", plugin.getDescription().getAuthors()));
        sender.sendMessage(ChatColor.YELLOW + "Website: " + ChatColor.WHITE + plugin.getDescription().getWebsite());
        
        // Server information
        sender.sendMessage(ChatColor.GOLD + "=== Server Information ===");
        sender.sendMessage(ChatColor.YELLOW + "Bukkit Version: " + ChatColor.WHITE + Bukkit.getBukkitVersion());
        sender.sendMessage(ChatColor.YELLOW + "Server Version: " + ChatColor.WHITE + Bukkit.getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Online Players: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
        
        // Plugin statistics
        sender.sendMessage(ChatColor.GOLD + "=== Guild Statistics ===");
        int guildCount = plugin.getGuildService().getAllGuilds().size();
        sender.sendMessage(ChatColor.YELLOW + "Total Guilds: " + ChatColor.WHITE + guildCount);
        
        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        
        sender.sendMessage(ChatColor.GOLD + "=== Memory Usage ===");
        sender.sendMessage(ChatColor.YELLOW + "Used Memory: " + ChatColor.WHITE + usedMemory + " MB");
        sender.sendMessage(ChatColor.YELLOW + "Allocated Memory: " + ChatColor.WHITE + totalMemory + " MB");
        sender.sendMessage(ChatColor.YELLOW + "Maximum Memory: " + ChatColor.WHITE + maxMemory + " MB");
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
            sender.sendMessage(ChatColor.YELLOW + "Available enchantment types: HARVESTER, TUNNELING, SHOVEL_TUNNELING, AUTO_SMELT, HASTE, TREASURE_HUNTER");
            return;
        }
        
        // Get the enchantment type
        String typeArg = args[0].toUpperCase();
        CustomEnchantmentType type;
        
        try {
            type = CustomEnchantmentType.valueOf(typeArg);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid enchantment type: " + args[0]);
            sender.sendMessage(ChatColor.YELLOW + "Available enchantment types: HARVESTER, TUNNELING, SHOVEL_TUNNELING, AUTO_SMELT, HASTE, TREASURE_HUNTER");
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
        
        // Check if the player is holding an item
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            sender.sendMessage(ChatColor.RED + "You must be holding an item to enchant it.");
            return;
        }
        
        // Check if the enchantment can be applied to this item
        if (!type.canEnchantItem(item)) {
            // Get item material type name in a readable format
            String itemType = item.getType().toString().toLowerCase().replace('_', ' ');
            
            // Send a descriptive error message
            sender.sendMessage(ChatColor.RED + "Incompatible item! " + 
                              ChatColor.YELLOW + type.getFormattedName(1) + 
                              ChatColor.RED + " cannot be applied to " + 
                              ChatColor.YELLOW + itemType + ChatColor.RED + ".");
            return;
        }
        
        // Add the enchantment to the item
        ItemStack enchantedItem = enchantmentManager.addEnchantment(item, type, level);
        
        // Update the player's inventory
        player.getInventory().setItemInMainHand(enchantedItem);
        
        // Send success message
        sender.sendMessage(ChatColor.GREEN + "Added " + type.getFormattedName(level) + ChatColor.GREEN + " to your item.");
    }
    
    /**
     * Handles the give command for enchanted books.
     * Gives the player an enchanted book with the specified enchantment.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void handleGive(CommandSender sender, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }
        
        Player player = (Player) sender;
        
        // Check arguments
        if (args.length < 1) {
            // Build a list of available enchantment types for the help message
            StringBuilder availableTypes = new StringBuilder();
            for (CustomEnchantmentType type : CustomEnchantmentType.values()) {
                availableTypes.append("BOOK_OF_").append(type.name()).append(", ");
            }
            // Remove the trailing comma and space
            String availableTypesStr = availableTypes.length() > 2 ?
                availableTypes.substring(0, availableTypes.length() - 2) : "";
                
            sender.sendMessage(ChatColor.RED + "Usage: /guildadmin give <book_type> [level]");
            sender.sendMessage(ChatColor.YELLOW + "Available book types: " + availableTypesStr);
            return;
        }
        
        // Get the book type
        String bookArg = args[0].toUpperCase();
        CustomEnchantmentType enchantType = null;
        
        // Normalize the book argument if it doesn't start with BOOK_OF_
        if (!bookArg.startsWith("BOOK_OF_")) {
            bookArg = "BOOK_OF_" + bookArg;
        }
        
        // Map to convert book name to enchantment type
        try {
            // Remove the "BOOK_OF_" prefix and convert to enum
            String enchantName = bookArg.replace("BOOK_OF_", "");
            enchantType = CustomEnchantmentType.valueOf(enchantName);
        } catch (IllegalArgumentException e) {
            // Build a list of available enchantment types for the error message
            StringBuilder availableTypes = new StringBuilder();
            for (CustomEnchantmentType type : CustomEnchantmentType.values()) {
                availableTypes.append("BOOK_OF_").append(type.name()).append(", ");
            }
            // Remove the trailing comma and space
            String availableTypesStr = availableTypes.length() > 2 ?
                availableTypes.substring(0, availableTypes.length() - 2) : "";
                
            sender.sendMessage(ChatColor.RED + "Invalid book type: " + args[0]);
            sender.sendMessage(ChatColor.YELLOW + "Available book types: " + availableTypesStr);
            return;
        }
        
        // Get the enchantment level
        int level = 1;
        if (args.length > 1) {
            try {
                level = Integer.parseInt(args[1]);
                if (level < 1 || level > enchantType.getMaxLevel()) {
                    sender.sendMessage(ChatColor.RED + "Invalid level. Must be between 1 and " + enchantType.getMaxLevel() + ".");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid level: " + args[1] + ". Must be a number.");
                return;
            }
        }
        
        // Create the enchanted book with custom name
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        
        // Set a custom name for the enchanted book
        meta.setDisplayName(ChatColor.AQUA + "Book of " + enchantType.getDisplayName() + " " + enchantType.getRomanNumeral(level));
        
        // Store enchantment data in the persistent data container
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, enchantType.getKey());
        container.set(key, PersistentDataType.INTEGER, level);
        
        // Format enchantment info in lore to match vanilla-like appearance but with added details
        // This helps players understand what the enchantment does
        List<String> lore = new ArrayList<>();
        
        // Add the enchantment name with proper formatting (like vanilla enchantments)
        // Purple color with no italics, just like vanilla enchanted books
        lore.add(ChatColor.LIGHT_PURPLE + enchantType.getDisplayName() + " " + enchantType.getRomanNumeral(level));
        
        // Add description on a new line with gray color
        lore.add(ChatColor.GRAY + enchantType.getDescription());
        
        // Set the lore
        meta.setLore(lore);
        
        // Also store in the PersistentDataContainer for programmatic access
        NamespacedKey enchNameKey = new NamespacedKey(plugin, "book_enchant_name");
        container.set(enchNameKey, PersistentDataType.STRING, enchantType.getDisplayName() + " " + enchantType.getRomanNumeral(level));
        
        // Add stored enchantment to make it look like a real enchanted book
        // This is what vanilla does - it adds the enchantment to the book's stored enchantments
        meta.addStoredEnchant(Enchantment.DURABILITY, 1, true);
        
        // Hide the dummy enchantment we're using
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
        // Apply meta to book
        book.setItemMeta(meta);
        
        // Give the book to the player
        player.getInventory().addItem(book);
        
        // Send success message with additional info about applying the enchantment
        sender.sendMessage(ChatColor.GREEN + "You received a Book of " + enchantType.getDisplayName() + " " + enchantType.getRomanNumeral(level) + ".");
        
        // Show usage hint if the player is holding an item
        if (player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            Material handItem = player.getInventory().getItemInMainHand().getType();
            if (enchantType.canEnchantItem(player.getInventory().getItemInMainHand())) {
                sender.sendMessage(ChatColor.GRAY + "You can apply this book to your " + 
                    handItem.toString().toLowerCase().replace('_', ' ') + " using an anvil.");
            }
        }
    }
    
    /**
     * Handles the summon command.
     * Summons a mob at the player's location.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void handleSummon(CommandSender sender, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }
        
        Player player = (Player) sender;
        
        // Check arguments
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /guildadmin summon <mob_type>");
            sender.sendMessage(ChatColor.YELLOW + "Available mob types: Debug, FrostGiant, ShadowAssassin, CorruptedWarden");
            return;
        }
        
        // Get the mob type
        String mobArg = args[0].toUpperCase();
        
        // Parse mob type
        if (mobArg.equals("DEBUG")) {
            // Summon the Debug mob
            mobManager.summonDebugMob(player.getLocation());
            sender.sendMessage(ChatColor.GREEN + "Summoned a Debug mob.");
        } else if (mobArg.equals("FROSTGIANT")) {
            // Summon the Frost Giant mob
            mobManager.summonFrostGiant(player.getLocation());
            sender.sendMessage(ChatColor.AQUA + "Summoned a Frost Giant at your location.");
        } else if (mobArg.equals("SHADOWASSASSIN")) {
            // Summon the Shadow Assassin mob
            mobManager.summonShadowAssassin(player.getLocation());
            sender.sendMessage(ChatColor.DARK_PURPLE + "Summoned a Shadow Assassin at your location.");
        } else if (mobArg.equals("CORRUPTEDWARDEN")) {
            // Summon the Corrupted Warden mob
            mobManager.summonCorruptedWarden(player.getLocation());
            sender.sendMessage(ChatColor.DARK_RED + "Summoned a Corrupted Warden at your location.");
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid mob type: " + args[0]);
            sender.sendMessage(ChatColor.YELLOW + "Available mob types: Debug, FrostGiant, ShadowAssassin, CorruptedWarden");
        }
    }
    
    /**
     * Handles the godmode command.
     * Toggles godmode for the player, granting them full immunity and making them invisible to mobs.
     *
     * @param sender The command sender
     */
    private void handleGodMode(CommandSender sender) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }
        
        Player player = (Player) sender;
        
        // Check if player is already in godmode
        boolean isInGodMode = player.hasMetadata("godmode");
        
        if (isInGodMode) {
            // Remove godmode
            player.removeMetadata("godmode", plugin);
            
            // Remove effects
            player.setInvulnerable(false);
            player.setInvisible(false);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setFireTicks(0);
            player.setFallDistance(0);
            
            // Send message
            player.sendMessage(ChatColor.RED + "Godmode disabled. You are now mortal again.");
        } else {
            // Add godmode
            player.setMetadata("godmode", new FixedMetadataValue(plugin, true));
            
            // Add effects
            player.setInvulnerable(true);
            player.setInvisible(true);
            player.setAllowFlight(true);
            player.setFlying(true);
            
            // Send message
            player.sendMessage(ChatColor.GREEN + "Godmode enabled. You are now invulnerable and invisible to mobs.");
        }
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
            String[] subCommands = {"delete", "reload", "about", "enchant", "give", "summon", "godmode"};
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete")) {
                // Second argument for delete - guild name
                String input = args[1].toLowerCase();
                
                // Get all guild names from the guild service
                List<String> guildNames = plugin.getGuildService().getAllGuilds().stream()
                        .map(Guild::getName)
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());
                
                completions.addAll(guildNames);
            } else if (args[0].equalsIgnoreCase("enchant")) {
                // Second argument for enchant - enchantment type
                String input = args[1].toLowerCase();
                
                for (CustomEnchantmentType type : CustomEnchantmentType.values()) {
                    if (type.name().toLowerCase().startsWith(input)) {
                        completions.add(type.name().toLowerCase());
                    }
                }
            } else if (args[0].equalsIgnoreCase("give")) {
                // Second argument for give - book type
                String input = args[1].toLowerCase();
                
                String[] bookTypes = {
                    "book_of_harvester",
                    "book_of_tunneling",
                    "book_of_shovel_tunneling",
                    "book_of_execute",
                    "book_of_vampiric_edge",
                    "book_of_crippling_strike",
                    "book_of_frenzy",
                    "book_of_mark_of_death"
                };
                
                for (String bookType : bookTypes) {
                    if (bookType.startsWith(input)) {
                        completions.add(bookType);
                    }
                }
            } else if (args[0].equalsIgnoreCase("summon")) {
                // Second argument for summon - mob types
                String input = args[1].toLowerCase();
                
                String[] mobTypes = {"Debug", "FrostGiant", "ShadowAssassin", "CorruptedWarden"};
                
                for (String type : mobTypes) {
                    if (type.toLowerCase().startsWith(input)) {
                        completions.add(type);
                    }
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("enchant")) {
                // Third argument for enchant - level
                String input = args[2].toLowerCase();
                
                // Get the enchantment type
                try {
                    CustomEnchantmentType type = CustomEnchantmentType.valueOf(args[1].toUpperCase());
                    
                    // Add level suggestions based on the max level
                    for (int i = 1; i <= type.getMaxLevel(); i++) {
                        if (String.valueOf(i).startsWith(input)) {
                            completions.add(String.valueOf(i));
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid enchantment type, no suggestions
                }
            } else if (args[0].equalsIgnoreCase("give")) {
                // Third argument for give - level
                String input = args[2].toLowerCase();
                
                // Get the book type
                String bookType = args[1].toUpperCase();
                CustomEnchantmentType type = null;
                
                // Parse book type to enchantment type
                if (bookType.equals("BOOK_OF_HARVESTER")) {
                    type = CustomEnchantmentType.HARVESTER;
                } else if (bookType.equals("BOOK_OF_TUNNELING")) {
                    type = CustomEnchantmentType.TUNNELING;
                } else if (bookType.equals("BOOK_OF_SHOVEL_TUNNELING")) {
                    type = CustomEnchantmentType.SHOVEL_TUNNELING;
                // Admin enchantments have been removed
                }
                
                if (type != null) {
                    // Add level suggestions based on the max level
                    for (int i = 1; i <= type.getMaxLevel(); i++) {
                        if (String.valueOf(i).startsWith(input)) {
                            completions.add(String.valueOf(i));
                        }
                    }
                }
            }
        }
        
        return completions;
    }
}
