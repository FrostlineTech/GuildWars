package com.guildwars.enchantments;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a type of custom enchantment.
 */
public enum CustomEnchantmentType {
    
    /**
     * Harvester enchantment for hoes.
     * Harvests crops in an area around the target crop.
     * Level 1-3 determines the radius of effect.
     */
    HARVESTER("Harvester", 3, "Harvests crops in a radius around the target crop"),
    
    /**
     * Tunneling enchantment for pickaxes.
     * Mines blocks in a 3x3 area when a block is mined.
     */
    TUNNELING("Tunneling", 1, "Mines blocks in a 3x3 area");
    
    private final String displayName;
    private final int maxLevel;
    private final String description;
    
    CustomEnchantmentType(String displayName, int maxLevel, String description) {
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.description = description;
    }
    
    /**
     * Gets the display name of this enchantment.
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the maximum level of this enchantment.
     *
     * @return The maximum level
     */
    public int getMaxLevel() {
        return maxLevel;
    }
    
    /**
     * Gets the description of this enchantment.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the formatted display name of this enchantment.
     *
     * @param level The level of the enchantment
     * @return The formatted display name
     */
    public String getFormattedName(int level) {
        return ChatColor.GRAY + displayName + " " + getRomanNumeral(level);
    }
    
    /**
     * Converts an integer to a Roman numeral.
     *
     * @param number The number to convert
     * @return The Roman numeral representation
     */
    private String getRomanNumeral(int number) {
        switch (number) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            default:
                return String.valueOf(number);
        }
    }
    
    /**
     * Checks if this enchantment can be applied to the given item.
     *
     * @param item The item to check
     * @return True if the enchantment can be applied
     */
    public boolean canEnchantItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        
        Material type = item.getType();
        
        switch (this) {
            case HARVESTER:
                return type == Material.WOODEN_HOE || 
                       type == Material.STONE_HOE || 
                       type == Material.IRON_HOE || 
                       type == Material.GOLDEN_HOE || 
                       type == Material.DIAMOND_HOE || 
                       type == Material.NETHERITE_HOE;
                
            case TUNNELING:
                return type == Material.WOODEN_PICKAXE || 
                       type == Material.STONE_PICKAXE || 
                       type == Material.IRON_PICKAXE || 
                       type == Material.GOLDEN_PICKAXE || 
                       type == Material.DIAMOND_PICKAXE || 
                       type == Material.NETHERITE_PICKAXE;
                
            default:
                return false;
        }
    }
    
    /**
     * Gets the key for this enchantment.
     *
     * @return The key
     */
    public String getKey() {
        return name().toLowerCase();
    }
}
