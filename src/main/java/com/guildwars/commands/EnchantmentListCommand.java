package com.guildwars.commands;

import com.guildwars.GuildWars;
import com.guildwars.enchantments.CustomEnchantmentType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Command for listing available custom enchantments and their descriptions.
 */
public class EnchantmentListCommand implements CommandExecutor, TabCompleter {

    private final GuildWars plugin;
    private final Map<String, List<CustomEnchantmentType>> categoryMap = new HashMap<>();
    
    public EnchantmentListCommand(GuildWars plugin) {
        this.plugin = plugin;
        initializeCategoryMap();
        
        // Register the command
        plugin.getCommand("enchantments").setExecutor(this);
        plugin.getCommand("enchantments").setTabCompleter(this);
        plugin.getLogger().info("Enchantment List command registered.");
    }
    
    /**
     * Initializes the category map for organizing enchantments by tool type.
     */
    private void initializeCategoryMap() {
        // Pickaxe enchantments
        categoryMap.put("pickaxe", Arrays.asList(
            CustomEnchantmentType.TUNNELING,
            CustomEnchantmentType.AUTO_SMELT,
            CustomEnchantmentType.TREASURE_HUNTER,
            CustomEnchantmentType.HASTE
        ));
        
        // Shovel enchantments
        categoryMap.put("shovel", Arrays.asList(
            CustomEnchantmentType.SHOVEL_TUNNELING
        ));
        
        // Hoe enchantments
        categoryMap.put("hoe", Arrays.asList(
            CustomEnchantmentType.HARVESTER
        ));
        
        // Sword enchantments
        categoryMap.put("sword", Arrays.asList(
            CustomEnchantmentType.EXECUTE,
            CustomEnchantmentType.VAMPIRIC_EDGE,
            CustomEnchantmentType.CRIPPLING_STRIKE,
            CustomEnchantmentType.FRENZY,
            CustomEnchantmentType.MARK_OF_DEATH
        ));
        
        // Axe enchantments
        categoryMap.put("axe", Arrays.asList(
            CustomEnchantmentType.ARMOR_CRACK,
            CustomEnchantmentType.SHOCKWAVE,
            CustomEnchantmentType.RUPTURE
        ));
        
        // Bow enchantments
        categoryMap.put("bow", Arrays.asList(
            CustomEnchantmentType.EXPLOSIVE_ARROW,
            CustomEnchantmentType.MULTI_SHOT,
            CustomEnchantmentType.TELEPORT_ARROW,
            CustomEnchantmentType.GRAVITY_WELL,
            CustomEnchantmentType.LIGHTNING_STRIKE
        ));
        
        // Armor enchantments
        categoryMap.put("armor", Arrays.asList(
            CustomEnchantmentType.MOLTEN,
            CustomEnchantmentType.GUARDIANS,
            CustomEnchantmentType.SECOND_WIND,
            CustomEnchantmentType.STABILITY,
            CustomEnchantmentType.THORN_BURST
        ));
        
        // Boot enchantments
        categoryMap.put("boots", Arrays.asList(
            CustomEnchantmentType.CLIMB,
            CustomEnchantmentType.SHOCK_ABSORB,
            CustomEnchantmentType.SPEED_BOOST,
            CustomEnchantmentType.SHADOWSTEP
        ));
        
        // All enchantments
        List<CustomEnchantmentType> allEnchantments = new ArrayList<>();
        for (List<CustomEnchantmentType> enchantments : categoryMap.values()) {
            allEnchantments.addAll(enchantments);
        }
        
        // Deduplicate by creating a set and then back to list
        categoryMap.put("all", allEnchantments.stream().distinct().collect(Collectors.toList()));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Log command usage
        plugin.getLogger().info(sender.getName() + " used the enchantments command.");
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Show available categories
            showCategories(player);
            return true;
        }
        
        String category = args[0].toLowerCase();
        
        if (!categoryMap.containsKey(category)) {
            player.sendMessage(ChatColor.RED + "Unknown category: " + category);
            showCategories(player);
            return true;
        }
        
        showEnchantmentsForCategory(player, category);
        return true;
    }
    
    /**
     * Shows available enchantment categories to the player.
     */
    private void showCategories(Player player) {
        player.sendMessage(ChatColor.GOLD + "==== " + ChatColor.YELLOW + "Custom Enchantment Categories" + 
                ChatColor.GOLD + " ====");
        player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/enchantments <category>" + 
                ChatColor.YELLOW + " to view enchantments in a category.");
        player.sendMessage(ChatColor.YELLOW + "Available categories:");
        
        for (String category : categoryMap.keySet()) {
            if (!category.equals("all")) {
                player.sendMessage(ChatColor.GOLD + " • " + ChatColor.WHITE + category + 
                        ChatColor.GRAY + " (" + categoryMap.get(category).size() + " enchantments)");
            }
        }
        
        player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/enchantments all" + 
                ChatColor.YELLOW + " to view all enchantments.");
    }
    
    /**
     * Shows enchantments for a specific category.
     */
    private void showEnchantmentsForCategory(Player player, String category) {
        List<CustomEnchantmentType> enchantments = categoryMap.get(category);
        
        if (enchantments.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No enchantments available for this category.");
            return;
        }
        
        String categoryName = category.substring(0, 1).toUpperCase() + category.substring(1);
        player.sendMessage(ChatColor.GOLD + "==== " + ChatColor.YELLOW + categoryName + " Enchantments" + 
                ChatColor.GOLD + " ====");
        
        for (CustomEnchantmentType enchantment : enchantments) {
            String maxLevel = enchantment.getMaxLevel() > 1 ? 
                    " (Max Level: " + enchantment.getMaxLevel() + ")" : "";
            
            player.sendMessage(ChatColor.GOLD + enchantment.getDisplayName() + 
                    ChatColor.GRAY + maxLevel);
            player.sendMessage(ChatColor.YELLOW + " ↪ " + ChatColor.WHITE + 
                    enchantment.getDescription());
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partialCategory = args[0].toLowerCase();
            return categoryMap.keySet().stream()
                    .filter(cat -> cat.startsWith(partialCategory))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}
