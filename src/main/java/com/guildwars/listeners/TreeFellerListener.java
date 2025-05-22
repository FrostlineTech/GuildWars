package com.guildwars.listeners;

import com.guildwars.GuildWars;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Listener for the TreeFeller feature.
 * When a player breaks the bottom log of a tree, the entire tree is felled.
 */
public class TreeFellerListener implements Listener {

    private final GuildWars plugin;
    private final Set<Material> LOG_TYPES = new HashSet<>();
    private final Set<Material> LEAF_TYPES = new HashSet<>();
    private final int MAX_BLOCKS = 100; // Safety limit to prevent excessive block breaking

    public TreeFellerListener(GuildWars plugin) {
        this.plugin = plugin;
        
        // Initialize log types
        LOG_TYPES.add(Material.OAK_LOG);
        LOG_TYPES.add(Material.SPRUCE_LOG);
        LOG_TYPES.add(Material.BIRCH_LOG);
        LOG_TYPES.add(Material.JUNGLE_LOG);
        LOG_TYPES.add(Material.ACACIA_LOG);
        LOG_TYPES.add(Material.DARK_OAK_LOG);
        
        // Add stripped logs
        LOG_TYPES.add(Material.STRIPPED_OAK_LOG);
        LOG_TYPES.add(Material.STRIPPED_SPRUCE_LOG);
        LOG_TYPES.add(Material.STRIPPED_BIRCH_LOG);
        LOG_TYPES.add(Material.STRIPPED_JUNGLE_LOG);
        LOG_TYPES.add(Material.STRIPPED_ACACIA_LOG);
        LOG_TYPES.add(Material.STRIPPED_DARK_OAK_LOG);
        
        // Initialize leaf types
        LEAF_TYPES.add(Material.OAK_LEAVES);
        LEAF_TYPES.add(Material.SPRUCE_LEAVES);
        LEAF_TYPES.add(Material.BIRCH_LEAVES);
        LEAF_TYPES.add(Material.JUNGLE_LEAVES);
        LEAF_TYPES.add(Material.ACACIA_LEAVES);
        LEAF_TYPES.add(Material.DARK_OAK_LEAVES);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        // Check if TreeFeller is enabled in config
        if (!plugin.getConfig().getBoolean("treefeller", false)) {
            return;
        }
        
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Check if the broken block is a log
        if (!LOG_TYPES.contains(block.getType())) {
            return;
        }
        
        // Check if the block is part of a tree (has leaves nearby)
        if (!isPartOfTree(block)) {
            return;
        }
        
        // Get all connected logs
        List<Block> logs = new ArrayList<>();
        findConnectedLogs(block, logs, new HashSet<>());
        
        // Break all logs and drop items
        for (Block log : logs) {
            // Skip the original block as it's already being broken by the event
            if (log.equals(block)) {
                continue;
            }
            
            // Break the block and drop items naturally
            log.breakNaturally(player.getInventory().getItemInMainHand());
        }
    }
    
    /**
     * Check if a block is part of a tree by looking for leaves nearby.
     * 
     * @param block The block to check
     * @return True if the block is part of a tree
     */
    private boolean isPartOfTree(Block block) {
        // Check in a 5x5x5 area around the block for leaves
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 4; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block nearby = block.getRelative(x, y, z);
                    if (LEAF_TYPES.contains(nearby.getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Recursively find all connected logs.
     * 
     * @param block The current block
     * @param logs The list of logs found
     * @param checked The set of blocks already checked
     */
    private void findConnectedLogs(Block block, List<Block> logs, Set<Block> checked) {
        // Safety check to prevent excessive recursion
        if (logs.size() >= MAX_BLOCKS || checked.contains(block)) {
            return;
        }
        
        checked.add(block);
        
        // If the block is a log, add it to the list
        if (LOG_TYPES.contains(block.getType())) {
            logs.add(block);
            
            // Check all adjacent blocks
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 1; y++) { // Only check same level and above
                    for (int z = -1; z <= 1; z++) {
                        // Skip the current block
                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }
                        
                        Block adjacent = block.getRelative(x, y, z);
                        findConnectedLogs(adjacent, logs, checked);
                    }
                }
            }
        }
    }
}
