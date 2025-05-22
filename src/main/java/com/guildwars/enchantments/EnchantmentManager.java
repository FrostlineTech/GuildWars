package com.guildwars.enchantments;

import com.guildwars.GuildWars;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Manager for custom enchantments.
 */
public class EnchantmentManager implements Listener {

    private final GuildWars plugin;
    private final Random random = new Random();
    
    // Custom enchantments
    private HarvesterEnchantment harvesterEnchantment;
    private TunnelingEnchantment tunnelingEnchantment;
    
    // Enchantment keys
    private NamespacedKey harvesterKey;
    private NamespacedKey tunnelingKey;
    
    /**
     * Creates a new enchantment manager.
     *
     * @param plugin The plugin instance
     */
    public EnchantmentManager(GuildWars plugin) {
        this.plugin = plugin;
        
        // Register enchantments
        registerEnchantments();
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Registers all custom enchantments.
     */
    private void registerEnchantments() {
        try {
            // Make enchantments acceptingNew field accessible
            Field acceptingNew = Enchantment.class.getDeclaredField("acceptingNew");
            acceptingNew.setAccessible(true);
            acceptingNew.set(null, true);
            
            // Create enchantment keys
            harvesterKey = new NamespacedKey(plugin, "harvester");
            tunnelingKey = new NamespacedKey(plugin, "tunneling");
            
            // Create enchantments
            harvesterEnchantment = new HarvesterEnchantment(harvesterKey);
            tunnelingEnchantment = new TunnelingEnchantment(tunnelingKey);
            
            // Register enchantments
            Enchantment.registerEnchantment(harvesterEnchantment);
            Enchantment.registerEnchantment(tunnelingEnchantment);
            
            plugin.getLogger().info("Custom enchantments registered successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register custom enchantments: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Unregisters all custom enchantments.
     */
    public void unregisterEnchantments() {
        try {
            // Get the byKey field
            Field byKeyField = Enchantment.class.getDeclaredField("byKey");
            byKeyField.setAccessible(true);
            
            // Get the byName field
            Field byNameField = Enchantment.class.getDeclaredField("byName");
            byNameField.setAccessible(true);
            
            // Get the maps
            @SuppressWarnings("unchecked")
            HashMap<NamespacedKey, Enchantment> byKey = (HashMap<NamespacedKey, Enchantment>) byKeyField.get(null);
            
            @SuppressWarnings("unchecked")
            HashMap<String, Enchantment> byName = (HashMap<String, Enchantment>) byNameField.get(null);
            
            // Remove our enchantments
            if (harvesterEnchantment != null) {
                byKey.remove(harvesterKey);
                byName.remove(harvesterEnchantment.getName());
            }
            
            if (tunnelingEnchantment != null) {
                byKey.remove(tunnelingKey);
                byName.remove(tunnelingEnchantment.getName());
            }
            
            plugin.getLogger().info("Custom enchantments unregistered successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to unregister custom enchantments: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the Harvester enchantment.
     *
     * @return The Harvester enchantment
     */
    public HarvesterEnchantment getHarvesterEnchantment() {
        return harvesterEnchantment;
    }
    
    /**
     * Gets the Tunneling enchantment.
     *
     * @return The Tunneling enchantment
     */
    public TunnelingEnchantment getTunnelingEnchantment() {
        return tunnelingEnchantment;
    }
    
    /**
     * Handles the BlockBreakEvent for custom enchantments.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        // Check for Harvester enchantment
        if (tool != null && tool.containsEnchantment(harvesterEnchantment)) {
            int level = tool.getEnchantmentLevel(harvesterEnchantment);
            
            // Check if the block is a fully grown crop
            if (HarvesterEnchantment.isFullyGrownCrop(block)) {
                // Get radius based on enchantment level
                int radius = HarvesterEnchantment.getRadius(level);
                
                // Harvest crops in radius
                harvestCropsInRadius(block, player, tool, radius);
            }
        }
        
        // Check for Tunneling enchantment
        if (tool != null && tool.containsEnchantment(tunnelingEnchantment)) {
            // Check if the block can be mined with a pickaxe
            if (isMineable(block.getType())) {
                // Mine blocks in 3x3 area
                mineBlocksIn3x3(block, player, tool);
            }
        }
    }
    
    /**
     * Handles the EnchantItemEvent to add custom enchantments.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        // 1 in 1000 chance to add a custom enchantment
        if (random.nextInt(1000) == 0) {
            ItemStack item = event.getItem();
            
            // Add Harvester to hoes
            if (item.getType().name().endsWith("_HOE")) {
                int level = random.nextInt(3) + 1; // Random level 1-3
                Bukkit.getScheduler().runTask(plugin, () -> {
                    harvesterEnchantment.addToItem(item, level);
                    event.getEnchanter().sendMessage("§6Your tool was blessed with §bHarvester " + level + "§6!");
                });
            }
            
            // Add Tunneling to pickaxes
            else if (item.getType().name().endsWith("_PICKAXE")) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    tunnelingEnchantment.addToItem(item, 1);
                    event.getEnchanter().sendMessage("§6Your tool was blessed with §bTunneling§6!");
                });
            }
        }
    }
    
    /**
     * Handles the LootGenerateEvent to add custom enchantments to loot.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        // Check if this is a structure loot (desert temple, stronghold, etc.)
        if (event.getLootTable().getKey().getKey().contains("chest")) {
            // 10% chance to add a custom enchanted item
            if (random.nextInt(10) == 0) {
                List<ItemStack> loot = event.getLoot();
                
                // Create enchanted book with custom enchantment
                ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantedBook.getItemMeta();
                
                // 50% chance for each enchantment
                if (random.nextBoolean()) {
                    int level = random.nextInt(3) + 1; // Random level 1-3
                    meta.addStoredEnchant(harvesterEnchantment, level, true);
                } else {
                    meta.addStoredEnchant(tunnelingEnchantment, 1, true);
                }
                
                enchantedBook.setItemMeta(meta);
                loot.add(enchantedBook);
            }
        }
    }
    
    /**
     * Harvests crops in a radius around a center block.
     *
     * @param center The center block
     * @param player The player harvesting
     * @param tool The tool used
     * @param radius The radius to harvest
     */
    private void harvestCropsInRadius(Block center, Player player, ItemStack tool, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Skip the center block as it's already being harvested
                if (x == 0 && z == 0) {
                    continue;
                }
                
                Block relative = center.getRelative(x, 0, z);
                
                // Check if the block is a fully grown crop
                if (HarvesterEnchantment.isFullyGrownCrop(relative)) {
                    // Break the crop naturally
                    relative.breakNaturally(tool);
                }
            }
        }
    }
    
    /**
     * Mines blocks in a 3x3 area around a center block.
     *
     * @param center The center block
     * @param player The player mining
     * @param tool The tool used
     */
    private void mineBlocksIn3x3(Block center, Player player, ItemStack tool) {
        // Get the block face the player is looking at
        BlockFace face = getBlockFace(player);
        
        // Mine blocks in a 3x3 area perpendicular to the face
        if (face == BlockFace.UP || face == BlockFace.DOWN) {
            // Mine in X-Z plane
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    // Skip the center block as it's already being mined
                    if (x == 0 && z == 0) {
                        continue;
                    }
                    
                    mineBlock(center.getRelative(x, 0, z), player, tool);
                }
            }
        } else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
            // Mine in X-Y plane
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    // Skip the center block as it's already being mined
                    if (x == 0 && y == 0) {
                        continue;
                    }
                    
                    mineBlock(center.getRelative(x, y, 0), player, tool);
                }
            }
        } else {
            // Mine in Z-Y plane
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    // Skip the center block as it's already being mined
                    if (z == 0 && y == 0) {
                        continue;
                    }
                    
                    mineBlock(center.getRelative(0, y, z), player, tool);
                }
            }
        }
    }
    
    /**
     * Mines a block if it can be mined with a pickaxe.
     *
     * @param block The block to mine
     * @param player The player mining
     * @param tool The tool used
     */
    private void mineBlock(Block block, Player player, ItemStack tool) {
        // Check if the block can be mined with a pickaxe
        if (isMineable(block.getType())) {
            // Break the block naturally
            block.breakNaturally(tool);
        }
    }
    
    /**
     * Gets the block face the player is looking at.
     *
     * @param player The player
     * @return The block face
     */
    private BlockFace getBlockFace(Player player) {
        float pitch = player.getLocation().getPitch();
        
        if (pitch <= -45) {
            return BlockFace.UP;
        } else if (pitch >= 45) {
            return BlockFace.DOWN;
        } else {
            float yaw = player.getLocation().getYaw();
            
            if (yaw < 0) {
                yaw += 360;
            }
            
            if (yaw >= 315 || yaw < 45) {
                return BlockFace.SOUTH;
            } else if (yaw < 135) {
                return BlockFace.WEST;
            } else if (yaw < 225) {
                return BlockFace.NORTH;
            } else {
                return BlockFace.EAST;
            }
        }
    }
    
    // Cache for mineable materials to improve performance
    private static final java.util.Set<Material> MINEABLE_MATERIALS = new java.util.HashSet<>();
    
    // Initialize the mineable materials cache
    static {
        // Add specific materials
        java.util.Set<Material> specificMaterials = java.util.Set.of(
            Material.OBSIDIAN,
            Material.NETHERRACK,
            Material.END_STONE,
            Material.BEDROCK,
            Material.ANCIENT_DEBRIS
        );
        MINEABLE_MATERIALS.addAll(specificMaterials);
        
        // Add materials by name pattern
        for (Material material : Material.values()) {
            if (material.isBlock()) {
                String name = material.name();
                if (name.contains("STONE") || 
                    name.contains("ORE") || 
                    name.contains("BRICK") || 
                    name.contains("CONCRETE") || 
                    name.contains("TERRACOTTA")) {
                    MINEABLE_MATERIALS.add(material);
                }
            }
        }
    }
    
    /**
     * Checks if a material can be mined with a pickaxe.
     *
     * @param material The material to check
     * @return True if the material can be mined with a pickaxe
     */
    private boolean isMineable(Material material) {
        return MINEABLE_MATERIALS.contains(material);
    }
}
