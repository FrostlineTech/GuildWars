package com.guildwars.enchantments;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import io.papermc.paper.enchantments.EnchantmentRarity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Harvester enchantment for hoes.
 * Harvests crops in an area around the target crop.
 */
public class HarvesterEnchantment extends CustomEnchantment {

    private static final Set<Material> CROP_TYPES = new HashSet<>(Arrays.asList(
            Material.WHEAT,
            Material.POTATOES,
            Material.CARROTS,
            Material.BEETROOTS,
            Material.NETHER_WART
    ));

    public HarvesterEnchantment(NamespacedKey key) {
        super(key, "Harvester", 3, EnchantmentTarget.TOOL);
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item != null && (
                item.getType() == Material.WOODEN_HOE ||
                item.getType() == Material.STONE_HOE ||
                item.getType() == Material.IRON_HOE ||
                item.getType() == Material.GOLDEN_HOE ||
                item.getType() == Material.DIAMOND_HOE ||
                item.getType() == Material.NETHERITE_HOE
        );
    }

    /**
     * Checks if a block is a fully grown crop.
     *
     * @param block The block to check
     * @return True if the block is a fully grown crop
     */
    public static boolean isFullyGrownCrop(Block block) {
        if (!CROP_TYPES.contains(block.getType())) {
            return false;
        }

        if (block.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) block.getBlockData();
            return ageable.getAge() == ageable.getMaximumAge();
        }

        return false;
    }

    /**
     * Gets the radius of effect based on the enchantment level.
     *
     * @param level The enchantment level
     * @return The radius of effect
     */
    public static int getRadius(int level) {
        return level;
    }
    
    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }
}
