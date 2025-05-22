package com.guildwars.enchantments;

import org.bukkit.Material;
import java.util.HashSet;
import java.util.Set;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class containing material category definitions for the custom enchantment system.
 * This class improves performance by pre-defining sets of materials for different tool types.
 */
public class MaterialCategories {
    
    // Pickaxe materials
    public static final Set<Material> PICKAXES = new HashSet<>();
    
    // Sword materials
    public static final Set<Material> SWORDS = new HashSet<>();
    
    // Axe materials
    public static final Set<Material> AXES = new HashSet<>();
    
    // Armor materials
    public static final Set<Material> ARMOR = new HashSet<>();
    
    // Boot materials
    public static final Set<Material> BOOTS = new HashSet<>();
    
    // Crop materials
    public static final Set<Material> CROP_TYPES = new HashSet<>();
    
    // Mineable materials
    public static final Set<Material> MINEABLE_TYPES = new HashSet<>();
    
    // Shovel-diggable materials
    public static final Set<Material> DIGGABLE_TYPES = new HashSet<>();
    
    // Smeltable ores mapping
    public static final Map<Material, Material> SMELTABLE_ORES = new EnumMap<>(Material.class);
    
    // Valuable blocks (for treasure hunter)
    public static final Set<Material> VALUABLE_BLOCKS = new HashSet<>();
    
    static {
        // Initialize pickaxes
        PICKAXES.add(Material.WOODEN_PICKAXE);
        PICKAXES.add(Material.STONE_PICKAXE);
        PICKAXES.add(Material.IRON_PICKAXE);
        PICKAXES.add(Material.GOLDEN_PICKAXE);
        PICKAXES.add(Material.DIAMOND_PICKAXE);
        PICKAXES.add(Material.NETHERITE_PICKAXE);
        
        // Initialize swords
        SWORDS.add(Material.WOODEN_SWORD);
        SWORDS.add(Material.STONE_SWORD);
        SWORDS.add(Material.IRON_SWORD);
        SWORDS.add(Material.GOLDEN_SWORD);
        SWORDS.add(Material.DIAMOND_SWORD);
        SWORDS.add(Material.NETHERITE_SWORD);
        
        // Initialize axes
        AXES.add(Material.WOODEN_AXE);
        AXES.add(Material.STONE_AXE);
        AXES.add(Material.IRON_AXE);
        AXES.add(Material.GOLDEN_AXE);
        AXES.add(Material.DIAMOND_AXE);
        AXES.add(Material.NETHERITE_AXE);
        
        // Initialize boots
        BOOTS.add(Material.LEATHER_BOOTS);
        BOOTS.add(Material.CHAINMAIL_BOOTS);
        BOOTS.add(Material.IRON_BOOTS);
        BOOTS.add(Material.GOLDEN_BOOTS);
        BOOTS.add(Material.DIAMOND_BOOTS);
        BOOTS.add(Material.NETHERITE_BOOTS);
        
        // Initialize armor (all pieces)
        // Helmets
        ARMOR.add(Material.LEATHER_HELMET);
        ARMOR.add(Material.CHAINMAIL_HELMET);
        ARMOR.add(Material.IRON_HELMET);
        ARMOR.add(Material.GOLDEN_HELMET);
        ARMOR.add(Material.DIAMOND_HELMET);
        ARMOR.add(Material.NETHERITE_HELMET);
        // Chestplates
        ARMOR.add(Material.LEATHER_CHESTPLATE);
        ARMOR.add(Material.CHAINMAIL_CHESTPLATE);
        ARMOR.add(Material.IRON_CHESTPLATE);
        ARMOR.add(Material.GOLDEN_CHESTPLATE);
        ARMOR.add(Material.DIAMOND_CHESTPLATE);
        ARMOR.add(Material.NETHERITE_CHESTPLATE);
        // Leggings
        ARMOR.add(Material.LEATHER_LEGGINGS);
        ARMOR.add(Material.CHAINMAIL_LEGGINGS);
        ARMOR.add(Material.IRON_LEGGINGS);
        ARMOR.add(Material.GOLDEN_LEGGINGS);
        ARMOR.add(Material.DIAMOND_LEGGINGS);
        ARMOR.add(Material.NETHERITE_LEGGINGS);
        // Add boots to armor set as well
        ARMOR.addAll(BOOTS);
        
        // Initialize crop types
        CROP_TYPES.add(Material.WHEAT);
        CROP_TYPES.add(Material.POTATOES);
        CROP_TYPES.add(Material.CARROTS);
        CROP_TYPES.add(Material.BEETROOTS);
        CROP_TYPES.add(Material.NETHER_WART);
        CROP_TYPES.add(Material.SWEET_BERRY_BUSH);
        CROP_TYPES.add(Material.COCOA);
        
        // Initialize mineable types
        // Stone types
        MINEABLE_TYPES.add(Material.STONE);
        MINEABLE_TYPES.add(Material.COBBLESTONE);
        MINEABLE_TYPES.add(Material.GRANITE);
        MINEABLE_TYPES.add(Material.DIORITE);
        MINEABLE_TYPES.add(Material.ANDESITE);
        
        // Ores
        MINEABLE_TYPES.add(Material.COAL_ORE);
        MINEABLE_TYPES.add(Material.IRON_ORE);
        MINEABLE_TYPES.add(Material.GOLD_ORE);
        MINEABLE_TYPES.add(Material.DIAMOND_ORE);
        MINEABLE_TYPES.add(Material.EMERALD_ORE);
        MINEABLE_TYPES.add(Material.LAPIS_ORE);
        MINEABLE_TYPES.add(Material.REDSTONE_ORE);
        MINEABLE_TYPES.add(Material.COPPER_ORE);
        MINEABLE_TYPES.add(Material.NETHER_QUARTZ_ORE);
        MINEABLE_TYPES.add(Material.NETHER_GOLD_ORE);
        MINEABLE_TYPES.add(Material.ANCIENT_DEBRIS);
        
        // Deepslate variants
        MINEABLE_TYPES.add(Material.DEEPSLATE);
        MINEABLE_TYPES.add(Material.DEEPSLATE_COAL_ORE);
        MINEABLE_TYPES.add(Material.DEEPSLATE_IRON_ORE);
        MINEABLE_TYPES.add(Material.DEEPSLATE_GOLD_ORE);
        MINEABLE_TYPES.add(Material.DEEPSLATE_DIAMOND_ORE);
        MINEABLE_TYPES.add(Material.DEEPSLATE_EMERALD_ORE);
        MINEABLE_TYPES.add(Material.DEEPSLATE_LAPIS_ORE);
        MINEABLE_TYPES.add(Material.DEEPSLATE_REDSTONE_ORE);
        MINEABLE_TYPES.add(Material.DEEPSLATE_COPPER_ORE);
        
        // Other mineable blocks
        MINEABLE_TYPES.add(Material.NETHERRACK);
        MINEABLE_TYPES.add(Material.END_STONE);
        MINEABLE_TYPES.add(Material.OBSIDIAN);
        MINEABLE_TYPES.add(Material.CRYING_OBSIDIAN);
        MINEABLE_TYPES.add(Material.BLACKSTONE);
        MINEABLE_TYPES.add(Material.BASALT);
        
        // Initialize diggable types
        DIGGABLE_TYPES.add(Material.DIRT);
        DIGGABLE_TYPES.add(Material.GRASS_BLOCK);
        DIGGABLE_TYPES.add(Material.SAND);
        DIGGABLE_TYPES.add(Material.RED_SAND);
        DIGGABLE_TYPES.add(Material.GRAVEL);
        DIGGABLE_TYPES.add(Material.CLAY);
        DIGGABLE_TYPES.add(Material.SOUL_SAND);
        DIGGABLE_TYPES.add(Material.SOUL_SOIL);
        DIGGABLE_TYPES.add(Material.MYCELIUM);
        DIGGABLE_TYPES.add(Material.PODZOL);
        DIGGABLE_TYPES.add(Material.COARSE_DIRT);
        DIGGABLE_TYPES.add(Material.ROOTED_DIRT);
        // MUD material not available in this version
        DIGGABLE_TYPES.add(Material.SNOW);
        DIGGABLE_TYPES.add(Material.SNOW_BLOCK);
        
        // Initialize smeltable ores
        SMELTABLE_ORES.put(Material.IRON_ORE, Material.IRON_INGOT);
        SMELTABLE_ORES.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        SMELTABLE_ORES.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        SMELTABLE_ORES.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
        SMELTABLE_ORES.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        SMELTABLE_ORES.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        SMELTABLE_ORES.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        SMELTABLE_ORES.put(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT);
        SMELTABLE_ORES.put(Material.SAND, Material.GLASS);
        SMELTABLE_ORES.put(Material.RED_SAND, Material.GLASS);
        SMELTABLE_ORES.put(Material.COBBLESTONE, Material.STONE);
        SMELTABLE_ORES.put(Material.STONE, Material.SMOOTH_STONE);
        
        // Initialize valuable blocks (same as ores for treasure hunter)
        VALUABLE_BLOCKS.add(Material.DIAMOND_ORE);
        VALUABLE_BLOCKS.add(Material.DEEPSLATE_DIAMOND_ORE);
        VALUABLE_BLOCKS.add(Material.EMERALD_ORE);
        VALUABLE_BLOCKS.add(Material.DEEPSLATE_EMERALD_ORE);
        VALUABLE_BLOCKS.add(Material.GOLD_ORE);
        VALUABLE_BLOCKS.add(Material.DEEPSLATE_GOLD_ORE);
        VALUABLE_BLOCKS.add(Material.IRON_ORE);
        VALUABLE_BLOCKS.add(Material.DEEPSLATE_IRON_ORE);
        VALUABLE_BLOCKS.add(Material.LAPIS_ORE);
        VALUABLE_BLOCKS.add(Material.DEEPSLATE_LAPIS_ORE);
        VALUABLE_BLOCKS.add(Material.REDSTONE_ORE);
        VALUABLE_BLOCKS.add(Material.DEEPSLATE_REDSTONE_ORE);
        VALUABLE_BLOCKS.add(Material.COPPER_ORE);
        VALUABLE_BLOCKS.add(Material.DEEPSLATE_COPPER_ORE);
        VALUABLE_BLOCKS.add(Material.COAL_ORE);
        VALUABLE_BLOCKS.add(Material.DEEPSLATE_COAL_ORE);
        VALUABLE_BLOCKS.add(Material.ANCIENT_DEBRIS);
        VALUABLE_BLOCKS.add(Material.NETHER_GOLD_ORE);
        VALUABLE_BLOCKS.add(Material.NETHER_QUARTZ_ORE);
    }
    
    /**
     * Checks if the given material is a pickaxe.
     * 
     * @param material The material to check
     * @return True if the material is a pickaxe
     */
    public static boolean isPickaxe(Material material) {
        return PICKAXES.contains(material);
    }
    
    /**
     * Checks if the given material is a sword.
     * 
     * @param material The material to check
     * @return True if the material is a sword
     */
    public static boolean isSword(Material material) {
        return SWORDS.contains(material);
    }
    
    /**
     * Checks if the given material is an axe.
     * 
     * @param material The material to check
     * @return True if the material is an axe
     */
    public static boolean isAxe(Material material) {
        return AXES.contains(material);
    }
    
    /**
     * Checks if the given material is a boot armor piece.
     * 
     * @param material The material to check
     * @return True if the material is boots
     */
    public static boolean isBoots(Material material) {
        return BOOTS.contains(material);
    }
    
    /**
     * Checks if the given material is any armor piece.
     * 
     * @param material The material to check
     * @return True if the material is armor
     */
    public static boolean isArmor(Material material) {
        return ARMOR.contains(material);
    }
    
    /**
     * Checks if the given material is a crop.
     * 
     * @param material The material to check
     * @return True if the material is a crop
     */
    public static boolean isCrop(Material material) {
        return CROP_TYPES.contains(material);
    }
    
    /**
     * Checks if the given material can be mined with a pickaxe.
     * 
     * @param material The material to check
     * @return True if the material can be mined with a pickaxe
     */
    public static boolean isPickaxeMineable(Material material) {
        return MINEABLE_TYPES.contains(material);
    }
    
    /**
     * Checks if the given material can be dug with a shovel.
     * 
     * @param material The material to check
     * @return True if the material can be dug with a shovel
     */
    public static boolean isShovelDiggable(Material material) {
        return DIGGABLE_TYPES.contains(material);
    }
    
    /**
     * Checks if the given material is a valuable block (for Treasure Hunter).
     * 
     * @param material The material to check
     * @return True if the material is valuable
     */
    public static boolean isValuableBlock(Material material) {
        return VALUABLE_BLOCKS.contains(material);
    }
    
    /**
     * Checks if the given material can be smelted.
     * 
     * @param material The material to check
     * @return True if the material can be smelted
     */
    public static boolean isSmeltable(Material material) {
        return SMELTABLE_ORES.containsKey(material);
    }
    
    /**
     * Gets the smelted result for a material.
     * 
     * @param material The material to smelt
     * @return The smelted material or null if not smeltable
     */
    public static Material getSmeltedResult(Material material) {
        return SMELTABLE_ORES.get(material);
    }
}
