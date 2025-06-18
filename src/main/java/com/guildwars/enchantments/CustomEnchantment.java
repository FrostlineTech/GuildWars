package com.guildwars.enchantments;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import io.papermc.paper.enchantments.EnchantmentRarity;

import java.util.Collections;
import java.util.Set;

/**
 * Base class for custom enchantments.
 */
public abstract class CustomEnchantment extends Enchantment {
    
    private final String name;
    private final int maxLevel;
    private final EnchantmentTarget target;
    private final NamespacedKey key;
    
    /**
     * Creates a new custom enchantment.
     *
     * @param key The unique key for this enchantment
     * @param name The display name of the enchantment
     * @param maxLevel The maximum level of the enchantment
     * @param target The target items for this enchantment
     */
    public CustomEnchantment(NamespacedKey key, String name, int maxLevel, EnchantmentTarget target) {
        // Note: In newer versions of Spigot/Paper, Enchantment is now abstract with no constructor
        // The key is handled by implementing getKey() method
        this.name = name;
        this.maxLevel = maxLevel;
        this.target = target;
        this.key = key;
    }
    
    /**
     * Gets the formatted display name of this enchantment.
     *
     * @param level The level of the enchantment
     * @return The formatted display name
     */
    public String getFormattedName(int level) {
        return ChatColor.GRAY + name + " " + getRomanNumeral(level);
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
     * Adds this enchantment to an item.
     *
     * @param item The item to enchant
     * @param level The level of the enchantment
     * @return The enchanted item
     */
    public ItemStack addToItem(ItemStack item, int level) {
        if (item == null) {
            return null;
        }
        
        if (!canEnchantItem(item)) {
            return item;
        }
        
        // Add the enchantment to the item
        item.addUnsafeEnchantment(this, level);
        
        // Update the lore to show the enchantment
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            java.util.List<net.kyori.adventure.text.Component> lore = meta.lore();
            if (lore == null) {
                lore = new java.util.ArrayList<>();
            }
            
            // Add the enchantment name to the lore
            lore.add(net.kyori.adventure.text.Component.text(getFormattedName(level)));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    // getName() is deprecated in newer Bukkit versions
    // Use getKey() and translationKey() instead
    @Override
    @Deprecated
    public String getName() {
        return name;
    }
    
    @Override
    public int getMaxLevel() {
        return maxLevel;
    }
    
    @Override
    public int getStartLevel() {
        return 1;
    }
    
    @Override
    public EnchantmentTarget getItemTarget() {
        return target;
    }
    
    @Override
    public boolean isTreasure() {
        return true; // These are special enchantments
    }
    
    @Override
    public boolean isCursed() {
        return false;
    }
    
    @Override
    public boolean conflictsWith(Enchantment other) {
        return false; // By default, custom enchantments don't conflict
    }
    
    @Override
    public boolean isTradeable() {
        return true;
    }
    
    @Override
    public boolean isDiscoverable() {
        return true;
    }
    
    @Override
    public net.kyori.adventure.text.Component displayName(int level) {
        return net.kyori.adventure.text.Component.text(getFormattedName(level));
    }
    
    @Override
    public String translationKey() {
        return "enchantment.guildwars." + getName().toLowerCase();
    }
    
    @Override
    public float getDamageIncrease(int level, EntityCategory entityCategory) {
        return 0; // No damage increase for these enchantments
    }
    
    @Override
    public Set<EquipmentSlot> getActiveSlots() {
        return Collections.singleton(EquipmentSlot.HAND);
    }
    
    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }
    
    @Override
    public NamespacedKey getKey() {
        return key;
    }
    
    @Override
    public int getMinModifiedCost(int level) {
        return level * 10;
    }
    
    @Override
    public int getMaxModifiedCost(int level) {
        return getMinModifiedCost(level) + 15;
    }
}
