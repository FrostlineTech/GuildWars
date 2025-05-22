package com.guildwars.enchantments;

import com.guildwars.GuildWars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;

import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Manager for custom enchantments.
 */
public class CustomEnchantmentManager implements Listener {

    private final GuildWars plugin;
    private final Random random = new Random();
    
    // Set of crop materials
    private static final Set<Material> CROP_TYPES = new HashSet<>();
    
    // Set of mineable materials
    private static final Set<Material> MINEABLE_TYPES = new HashSet<>();
    
    static {
        // Initialize crop types
        CROP_TYPES.add(Material.WHEAT);
        CROP_TYPES.add(Material.POTATOES);
        CROP_TYPES.add(Material.CARROTS);
        CROP_TYPES.add(Material.BEETROOTS);
        CROP_TYPES.add(Material.NETHER_WART);
        
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
        
        // Other mineable blocks
        MINEABLE_TYPES.add(Material.NETHERRACK);
        MINEABLE_TYPES.add(Material.END_STONE);
        MINEABLE_TYPES.add(Material.OBSIDIAN);
        MINEABLE_TYPES.add(Material.CRYING_OBSIDIAN);
        MINEABLE_TYPES.add(Material.BLACKSTONE);
        MINEABLE_TYPES.add(Material.BASALT);
    }
    
    /**
     * Creates a new custom enchantment manager.
     *
     * @param plugin The plugin instance
     */
    public CustomEnchantmentManager(GuildWars plugin) {
        this.plugin = plugin;
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("Custom enchantment manager initialized.");
    }
    
    /**
     * Adds a custom enchantment to an item.
     *
     * @param item The item to enchant
     * @param type The type of enchantment
     * @param level The level of the enchantment
     * @return The enchanted item
     */
    public ItemStack addEnchantment(ItemStack item, CustomEnchantmentType type, int level) {
        if (item == null || !type.canEnchantItem(item)) {
            return item;
        }
        
        // Clamp level to valid range
        level = Math.max(1, Math.min(level, type.getMaxLevel()));
        
        // Get item meta
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        
        // Add enchantment data
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, type.getKey());
        container.set(key, PersistentDataType.INTEGER, level);
        
        // Add enchantment lore
        List<net.kyori.adventure.text.Component> lore = meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        
        // Remove existing enchantment lore for this type
        lore.removeIf(component -> {
            String plain = component.toString();
            return plain.contains(type.getDisplayName());
        });
        
        // Add new enchantment lore
        lore.add(net.kyori.adventure.text.Component.text(type.getFormattedName(level)));
        meta.lore(lore);
        
        // Add enchantment glow
        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        
        // Apply meta to item
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Checks if an item has a custom enchantment.
     *
     * @param item The item to check
     * @param type The type of enchantment
     * @return True if the item has the enchantment
     */
    public boolean hasEnchantment(ItemStack item, CustomEnchantmentType type) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, type.getKey());
        
        return container.has(key, PersistentDataType.INTEGER);
    }
    
    /**
     * Gets the level of a custom enchantment on an item.
     *
     * @param item The item to check
     * @param type The type of enchantment
     * @return The level of the enchantment, or 0 if not present
     */
    public int getEnchantmentLevel(ItemStack item, CustomEnchantmentType type) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return 0;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, type.getKey());
        
        return container.getOrDefault(key, PersistentDataType.INTEGER, 0);
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
        if (hasEnchantment(tool, CustomEnchantmentType.HARVESTER)) {
            int level = getEnchantmentLevel(tool, CustomEnchantmentType.HARVESTER);
            
            // Check if the block is a fully grown crop
            if (isFullyGrownCrop(block)) {
                // Get radius based on enchantment level
                int radius = level;
                
                // Harvest crops in radius
                harvestCropsInRadius(block, player, tool, radius);
            }
        }
        
        // Check for Tunneling enchantment
        if (hasEnchantment(tool, CustomEnchantmentType.TUNNELING)) {
            // Check if the block can be mined with a pickaxe
            if (MINEABLE_TYPES.contains(block.getType())) {
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
                    addEnchantment(item, CustomEnchantmentType.HARVESTER, level);
                    event.getEnchanter().sendMessage(ChatColor.GOLD + "Your tool was blessed with " + 
                            ChatColor.AQUA + "Harvester " + level + ChatColor.GOLD + "!");
                });
            }
            
            // Add Tunneling to pickaxes
            else if (item.getType().name().endsWith("_PICKAXE")) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    addEnchantment(item, CustomEnchantmentType.TUNNELING, 1);
                    event.getEnchanter().sendMessage(ChatColor.GOLD + "Your tool was blessed with " + 
                            ChatColor.AQUA + "Tunneling" + ChatColor.GOLD + "!");
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
                
                if (meta != null) {
                    // Add glowing effect
                    meta.addStoredEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                    
                    // Add lore
                    List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                    
                    // 50% chance for each enchantment
                    if (random.nextBoolean()) {
                        int level = random.nextInt(3) + 1; // Random level 1-3
                        
                        // Add enchantment data
                        PersistentDataContainer container = meta.getPersistentDataContainer();
                        NamespacedKey key = new NamespacedKey(plugin, CustomEnchantmentType.HARVESTER.getKey());
                        container.set(key, PersistentDataType.INTEGER, level);
                        
                        // Add lore
                        lore.add(net.kyori.adventure.text.Component.text(CustomEnchantmentType.HARVESTER.getFormattedName(level)));
                    } else {
                        // Add enchantment data
                        PersistentDataContainer container = meta.getPersistentDataContainer();
                        NamespacedKey key = new NamespacedKey(plugin, CustomEnchantmentType.TUNNELING.getKey());
                        container.set(key, PersistentDataType.INTEGER, 1);
                        
                        // Add lore
                        lore.add(net.kyori.adventure.text.Component.text(CustomEnchantmentType.TUNNELING.getFormattedName(1)));
                    }
                    
                    meta.lore(lore);
                    enchantedBook.setItemMeta(meta);
                    loot.add(enchantedBook);
                }
            }
        }
    }
    
    /**
     * Handles the PrepareAnvilEvent to preserve custom enchantments.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        
        if (result == null || first == null) {
            return;
        }
        
        // Transfer custom enchantments from first item to result
        for (CustomEnchantmentType type : CustomEnchantmentType.values()) {
            if (hasEnchantment(first, type)) {
                int level = getEnchantmentLevel(first, type);
                
                // If second item has the same enchantment, use the higher level
                if (second != null && hasEnchantment(second, type)) {
                    int secondLevel = getEnchantmentLevel(second, type);
                    if (secondLevel > level) {
                        level = secondLevel;
                    }
                }
                
                addEnchantment(result, type, level);
            }
        }
        
        // Transfer custom enchantments from second item to result if it's an enchanted book
        if (second != null && second.getType() == Material.ENCHANTED_BOOK) {
            for (CustomEnchantmentType type : CustomEnchantmentType.values()) {
                if (hasEnchantment(second, type) && !hasEnchantment(result, type)) {
                    int level = getEnchantmentLevel(second, type);
                    if (type.canEnchantItem(result)) {
                        addEnchantment(result, type, level);
                    }
                }
            }
        }
        
        event.setResult(result);
    }
    
    /**
     * Checks if a block is a fully grown crop.
     *
     * @param block The block to check
     * @return True if the block is a fully grown crop
     */
    private boolean isFullyGrownCrop(Block block) {
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
                if (isFullyGrownCrop(relative)) {
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
        if (MINEABLE_TYPES.contains(block.getType())) {
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
}
