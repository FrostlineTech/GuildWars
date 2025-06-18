package com.guildwars.enchantments;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import io.papermc.paper.enchantments.EnchantmentRarity;

/**
 * Tunneling enchantment for pickaxes.
 * Mines blocks in a 3x3 area when a block is mined.
 */
public class TunnelingEnchantment extends CustomEnchantment {

    public TunnelingEnchantment(NamespacedKey key) {
        super(key, "Tunneling", 1, EnchantmentTarget.TOOL);
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item != null && (
                item.getType() == Material.WOODEN_PICKAXE ||
                item.getType() == Material.STONE_PICKAXE ||
                item.getType() == Material.IRON_PICKAXE ||
                item.getType() == Material.GOLDEN_PICKAXE ||
                item.getType() == Material.DIAMOND_PICKAXE ||
                item.getType() == Material.NETHERITE_PICKAXE
        );
    }
    
    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }
    
    @Override
    public String getTranslationKey() {
        return "enchantment.guildwars.tunneling";
    }
    
    // These methods are already implemented in the CustomEnchantment parent class,
    // but we provide them here for clarity
    @Override
    public int getMaxModifiedCost(int level) {
        return getMinModifiedCost(level) + 20;
    }
    
    @Override
    public int getMinModifiedCost(int level) {
        return level * 15;
    }
}
