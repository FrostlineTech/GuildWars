package com.guildwars.enchantments;

import com.guildwars.GuildWars;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for custom enchantments.
 */
public class CustomEnchantmentManager implements Listener {

    private final GuildWars plugin;
    private final Random random = new Random();
    
    // Map to track players with active haste enchantment (using ConcurrentHashMap for thread safety)
    private final Map<UUID, Integer> activeHasteEffects = new ConcurrentHashMap<>();
    
    // Map to track players with active climb enchantment (using ConcurrentHashMap for thread safety)
    private final Map<UUID, Boolean> activeClimbEffects = new ConcurrentHashMap<>();
    
    // Map to track cooldowns for various enchantments (playerUUID -> (enchantmentType -> cooldownEndTime))
    private final Map<UUID, Map<CustomEnchantmentType, Long>> cooldowns = new ConcurrentHashMap<>();
    
    // Default cooldowns in milliseconds for enchantments that need it
    private static final Map<CustomEnchantmentType, Long> DEFAULT_COOLDOWNS = new HashMap<>();
    
    static {
        // Initialize default cooldowns (in milliseconds)
        DEFAULT_COOLDOWNS.put(CustomEnchantmentType.TUNNELING, 500L); // 500ms cooldown for Tunneling to prevent lag
        DEFAULT_COOLDOWNS.put(CustomEnchantmentType.SHOVEL_TUNNELING, 500L); // 500ms cooldown for Shovel Tunneling
        DEFAULT_COOLDOWNS.put(CustomEnchantmentType.HARVESTER, 1000L); // 1s cooldown for Harvester
        DEFAULT_COOLDOWNS.put(CustomEnchantmentType.SECOND_WIND, 60000L); // 60s cooldown for Second Wind
        DEFAULT_COOLDOWNS.put(CustomEnchantmentType.SHOCKWAVE, 10000L); // 10s cooldown for Shockwave
        DEFAULT_COOLDOWNS.put(CustomEnchantmentType.SHADOWSTEP, 10000L); // 10s cooldown for Shadowstep
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
     * Checks if a player has an enchantment on cooldown.
     * 
     * @param player The player to check
     * @param type The enchantment type
     * @return True if the enchantment is on cooldown
     */
    private boolean isOnCooldown(Player player, CustomEnchantmentType type) {
        UUID playerUuid = player.getUniqueId();
        if (!cooldowns.containsKey(playerUuid) || !cooldowns.get(playerUuid).containsKey(type)) {
            return false;
        }
        
        long cooldownEnd = cooldowns.get(playerUuid).get(type);
        return System.currentTimeMillis() < cooldownEnd;
    }
    
    /**
     * Sets a cooldown for an enchantment.
     * 
     * @param player The player
     * @param type The enchantment type
     */
    private void setCooldown(Player player, CustomEnchantmentType type) {
        if (!DEFAULT_COOLDOWNS.containsKey(type)) {
            return; // No cooldown for this enchantment
        }
        
        UUID playerUuid = player.getUniqueId();
        long cooldownDuration = DEFAULT_COOLDOWNS.get(type);
        long cooldownEnd = System.currentTimeMillis() + cooldownDuration;
        
        // Initialize the player's cooldown map if needed
        cooldowns.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                .put(type, cooldownEnd);
        
        // Schedule automatic cleanup of expired cooldowns
        new BukkitRunnable() {
            @Override
            public void run() {
                if (cooldowns.containsKey(playerUuid)) {
                    cooldowns.get(playerUuid).remove(type);
                    
                    // If player has no more cooldowns, remove the entire entry
                    if (cooldowns.get(playerUuid).isEmpty()) {
                        cooldowns.remove(playerUuid);
                    }
                }
            }
        }.runTaskLater(plugin, (cooldownDuration / 50) + 1); // Convert ms to ticks (1 tick = 50ms)
    }
    
    /**
     * Handles player quit to clean up resources.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();
        
        // Clean up haste effects
        activeHasteEffects.remove(playerUuid);
        
        // Clean up cooldowns
        cooldowns.remove(playerUuid);
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
        if (item == null || item.getType() == Material.AIR) {
            return item;
        }
        
        // Check if the enchantment can be applied to this item type
        if (!type.canEnchantItem(item)) {
            return item;
        }
        
        // Limit level to the max level of the enchantment
        level = Math.min(level, type.getMaxLevel());
        
        // Get item meta
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        
        // Store the enchantment in the item's persistent data container
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, type.getKey());
        container.set(key, PersistentDataType.INTEGER, level);
        
        // Add enchantment lore
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        
        // Remove all existing entries related to this enchantment (name, separator, and description)
        // First, find if the enchantment exists already
        int enchantmentIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains(type.getDisplayName())) {
                enchantmentIndex = i;
                break;
            }
        }
        
        // If found, remove all related lines (name, separator, description lines)
        if (enchantmentIndex >= 0) {
            // Remove up to 4 lines (enchantment name, separator, description header, description)
            int linesToRemove = Math.min(4, lore.size() - enchantmentIndex);
            for (int i = 0; i < linesToRemove; i++) {
                lore.remove(enchantmentIndex);
            }
        }
        
        // Add the enchantment name with proper formatting (like vanilla enchantments)
        // Purple color with no italics, just like vanilla
        lore.add(0, ChatColor.LIGHT_PURPLE + type.getDisplayName() + " " + type.getRomanNumeral(level));
        
        // Add a separator line for visual clarity
        lore.add(1, ChatColor.DARK_GRAY + "⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        
        // Add the enchantment description with gray color
        lore.add(2, ChatColor.GRAY + "When active:");
        lore.add(3, ChatColor.GRAY + type.getDescription());
        
        // Set the updated lore
        meta.setLore(lore);
        
        // Also store in the PersistentDataContainer for programmatic access
        NamespacedKey enchNameKey = new NamespacedKey(plugin, "enchant_display_" + type.getKey());
        container.set(enchNameKey, PersistentDataType.STRING, type.getFormattedName(level));
        
        // Hide the vanilla enchantment we're using for the glow effect
        // but keep our custom enchantment visible through lore
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
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
        try {
            Player player = event.getPlayer();
            Block block = event.getBlock();
            ItemStack tool = player.getInventory().getItemInMainHand();
            Material toolType = tool.getType();
            
            // Check if the tool has any custom enchantments
            if (toolType == Material.AIR || !hasCustomEnchantments(tool)) {
                return;
            }
            
            Material blockType = block.getType();
            
            // Process harvester enchantment for hoes on crops
            if (isCrop(blockType) && toolType.name().endsWith("_HOE")) {
                if (hasEnchantment(tool, CustomEnchantmentType.HARVESTER)) {
                    // Check cooldown before processing
                    if (!isOnCooldown(player, CustomEnchantmentType.HARVESTER)) {
                        processHarvesterEnchantment(event, block, tool, player);
                        setCooldown(player, CustomEnchantmentType.HARVESTER);
                    }
                }
            }
            
            // Process pickaxe enchantments
            if (MaterialCategories.isPickaxe(toolType)) {
                if (MaterialCategories.isPickaxeMineable(blockType)) {
                    // Check for Treasure Hunter enchantment
                    if (hasEnchantment(tool, CustomEnchantmentType.TREASURE_HUNTER) && 
                        MaterialCategories.isValuableBlock(blockType)) {
                        int level = getEnchantmentLevel(tool, CustomEnchantmentType.TREASURE_HUNTER);
                        handleTreasureHunter(event, block, player, level);
                    }
                    
                    // Check for Auto Smelt enchantment
                    if (hasEnchantment(tool, CustomEnchantmentType.AUTO_SMELT) && 
                        MaterialCategories.isSmeltable(blockType)) {
                        handleAutoSmelt(event, block, player);
                    }
                    
                    // Check for Tunneling enchantment (with cooldown to prevent lag)
                    if (hasEnchantment(tool, CustomEnchantmentType.TUNNELING)) {
                        if (!isOnCooldown(player, CustomEnchantmentType.TUNNELING)) {
                            mineBlocksIn3x3(block, player, tool);
                            setCooldown(player, CustomEnchantmentType.TUNNELING);
                        }
                    }
                }
            }
            
            // Process shovel enchantments
            if (toolType.name().endsWith("_SHOVEL") && isShovelDiggable(blockType)) {
                // Check for Shovel Tunneling enchantment (with cooldown)
                if (hasEnchantment(tool, CustomEnchantmentType.SHOVEL_TUNNELING)) {
                    if (!isOnCooldown(player, CustomEnchantmentType.SHOVEL_TUNNELING)) {
                        mineBlocksIn3x3(block, player, tool);
                        setCooldown(player, CustomEnchantmentType.SHOVEL_TUNNELING);
                    }
                }
            }
        } catch (Exception e) {
            // Add error handling to prevent plugin crashes
            plugin.getLogger().warning("Error handling block break event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Process axes with harvest enchantment if breaking crops
     */
    private void processHarvesterEnchantment(BlockBreakEvent event, Block block, ItemStack tool, Player player) {
        // Check if it's a crop
        if (!isCrop(block.getType())) {
            return;
        }
        
        // Get enchantment level (radius increases with level)
        int level = getEnchantmentLevel(tool, CustomEnchantmentType.HARVESTER);
        // Harvest crops in the radius determined by enchantment level
        harvestCropsInRadius(block, player, tool, level);
    }
    
    /**
     * Checks if a material is a crop.
     * 
     * @param material The material to check
     * @return True if the material is a crop
     */
    private boolean isCrop(Material material) {
        return MaterialCategories.isCrop(material);
    }
    
    /**
     * Checks if an item has any custom enchantments.
     *
     * @param item The item to check
     * @return True if the item has any custom enchantments
     */
    private boolean hasCustomEnchantments(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        for (CustomEnchantmentType type : CustomEnchantmentType.values()) {
            NamespacedKey key = new NamespacedKey(plugin, type.getKey());
            if (container.has(key, PersistentDataType.INTEGER)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Harvests crops in a radius around the center block.
     *
     * @param centerBlock The center block
     * @param player The player harvesting
     * @param tool The tool being used
     * @param radius The radius around the center block to harvest
     */
    private void harvestCropsInRadius(Block centerBlock, Player player, ItemStack tool, int radius) {
        // Check blocks in radius
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Skip the center block as it's already broken by the original event
                if (x == 0 && z == 0) continue;
                
                Block relativeBlock = centerBlock.getRelative(x, 0, z);
                
                // Check if it's a fully grown crop
                if (isFullyGrownCrop(relativeBlock)) {
                    // Break the block and drop items naturally
                    relativeBlock.breakNaturally(tool);
                    
                    // Small chance to damage the tool
                    if (random.nextInt(20) == 0 && tool.getType().getMaxDurability() > 0) {
                        ItemMeta meta = tool.getItemMeta();
                        if (meta != null && meta.isUnbreakable()) {
                            continue; // Skip damage for unbreakable tools
                        }
                        
                        // Damage the tool
                        if (meta instanceof Damageable) {
                            Damageable damageable = (Damageable) meta;
                            damageable.setDamage(damageable.getDamage() + 1);
                            tool.setItemMeta(meta);
                        }
                        
                        // Check if tool should break
                        if (meta instanceof Damageable) {
                            Damageable damageable = (Damageable) meta;
                            if (damageable.getDamage() >= tool.getType().getMaxDurability()) {
                                // Break the tool
                                player.getInventory().setItemInMainHand(null);
                                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Checks if a block is a fully grown crop.
     *
     * @param block The block to check
     * @return True if the block is a fully grown crop
     */
    private boolean isFullyGrownCrop(Block block) {
        if (isCrop(block.getType())) {
            BlockData blockData = block.getBlockData();
            if (blockData instanceof Ageable) {
                Ageable ageable = (Ageable) blockData;
                return ageable.getAge() == ageable.getMaximumAge();
            }
        }
        return false;
    }
    
    /**
     * Checks if a material can be mined with a pickaxe.
     * 
     * @param material The material to check
     * @return True if the material can be mined with a pickaxe
     */
    private boolean isPickaxeMineable(Material material) {
        return MaterialCategories.isPickaxeMineable(material);
    }
    
    /**
     * Mines blocks in a 3x3 area around the target block.
     *
     * @param centerBlock The center block
     * @param player The player mining
     * @param tool The tool being used
     */
    private void mineBlocksIn3x3(Block centerBlock, Player player, ItemStack tool) {
        // Play effect at center
        player.getWorld().spawnParticle(Particle.CRIT, centerBlock.getLocation().add(0.5, 0.5, 0.5), 10, 0.5, 0.5, 0.5, 0);
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 0.8f, 1.0f);
        
        // Get the face the player is looking at
        BlockFace face = getBlockFace(player);
        
        // Mine blocks in a 3x3 grid perpendicular to the face the player is looking at
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                // Skip the center block as it's already broken by the original event
                if (x == 0 && y == 0) continue;
                
                Block relativeBlock;
                
                // Determine which plane to mine in based on the face
                if (face == BlockFace.UP || face == BlockFace.DOWN) {
                    // Mine in the X-Z plane
                    relativeBlock = centerBlock.getRelative(x, 0, y);
                } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
                    // Mine in the Y-Z plane
                    relativeBlock = centerBlock.getRelative(0, x, y);
                } else {
                    // Mine in the X-Y plane
                    relativeBlock = centerBlock.getRelative(x, y, 0);
                }
                
                // Check if the block can be mined with the current tool
                boolean canMine = false;
                if (tool.getType().name().contains("_PICKAXE") && isPickaxeMineable(relativeBlock.getType())) {
                    canMine = true;
                } else if (tool.getType().name().contains("_SHOVEL") && isShovelDiggable(relativeBlock.getType())) {
                    canMine = true;
                }
                
                if (canMine) {
                    // Break the block and drop items naturally
                    relativeBlock.breakNaturally(tool);
                    
                    // Small chance to damage the tool
                    if (random.nextInt(10) == 0 && tool.getType().getMaxDurability() > 0) {
                        ItemMeta meta = tool.getItemMeta();
                        if (meta != null && meta.isUnbreakable()) {
                            continue; // Skip damage for unbreakable tools
                        }
                        
                        // Damage the tool
                        if (meta instanceof Damageable) {
                            Damageable damageable = (Damageable) meta;
                            damageable.setDamage(damageable.getDamage() + 1);
                            tool.setItemMeta(meta);
                        }
                        
                        // Check if tool should break
                        if (meta instanceof Damageable) {
                            Damageable damageable = (Damageable) meta;
                            if (damageable.getDamage() >= tool.getType().getMaxDurability()) {
                                // Break the tool
                                player.getInventory().setItemInMainHand(null);
                                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Gets the block face the player is looking at.
     *
     * @param player The player
     * @return The block face
     */
    private BlockFace getBlockFace(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 100);
        if (lastTwoTargetBlocks.size() != 2) {
            return BlockFace.UP;
        }
        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
    }

/**
 * Handles enchantment application on regular enchanting table.
 */
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onEnchantItem(EnchantItemEvent event) {
    // Check if custom enchantments are enabled
    if (!plugin.getConfig().getBoolean("general.custom-enchantments.enabled", true)) {
        return;
    }
    
    // Get chance from config (default 15 if not found)
    int chance = plugin.getConfig().getInt("general.custom-enchantments.enchant-table-chance", 15);
    
    // Random chance to add a custom enchantment
    if (random.nextInt(chance) == 0) {
        // Determine which custom enchantment to add based on the item type
        ItemStack item = event.getItem();
        CustomEnchantmentType type = null;
        int level = 1;
        
        // Select appropriate enchantment based on item type
        if (item.getType().name().contains("_HOE")) {
            type = CustomEnchantmentType.HARVESTER;
            level = Math.min(3, event.getEnchantsToAdd().size());
        } else if (item.getType().name().contains("_PICKAXE")) {
            // For pickaxes, randomly select one of the pickaxe enchantments
            int randomPickaxe = random.nextInt(4);
            switch (randomPickaxe) {
                case 0:
                    type = CustomEnchantmentType.TUNNELING;
                    break;
                case 1:
                    type = CustomEnchantmentType.AUTO_SMELT;
                    break;
                case 2:
                    type = CustomEnchantmentType.HASTE;
                    level = Math.min(2, event.getEnchantsToAdd().size());
                    break;
                case 3:
                    type = CustomEnchantmentType.TREASURE_HUNTER;
                    level = Math.min(2, event.getEnchantsToAdd().size());
                    break;
            }
        } else if (item.getType().name().contains("_SHOVEL")) {
            type = CustomEnchantmentType.SHOVEL_TUNNELING;
        } else if (item.getType().name().contains("_AXE")) {
            // For axes, randomly select one of the axe enchantments
            int randomAxe = random.nextInt(3);
            switch (randomAxe) {
                case 0:
                    type = CustomEnchantmentType.ARMOR_CRACK;
                    level = Math.min(4, event.getEnchantsToAdd().size());
                    break;
                case 1:
                    type = CustomEnchantmentType.SHOCKWAVE;
                    level = Math.min(2, event.getEnchantsToAdd().size());
                    break;
                case 2:
                    type = CustomEnchantmentType.RUPTURE;
                    level = Math.min(3, event.getEnchantsToAdd().size());
                    break;
            }
        } else if (item.getType().name().contains("_HELMET") || 
                 item.getType().name().contains("_CHESTPLATE") || 
                 item.getType().name().contains("_LEGGINGS") || 
                 item.getType().name().contains("_BOOTS")) {
            // For armor, randomly select one of the armor enchantments
            int randomArmor = random.nextInt(5);
            switch (randomArmor) {
                case 0:
                    type = CustomEnchantmentType.MOLTEN;
                    level = Math.min(2, event.getEnchantsToAdd().size());
                    break;
                case 1:
                    type = CustomEnchantmentType.GUARDIANS;
                    break;
                case 2:
                    type = CustomEnchantmentType.SECOND_WIND;
                    break;
                case 3:
                    type = CustomEnchantmentType.STABILITY;
                    level = Math.min(3, event.getEnchantsToAdd().size());
                    break;
                case 4:
                    type = CustomEnchantmentType.THORN_BURST;
                    level = Math.min(2, event.getEnchantsToAdd().size());
                    break;
            }
        } else if (item.getType().name().contains("_BOOTS")) {
            // For boots, randomly select one of the boot-specific enchantments
            int randomBoots = random.nextInt(4);
            switch (randomBoots) {
                case 0:
                    type = CustomEnchantmentType.CLIMB;
                    break;
                case 1:
                    type = CustomEnchantmentType.SHOCK_ABSORB;
                    level = Math.min(2, event.getEnchantsToAdd().size());
                    break;
                case 2:
                    type = CustomEnchantmentType.SPEED_BOOST;
                    level = Math.min(2, event.getEnchantsToAdd().size());
                    break;
                case 3:
                    type = CustomEnchantmentType.SHADOWSTEP;
                    break;
            }
        } else if (item.getType().name().contains("_SWORD")) {
            // For swords, randomly select one of the sword enchantments
            int randomSword = random.nextInt(5);
            switch (randomSword) {
                case 0:
                    type = CustomEnchantmentType.EXECUTE;
                    level = Math.min(3, event.getEnchantsToAdd().size());
                    break;
                case 1:
                    type = CustomEnchantmentType.VAMPIRIC_EDGE;
                    level = Math.min(2, event.getEnchantsToAdd().size());
                    break;
                case 2:
                    type = CustomEnchantmentType.CRIPPLING_STRIKE;
                    level = Math.min(3, event.getEnchantsToAdd().size());
                    break;
                case 3:
                    type = CustomEnchantmentType.FRENZY;
                    level = Math.min(3, event.getEnchantsToAdd().size());
                    break;
                case 4:
                    type = CustomEnchantmentType.MARK_OF_DEATH;
                    break;
            }
        }
        
        // If a valid enchantment was selected, add it
        if (type != null) {
            // Add the enchantment
            addEnchantment(event.getItem(), type, level);
            
            // Send message to player
            event.getEnchanter().sendMessage(ChatColor.LIGHT_PURPLE + "Your item has been blessed with a rare enchantment!");
        }
    }
    }
    

    /**
     * Handles the Auto Smelt enchantment effect.
     * 
     * @param event The block break event
     * @param block The block that was broken
     * @param player The player who broke the block
     */
    private void handleAutoSmelt(BlockBreakEvent event, Block block, Player player) {
        Material blockType = block.getType();

        // Check if the block is a smeltable ore using MaterialCategories
        if (MaterialCategories.isSmeltable(blockType)) {
            // Cancel the original drops
            event.setDropItems(false);

            // Get the smelted item type
            Material smeltedType = MaterialCategories.getSmeltedResult(blockType);

            // Create the smelted item stack (with appropriate quantity)
            ItemStack smeltedItem = new ItemStack(smeltedType);

            // For ores that drop multiple items when smelted
            if (blockType == Material.NETHER_GOLD_ORE) {
                smeltedItem.setAmount(random.nextInt(3) + 2); // 2-4 gold nuggets
            } else if (blockType == Material.REDSTONE_ORE || blockType == Material.DEEPSLATE_REDSTONE_ORE) {
                smeltedItem.setAmount(random.nextInt(3) + 3); // 3-5 redstone
            } else if (blockType == Material.LAPIS_ORE || blockType == Material.DEEPSLATE_LAPIS_ORE) {
                smeltedItem.setAmount(random.nextInt(3) + 3); // 3-5 lapis
            }

            // Drop the smelted item at the block location
            block.getWorld().dropItemNaturally(block.getLocation(), smeltedItem);

            // Visual and sound effects
            block.getWorld().spawnParticle(Particle.FLAME, 
                    block.getLocation().add(0.5, 0.5, 0.5), 8, 0.2, 0.2, 0.2, 0.01);
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.5f, 1.0f);

            // Grant experience as if the ore was smelted
            player.giveExp(2); // Standard smelting XP
        }
    }
    
    /**
     * Checks if a material can be dug with a shovel.
     * 
     * @param material The material to check
     * @return True if the material can be dug with a shovel
     */
    private boolean isShovelDiggable(Material material) {
        return MaterialCategories.isShovelDiggable(material);
    }

    /**
     * Handles the Treasure Hunter enchantment effect.
     * 
     * @param event The block break event
     * @param block The block that was broken
     * @param player The player who broke the block
     * @param level The level of the enchantment
     */
    private void handleTreasureHunter(BlockBreakEvent event, Block block, Player player, int level) {
        // Only apply to certain blocks (ores, etc.)
        Material blockType = block.getType();
        if (!isValuableBlock(blockType)) {
            return;
        }
        
        // Calculate chance based on level (5% per level)
        int chance = level * 5;
        
        if (random.nextInt(100) < chance) {
            // Determine what extra drop to give based on the block type
            ItemStack extraDrop = getExtraDropForBlock(blockType);
            
            if (extraDrop != null) {
                // Drop the extra item at the block location
                block.getWorld().dropItemNaturally(block.getLocation(), extraDrop);
                
                // Visual and sound effects
                block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, 
                        block.getLocation().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.05);
                block.getWorld().playSound(block.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                
                // Notify the player
                player.sendMessage(ChatColor.GOLD + "Your Treasure Hunter enchantment found something special!");
            }
        }
    }
    
    /**
     * Gets an extra drop for the Treasure Hunter enchantment based on block type.
     * 
     * @param blockType The type of block that was broken
     * @return An ItemStack representing the extra drop, or null if no extra drop
     */
    private ItemStack getExtraDropForBlock(Material blockType) {
        // Determine what extra drop to give based on the block type
        if (blockType == Material.DIAMOND_ORE || blockType == Material.DEEPSLATE_DIAMOND_ORE) {
            return new ItemStack(Material.DIAMOND, 1);
        } else if (blockType == Material.EMERALD_ORE || blockType == Material.DEEPSLATE_EMERALD_ORE) {
            return new ItemStack(Material.EMERALD, 1);
        } else if (blockType == Material.GOLD_ORE || blockType == Material.DEEPSLATE_GOLD_ORE || blockType == Material.NETHER_GOLD_ORE) {
            return new ItemStack(Material.GOLD_INGOT, 1);
        } else if (blockType == Material.IRON_ORE || blockType == Material.DEEPSLATE_IRON_ORE) {
            return new ItemStack(Material.IRON_INGOT, 1);
        } else if (blockType == Material.LAPIS_ORE || blockType == Material.DEEPSLATE_LAPIS_ORE) {
            return new ItemStack(Material.LAPIS_LAZULI, 4);
        } else if (blockType == Material.REDSTONE_ORE || blockType == Material.DEEPSLATE_REDSTONE_ORE) {
            return new ItemStack(Material.REDSTONE, 4);
        } else if (blockType == Material.COPPER_ORE || blockType == Material.DEEPSLATE_COPPER_ORE) {
            return new ItemStack(Material.COPPER_INGOT, 2);
        } else if (blockType == Material.COAL_ORE || blockType == Material.DEEPSLATE_COAL_ORE) {
            return new ItemStack(Material.COAL, 2);
        } else if (blockType == Material.ANCIENT_DEBRIS) {
            return new ItemStack(Material.NETHERITE_SCRAP, 1);
        } else if (blockType == Material.NETHER_QUARTZ_ORE) {
            return new ItemStack(Material.QUARTZ, 2);
        }
        
        return null;
    }
    
    /**
     * Checks if a block is valuable (for Treasure Hunter enchantment).
     * 
     * @param blockType The block type to check
     * @return True if the block is valuable
     */
    private boolean isValuableBlock(Material blockType) {
        return MaterialCategories.isValuableBlock(blockType);
    }
    
    /**
     * Handles the Haste enchantment effect.
     * This method will be used when implementing the PlayerItemHeldEvent handler.
     * 
     * @param player The player with the enchantment
     * @param level The level of the enchantment
     */
    private void handleHasteEffect(Player player, int level) {
        // Calculate haste effect level (amplifier = level - 1)
        int amplifier = level - 1; // Level 1 = Haste I, Level 2 = Haste II
        
        // Apply haste effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, amplifier, false, false, true));
        
        // Store the effect level for this player
        activeHasteEffects.put(player.getUniqueId(), level);
        
        // Visual effect
        player.getWorld().spawnParticle(Particle.CRIT, 
                player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
    }
    
    /**
     * Handles player item held change for Haste enchantment.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        
        // Check if player had a haste effect from previous item
        if (activeHasteEffects.containsKey(playerUuid)) {
            // Remove the effect
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
            activeHasteEffects.remove(playerUuid);
        }
        
        // Check if the new item has the Haste enchantment
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (newItem != null && hasEnchantment(newItem, CustomEnchantmentType.HASTE)) {
            int level = getEnchantmentLevel(newItem, CustomEnchantmentType.HASTE);
            handleHasteEffect(player, level);
        }
    }
    
    /**
     * Handles player movement for the Climb enchantment.
     * This allows players with boots enchanted with Climb to scale walls like spiders.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Skip if player is not against a wall or is on the ground
        if (player.isOnGround() || player.isSneaking()) {
            return;
        }
        
        // Check if player has boots with the Climb enchantment
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || !hasEnchantment(boots, CustomEnchantmentType.CLIMB)) {
            return;
        }
        
        // Check if the player is against a solid block (wall)
        if (isAgainstWall(player)) {
            // Cancel gravity effect - allow player to stick to the wall
            Vector velocity = player.getVelocity();
            
            // Only cancel downward velocity if the player is moving against a wall
            if (velocity.getY() < 0) {
                velocity.setY(0.0);
                player.setVelocity(velocity);
            }
            
            // Let player climb up if they're pressing the jump key (space)
            if (player.getVelocity().getY() > 0.1) {
                velocity.setY(0.2); // Climbing speed
                player.setVelocity(velocity);
            }
            
            // Visual effect to show the enchantment is working
            player.getWorld().spawnParticle(Particle.BLOCK_CRACK, 
                    player.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.1, 
                    Material.COBWEB.createBlockData());
            
            // Record that this player has an active climb effect
            activeClimbEffects.put(player.getUniqueId(), true);
        } else if (activeClimbEffects.containsKey(player.getUniqueId())) {
            // Remove the climb effect if player is no longer against a wall
            activeClimbEffects.remove(player.getUniqueId());
        }
    }
    
    /**
     * Checks if a player is against a solid wall.
     * 
     * @param player The player to check
     * @return True if the player is against a solid wall
     */
    private boolean isAgainstWall(Player player) {
        // Get the player's location
        Location loc = player.getLocation();
        
        // Check blocks around the player (cardinal directions)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // Skip corners and center
                if ((x != 0 && z != 0) || (x == 0 && z == 0)) {
                    continue;
                }
                
                // Get block at this position
                Block block = loc.getBlock().getRelative(x, 0, z);
                Block blockAbove = loc.getBlock().getRelative(x, 1, z);
                
                // Check if the block is solid and the block above it is solid too (for climbing)
                if (block.getType().isSolid() && blockAbove.getType().isSolid()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Handles damage events for armor enchantments.
     * This handles MOLTEN, GUARDIANS, SECOND_WIND, STABILITY, and THORN_BURST enchantments.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Skip if either entity is not a player
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Process armor enchantments for the damaged player
        processArmorEnchantments(event, player);
    }
    
    /**
     * Process armor enchantments for a player that took damage.
     * 
     * @param event The damage event
     * @param player The player who was damaged
     */
    private void processArmorEnchantments(EntityDamageByEntityEvent event, Player player) {
        // Skip if player is in cooldown for defensive enchantments
        if (isOnCooldown(player, CustomEnchantmentType.MOLTEN) || 
            isOnCooldown(player, CustomEnchantmentType.GUARDIANS) || 
            isOnCooldown(player, CustomEnchantmentType.SECOND_WIND) || 
            isOnCooldown(player, CustomEnchantmentType.THORN_BURST)) {
            return;
        }
        
        // Get player's armor items
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        
        // Check for MOLTEN enchantment - sets attacker on fire
        if (event.getDamager() instanceof LivingEntity && 
            (hasEnchantment(helmet, CustomEnchantmentType.MOLTEN) || 
             hasEnchantment(chestplate, CustomEnchantmentType.MOLTEN) || 
             hasEnchantment(leggings, CustomEnchantmentType.MOLTEN) || 
             hasEnchantment(boots, CustomEnchantmentType.MOLTEN))) {
            
            // Get highest level of MOLTEN enchantment
            int level = Math.max(
                getEnchantmentLevel(helmet, CustomEnchantmentType.MOLTEN),
                Math.max(
                    getEnchantmentLevel(chestplate, CustomEnchantmentType.MOLTEN),
                    Math.max(
                        getEnchantmentLevel(leggings, CustomEnchantmentType.MOLTEN),
                        getEnchantmentLevel(boots, CustomEnchantmentType.MOLTEN)
                    )
                )
            );
            
            // 20% chance to trigger the effect + 10% per level
            if (random.nextDouble() < (0.2 + (level * 0.1))) {
                // Set attacker on fire
                LivingEntity attacker = (LivingEntity) event.getDamager();
                int fireTicks = 40 + (level * 20); // 2 seconds + 1 second per level
                attacker.setFireTicks(fireTicks);
                
                // Visual and sound effects
                player.getWorld().spawnParticle(Particle.FLAME, 
                        attacker.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.05);
                player.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_GENERIC_BURN, 1.0f, 1.0f);
                
                // Set cooldown to prevent spamming
                setCooldown(player, CustomEnchantmentType.MOLTEN);
                
                // Send message to player
                player.sendMessage(ChatColor.RED + "Your " + ChatColor.GOLD + "Molten" + 
                                   ChatColor.RED + " enchantment set your attacker on fire!");
            }
        }
        
        // Check for GUARDIANS enchantment - spawns Iron Golem when attacked
        if (hasEnchantment(helmet, CustomEnchantmentType.GUARDIANS) || 
            hasEnchantment(chestplate, CustomEnchantmentType.GUARDIANS) || 
            hasEnchantment(leggings, CustomEnchantmentType.GUARDIANS) || 
            hasEnchantment(boots, CustomEnchantmentType.GUARDIANS)) {
            
            // 5% chance to trigger the effect
            if (random.nextDouble() < 0.05) {
                // Spawn an Iron Golem to protect the player
                IronGolem golem = (IronGolem) player.getWorld().spawnEntity(
                        player.getLocation(), EntityType.IRON_GOLEM);
                
                // Make the golem target the attacker if it's a living entity
                if (event.getDamager() instanceof LivingEntity) {
                    golem.setTarget((LivingEntity) event.getDamager());
                }
                
                // Set the golem to despawn after 30 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (golem != null && !golem.isDead()) {
                            golem.remove();
                        }
                    }
                }.runTaskLater(plugin, 30 * 20); // 30 seconds
                
                // Visual and sound effects
                player.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, 
                        player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 1.0f);
                
                // Set cooldown to prevent spamming
                setCooldown(player, CustomEnchantmentType.GUARDIANS);
                
                // Send message to player
                player.sendMessage(ChatColor.GRAY + "Your " + ChatColor.WHITE + "Guardians" + 
                                   ChatColor.GRAY + " enchantment summoned an Iron Golem to protect you!");
            }
        }
        
        // Check for SECOND_WIND enchantment - grants regeneration when low health
        if (hasEnchantment(helmet, CustomEnchantmentType.SECOND_WIND) || 
            hasEnchantment(chestplate, CustomEnchantmentType.SECOND_WIND) || 
            hasEnchantment(leggings, CustomEnchantmentType.SECOND_WIND) || 
            hasEnchantment(boots, CustomEnchantmentType.SECOND_WIND)) {
            
            // Trigger when health drops below 20% (4 hearts)
            if (player.getHealth() - event.getFinalDamage() <= 4.0) {
                // Apply regeneration effect
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1)); // 5 seconds of Regeneration II
                
                // Visual and sound effects
                player.getWorld().spawnParticle(Particle.HEART, 
                        player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
                
                // Set cooldown to prevent spamming
                setCooldown(player, CustomEnchantmentType.SECOND_WIND);
                
                // Send message to player
                player.sendMessage(ChatColor.RED + "Your " + ChatColor.GREEN + "Second Wind" + 
                                   ChatColor.RED + " enchantment has given you regeneration!");
            }
        }
        
        // Check for STABILITY enchantment - reduces knockback
        if (hasEnchantment(helmet, CustomEnchantmentType.STABILITY) || 
            hasEnchantment(chestplate, CustomEnchantmentType.STABILITY) || 
            hasEnchantment(leggings, CustomEnchantmentType.STABILITY) || 
            hasEnchantment(boots, CustomEnchantmentType.STABILITY)) {
            
            // Get highest level of STABILITY enchantment
            int level = Math.max(
                getEnchantmentLevel(helmet, CustomEnchantmentType.STABILITY),
                Math.max(
                    getEnchantmentLevel(chestplate, CustomEnchantmentType.STABILITY),
                    Math.max(
                        getEnchantmentLevel(leggings, CustomEnchantmentType.STABILITY),
                        getEnchantmentLevel(boots, CustomEnchantmentType.STABILITY)
                    )
                )
            );
            
            // Reduce knockback based on level (20% per level)
            double knockbackReduction = 0.2 * level;
            if (knockbackReduction > 0.8) knockbackReduction = 0.8; // Max 80% reduction
            
            // Apply knockback reduction
            Vector velocity = player.getVelocity();
            velocity.multiply(1.0 - knockbackReduction);
            player.setVelocity(velocity);
            
            // Visual effects (subtle)
            player.getWorld().spawnParticle(Particle.CRIT, 
                    player.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.02);
        }
        
        // Check for THORN_BURST enchantment - deals AoE damage to nearby entities
        if (hasEnchantment(helmet, CustomEnchantmentType.THORN_BURST) || 
            hasEnchantment(chestplate, CustomEnchantmentType.THORN_BURST) || 
            hasEnchantment(leggings, CustomEnchantmentType.THORN_BURST) || 
            hasEnchantment(boots, CustomEnchantmentType.THORN_BURST)) {
            
            // Get highest level of THORN_BURST enchantment
            int level = Math.max(
                getEnchantmentLevel(helmet, CustomEnchantmentType.THORN_BURST),
                Math.max(
                    getEnchantmentLevel(chestplate, CustomEnchantmentType.THORN_BURST),
                    Math.max(
                        getEnchantmentLevel(leggings, CustomEnchantmentType.THORN_BURST),
                        getEnchantmentLevel(boots, CustomEnchantmentType.THORN_BURST)
                    )
                )
            );
            
            // 15% chance to trigger the effect + 5% per level
            if (random.nextDouble() < (0.15 + (level * 0.05))) {
                // Radius based on level (3-4 blocks)
                double radius = 2.0 + (level * 0.5);
                
                // Damage based on level (1-2 hearts)
                double damage = 2.0 + (level * 1.0);
                
                // Damage nearby entities
                for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        target.damage(damage, player);
                    }
                }
                
                // Visual and sound effects
                player.getWorld().spawnParticle(Particle.CRIT_MAGIC, 
                        player.getLocation(), 50, radius/2, radius/2, radius/2, 0.1);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.5f, 2.0f);
                
                // Set cooldown to prevent spamming
                setCooldown(player, CustomEnchantmentType.THORN_BURST);
                
                // Send message to player
                player.sendMessage(ChatColor.DARK_PURPLE + "Your " + ChatColor.LIGHT_PURPLE + "Thorn Burst" + 
                                   ChatColor.DARK_PURPLE + " enchantment damaged nearby enemies!");
            }
        }
    }
    
    /**
     * Marks an arrow with enchantments from the bow that fired it.
     * 
     * @param arrow The arrow to mark
     * @param bow The bow that fired the arrow
     */
    private void markArrowWithEnchantments(Arrow arrow, ItemStack bow) {
        if (bow == null || !hasCustomEnchantments(bow)) {
            return;
        }
        
        PersistentDataContainer container = arrow.getPersistentDataContainer();
        PersistentDataContainer bowContainer = bow.getItemMeta().getPersistentDataContainer();
        
        // Transfer all enchantment data from bow to arrow
        for (CustomEnchantmentType type : CustomEnchantmentType.values()) {
            NamespacedKey key = new NamespacedKey(plugin, type.getKey());
            if (bowContainer.has(key, PersistentDataType.INTEGER)) {
                int level = bowContainer.get(key, PersistentDataType.INTEGER);
                container.set(key, PersistentDataType.INTEGER, level);
            }
        }
        
        // Mark this as an enchanted arrow
        container.set(new NamespacedKey(plugin, "enchanted_arrow"), PersistentDataType.BYTE, (byte) 1);
    }
    
    /**
     * Checks if an arrow has a specific enchantment.
     * 
     * @param arrow The arrow to check
     * @param type The enchantment type
     * @return True if the arrow has the enchantment
     */
    private boolean hasArrowEnchantment(Arrow arrow, CustomEnchantmentType type) {
        PersistentDataContainer container = arrow.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, type.getKey());
        return container.has(key, PersistentDataType.INTEGER);
    }
    
    /**
     * Gets the level of an enchantment on an arrow.
     * 
     * @param arrow The arrow to check
     * @param type The enchantment type
     * @return The level of the enchantment, or 0 if not present
     */
    private int getArrowEnchantmentLevel(Arrow arrow, CustomEnchantmentType type) {
        PersistentDataContainer container = arrow.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, type.getKey());
        
        if (container.has(key, PersistentDataType.INTEGER)) {
            return container.get(key, PersistentDataType.INTEGER);
        }
        
        return 0;
    }
    
    /**
     * Handles the EntityShootBowEvent for custom bow enchantments.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getProjectile() instanceof Arrow)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();
        Arrow arrow = (Arrow) event.getProjectile();
        
        if (bow == null || !hasCustomEnchantments(bow)) {
            return;
        }
        
        // Mark the arrow with enchantments from the bow
        markArrowWithEnchantments(arrow, bow);
        
        // Handle MULTI_SHOT enchantment
        if (hasEnchantment(bow, CustomEnchantmentType.MULTI_SHOT) && !isOnCooldown(player, CustomEnchantmentType.MULTI_SHOT)) {
            int level = getEnchantmentLevel(bow, CustomEnchantmentType.MULTI_SHOT);
            int extraArrows = (level == 1) ? 2 : 4; // Level 1: 2 extra arrows, Level 2: 4 extra arrows
            
            // Launch additional arrows in a spread pattern
            for (int i = 0; i < extraArrows; i++) {
                Arrow extraArrow = player.launchProjectile(Arrow.class);
                
                // Apply random spread (more spread for more arrows)
                Vector velocity = arrow.getVelocity().clone();
                double spreadFactor = 0.1 * (1 + (i % 2)); // Alternating spread distances
                double angle = Math.PI * 2 * i / extraArrows; // Distribute evenly in a circle
                
                // Calculate the spread vector
                Vector spread = new Vector(
                    Math.cos(angle) * spreadFactor,
                    0.03 * ((i % 2) - 0.5), // Small up/down variation
                    Math.sin(angle) * spreadFactor
                );
                
                extraArrow.setVelocity(velocity.add(spread));
                extraArrow.setShooter(player);
                extraArrow.setCritical(arrow.isCritical());
                extraArrow.setFireTicks(arrow.getFireTicks());
                
                // Copy enchantments to the extra arrow
                markArrowWithEnchantments(extraArrow, bow);
            }
            
            // Add sound and visual effects
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.8f);
            player.getWorld().spawnParticle(Particle.CRIT, arrow.getLocation(), 10, 0.2, 0.2, 0.2, 0.1);
            
            // Set a short cooldown to prevent rapid firing causing server lag
            setCooldown(player, CustomEnchantmentType.MULTI_SHOT);
        }
    }
    
    /**
     * Handles the ProjectileHitEvent for custom bow enchantments.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }
        
        Arrow arrow = (Arrow) event.getEntity();
        
        // Check if this is an enchanted arrow
        if (!arrow.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "enchanted_arrow"), PersistentDataType.BYTE)) {
            return;
        }
        
        // Get the shooter if it's a player
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }
        
        Player shooter = (Player) arrow.getShooter();
        Location hitLocation = arrow.getLocation();
        
        // Handle EXPLOSIVE_ARROW enchantment
        if (hasArrowEnchantment(arrow, CustomEnchantmentType.EXPLOSIVE_ARROW)) {
            int level = getArrowEnchantmentLevel(arrow, CustomEnchantmentType.EXPLOSIVE_ARROW);
            
            // Small explosion that doesn't destroy blocks
            float power = 1.0f + (level * 0.5f); // Level 1: 1.5, Level 2: 2.0, Level 3: 2.5
            
            // Cancel the event to prevent the arrow from sticking
            event.setCancelled(true);
            
            // Remove the arrow
            arrow.remove();
            
            // Create explosion effect without breaking blocks
            hitLocation.getWorld().createExplosion(
                hitLocation.getX(), hitLocation.getY(), hitLocation.getZ(), 
                power, false, false, shooter
            );
            
            // Additional particles for visual effect
            hitLocation.getWorld().spawnParticle(
                Particle.EXPLOSION_LARGE, hitLocation, 1, 0, 0, 0, 0
            );
            hitLocation.getWorld().playSound(hitLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        }
        
        // Handle TELEPORT_ARROW enchantment
        if (hasArrowEnchantment(arrow, CustomEnchantmentType.TELEPORT_ARROW) && 
            !isOnCooldown(shooter, CustomEnchantmentType.TELEPORT_ARROW)) {
            
            // Cancel the event to prevent the arrow from sticking
            event.setCancelled(true);
            
            // Remove the arrow
            arrow.remove();
            
            // Create a safe teleport location (feet position + looking in same direction)
            Location teleportLocation = hitLocation.clone();
            teleportLocation.setYaw(shooter.getLocation().getYaw());
            teleportLocation.setPitch(shooter.getLocation().getPitch());
            
            // Safety check - don't teleport inside blocks
            if (teleportLocation.getBlock().getType().isSolid()) {
                // Find the first non-solid block above the hit location
                for (int y = 0; y < 5; y++) {
                    teleportLocation.add(0, 1, 0);
                    if (!teleportLocation.getBlock().getType().isSolid() && 
                        !teleportLocation.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                        break;
                    }
                }
            }
            
            // Teleport the player
            shooter.teleport(teleportLocation);
            
            // Add visual and sound effects
            shooter.getWorld().spawnParticle(
                Particle.PORTAL, teleportLocation, 50, 0.5, 1.0, 0.5, 0.1
            );
            shooter.getWorld().playSound(teleportLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            
            // Set cooldown
            setCooldown(shooter, CustomEnchantmentType.TELEPORT_ARROW);
        }
        
        // Handle GRAVITY_WELL enchantment
        if (hasArrowEnchantment(arrow, CustomEnchantmentType.GRAVITY_WELL) &&
            !isOnCooldown(shooter, CustomEnchantmentType.GRAVITY_WELL)) {
            
            int level = getArrowEnchantmentLevel(arrow, CustomEnchantmentType.GRAVITY_WELL);
            
            // Cancel the event to prevent the arrow from sticking
            event.setCancelled(true);
            
            // Remove the arrow
            arrow.remove();
            
            // Define the gravity well parameters
            final double radius = 5.0 + (level * 2.0); // Level 1: 7 blocks, Level 2: 9 blocks
            final double strength = 0.3 + (level * 0.2); // Level 1: 0.5, Level 2: 0.7
            final int duration = 5 * 20; // 5 seconds (in ticks)
            final Location wellLocation = hitLocation.clone();
            
            // Create a visual indicator for the gravity well
            shooter.getWorld().spawnParticle(
                Particle.DRAGON_BREATH, wellLocation, 50, 0.5, 0.5, 0.5, 0.05
            );
            shooter.getWorld().playSound(wellLocation, Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 2.0f);
            
            // Schedule the gravity well effect
            new BukkitRunnable() {
                private int ticks = 0;
                
                @Override
                public void run() {
                    ticks++;
                    
                    // Pull entities toward the gravity well
                    for (Entity entity : wellLocation.getWorld().getNearbyEntities(wellLocation, radius, radius, radius)) {
                        if (entity instanceof LivingEntity && entity != shooter) {
                            // Calculate direction and distance to the gravity well
                            Vector pullDirection = wellLocation.toVector().subtract(entity.getLocation().toVector());
                            double distance = pullDirection.length();
                            
                            if (distance > 0.5) { // Don't pull if already very close
                                // Normalize and scale the pull strength (stronger pull at closer range)
                                pullDirection.normalize().multiply(strength * (1 - (distance / radius)));
                                
                                // Apply the pull effect
                                entity.setVelocity(entity.getVelocity().add(pullDirection));
                            }
                        }
                    }
                    
                    // Visual effects every 5 ticks
                    if (ticks % 5 == 0) {
                        wellLocation.getWorld().spawnParticle(
                            Particle.REVERSE_PORTAL, wellLocation, 20, radius/4, radius/4, radius/4, 0.01
                        );
                        
                        if (ticks % 20 == 0) { // Sound every second
                            wellLocation.getWorld().playSound(wellLocation, Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 2.0f);
                        }
                    }
                    
                    // End the effect after the duration
                    if (ticks >= duration) {
                        // Final explosion effect
                        wellLocation.getWorld().spawnParticle(
                            Particle.EXPLOSION_NORMAL, wellLocation, 10, 1.0, 1.0, 1.0, 0.1
                        );
                        wellLocation.getWorld().playSound(wellLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2.0f);
                        
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
            
            // Set cooldown
            setCooldown(shooter, CustomEnchantmentType.GRAVITY_WELL);
        }
        
        // Handle LIGHTNING_STRIKE enchantment
        if (hasArrowEnchantment(arrow, CustomEnchantmentType.LIGHTNING_STRIKE)) {
            int level = getArrowEnchantmentLevel(arrow, CustomEnchantmentType.LIGHTNING_STRIKE);
            
            // Calculate chance based on level (50% at level 1, 75% at level 2) - increased for more consistency
            double chance = 0.50 + ((level - 1) * 0.25);
            
            // Visual effect to show the enchantment is activating
            hitLocation.getWorld().spawnParticle(
                Particle.ELECTRIC_SPARK, hitLocation, 20, 0.5, 0.5, 0.5, 0.1
            );
            
            if (random.nextDouble() < chance) {
                // Number of lightning strikes based on level
                int strikes = level;
                
                // Add visual indicator that lightning is coming
                hitLocation.getWorld().spawnParticle(
                    Particle.WAX_ON, hitLocation, 30, 0.2, 4.0, 0.2, 0.1
                );
                hitLocation.getWorld().playSound(hitLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
                
                // Schedule strikes with a delay between them
                for (int i = 0; i < strikes; i++) {
                    final int strikeIndex = i;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Create a location slightly offset from the hit position
                            Location strikeLocation = hitLocation.clone();
                            if (strikeIndex > 0) {
                                // Less random offset for additional strikes to be more consistent
                                strikeLocation.add(
                                    random.nextDouble() * 2 - 1, // -1 to 1 (smaller offset)
                                    0,
                                    random.nextDouble() * 2 - 1  // -1 to 1 (smaller offset)
                                );
                            }
                            
                            // Strike lightning
                            hitLocation.getWorld().strikeLightning(strikeLocation);
                            
                            // Add more visual effects for the strike
                            hitLocation.getWorld().spawnParticle(
                                Particle.FLASH, strikeLocation, 2, 0.1, 0.1, 0.1, 0.05
                            );
                        }
                    }.runTaskLater(plugin, i * 10L); // 0.5 second between strikes
                }
                
                // Set cooldown to prevent excessive lightning in a short time
                if (!DEFAULT_COOLDOWNS.containsKey(CustomEnchantmentType.LIGHTNING_STRIKE)) {
                    DEFAULT_COOLDOWNS.put(CustomEnchantmentType.LIGHTNING_STRIKE, 2000L); // 2 second cooldown
                }
                setCooldown(shooter, CustomEnchantmentType.LIGHTNING_STRIKE);
            }
        }
    }
}
