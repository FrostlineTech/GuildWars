package com.guildwars.utils;

import com.guildwars.GuildWars;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages mob merging/stacking to reduce entity count and improve server performance
 */
public class MobMergeManager implements Listener {

    private final GuildWars plugin;
    private final Set<EntityType> excludedTypes;
    private final Map<UUID, Integer> stackedMobs = new HashMap<>();
    private boolean enabled;
    private int maxStackSize;

    /**
     * Creates a new mob merge manager
     *
     * @param plugin The plugin instance
     */
    public MobMergeManager(GuildWars plugin) {
        this.plugin = plugin;
        this.excludedTypes = loadExcludedTypes();
        loadConfig();
        
        // Register events
        if (enabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }
    
    /**
     * Loads configuration settings
     */
    public void loadConfig() {
        enabled = plugin.getConfig().getBoolean("general.performance.mob-merging.enabled", true);
        maxStackSize = plugin.getConfig().getInt("general.performance.mob-merging.max-stack-size", 10);
        
        plugin.getLogger().info("Mob merging enabled: " + enabled);
        plugin.getLogger().info("Max mob stack size: " + maxStackSize);
    }
    
    /**
     * Loads the entity types to exclude from merging
     * 
     * @return Set of excluded entity types
     */
    private Set<EntityType> loadExcludedTypes() {
        List<String> excludedTypeNames = plugin.getConfig().getStringList("general.performance.mob-merging.excluded-types");
        Set<EntityType> types = new HashSet<>();
        
        for (String typeName : excludedTypeNames) {
            try {
                EntityType type = EntityType.valueOf(typeName);
                types.add(type);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type in excluded-types: " + typeName);
            }
        }
        
        return types;
    }
    
    /**
     * Event handler for when creatures spawn
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!enabled || excludedTypes.contains(event.getEntityType())) {
            return;
        }
        
        // Don't merge named entities or entities with custom AI/attributes
        if (event.getEntity().getCustomName() != null) {
            return;
        }
        
        // Attempt to merge with nearby mobs
        tryMergeWithNearby(event.getEntity());
    }
    
    /**
     * Event handler for chunk load to merge existing mobs in newly loaded chunks
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!enabled) {
            return;
        }
        
        // Delay mob merging to ensure chunk is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            mergeEntitiesInChunk(event.getChunk());
        }, 2L);
    }
    
    /**
     * Event handler for entity death to handle stack splitting
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        UUID entityId = entity.getUniqueId();
        
        // Check if this is a stacked entity
        if (stackedMobs.containsKey(entityId)) {
            int stackSize = stackedMobs.get(entityId);
            
            // If there are more entities in the stack, spawn a new one with reduced stack
            if (stackSize > 1) {
                stackedMobs.remove(entityId);
                
                // Spawn a new entity of the same type with one less in stack
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (entity.getLocation().getChunk().isLoaded()) {
                        LivingEntity newEntity = (LivingEntity) entity.getWorld().spawnEntity(
                                entity.getLocation(), entity.getType());
                        
                        // Apply stack size
                        int newStackSize = stackSize - 1;
                        applyStackSize(newEntity, newStackSize);
                    }
                }, 1L);
            } else {
                // Last entity in stack died, remove from map
                stackedMobs.remove(entityId);
            }
        }
    }
    
    /**
     * Applies stack size to an entity and updates its display name
     * 
     * @param entity The entity to update
     * @param stackSize The new stack size
     */
    private void applyStackSize(LivingEntity entity, int stackSize) {
        if (stackSize <= 1) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
            stackedMobs.remove(entity.getUniqueId());
            return;
        }
        
        // Update stack in map
        stackedMobs.put(entity.getUniqueId(), stackSize);
        
        // Create stack display name
        String displayName = ChatColor.GRAY + "x" + stackSize;
        
        // Apply custom name
        entity.setCustomName(displayName);
        entity.setCustomNameVisible(true);
    }
    
    /**
     * Attempts to merge an entity with nearby similar entities
     * 
     * @param entity The entity to merge
     */
    private void tryMergeWithNearby(LivingEntity entity) {
        if (entity == null || entity.isDead()) {
            return;
        }
        
        // Get entities of the same type within 8 blocks
        List<Entity> nearbyEntities = entity.getNearbyEntities(8, 4, 8).stream()
                .filter(e -> e.getType() == entity.getType())
                .filter(e -> e instanceof LivingEntity)
                .filter(e -> !excludedTypes.contains(e.getType()))
                .collect(Collectors.toList());
        
        for (Entity nearby : nearbyEntities) {
            // Check if this entity or the nearby entity is already part of a stack at max size
            int thisStackSize = stackedMobs.getOrDefault(entity.getUniqueId(), 1);
            int nearbyStackSize = stackedMobs.getOrDefault(nearby.getUniqueId(), 1);
            
            // Skip if either stack is at max size
            if (thisStackSize >= maxStackSize || nearbyStackSize >= maxStackSize) {
                continue;
            }
            
            // Calculate new stack size
            int newStackSize = Math.min(thisStackSize + nearbyStackSize, maxStackSize);
            
            // Apply stack to this entity and remove the nearby one
            applyStackSize(entity, newStackSize);
            nearby.remove();
            
            // We've merged once, stop processing
            break;
        }
    }
    
    /**
     * Merges entities of the same type within a specific chunk
     * 
     * @param chunk The chunk to process
     */
    private void mergeEntitiesInChunk(Chunk chunk) {
        // Group entities in the chunk by type
        Map<EntityType, List<Entity>> entitiesByType = new HashMap<>();
        
        for (Entity entity : chunk.getEntities()) {
            // Skip excluded types, players, and named entities
            if (!(entity instanceof LivingEntity) || 
                    excludedTypes.contains(entity.getType()) || 
                    entity instanceof Player || 
                    entity.getCustomName() != null) {
                continue;
            }
            
            entitiesByType.computeIfAbsent(entity.getType(), k -> new ArrayList<>()).add(entity);
        }
        
        // Process each group of entities
        for (Map.Entry<EntityType, List<Entity>> entry : entitiesByType.entrySet()) {
            List<Entity> entities = entry.getValue();
            
            // If only 1 entity of this type, nothing to merge
            if (entities.size() <= 1) {
                continue;
            }
            
            // Sort by oldest first (lower entity ID typically means spawned earlier)
            entities.sort(Comparator.comparing(Entity::getEntityId));
            
            // Merge entities
            Entity primary = entities.get(0);
            int stackSize = 1;
            
            // Add existing stack size if any
            if (stackedMobs.containsKey(primary.getUniqueId())) {
                stackSize = stackedMobs.get(primary.getUniqueId());
            }
            
            // Merge additional entities into the stack
            for (int i = 1; i < entities.size() && stackSize < maxStackSize; i++) {
                Entity current = entities.get(i);
                int currentStackSize = stackedMobs.getOrDefault(current.getUniqueId(), 1);
                
                // Calculate new stack size, bounded by max
                int newStackSize = Math.min(stackSize + currentStackSize, maxStackSize);
                
                // If we can actually merge more
                if (newStackSize > stackSize) {
                    stackSize = newStackSize;
                    current.remove();  // Remove the merged entity
                }
            }
            
            // Apply stack size to the primary entity
            applyStackSize((LivingEntity) primary, stackSize);
        }
    }
    
    /**
     * Process all loaded chunks for mob merging
     * Often useful when first enabling the feature
     */
    public void processLoadedChunks() {
        if (!enabled) {
            return;
        }
        
        plugin.getLogger().info("Processing all loaded chunks for mob merging...");
        
        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                mergeEntitiesInChunk(chunk);
            }
        }
        
        plugin.getLogger().info("Chunk processing complete!");
    }
    
    /**
     * Set whether mob merging is enabled
     * 
     * @param enabled True to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        
        if (enabled) {
            // Register events if not already registered
            try {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
                plugin.getLogger().info("Mob merging enabled");
                
                // Process existing mobs
                processLoadedChunks();
            } catch (IllegalStateException e) {
                // Already registered, ignore
            }
        }
    }
    
    /**
     * Check if mob merging is enabled
     * 
     * @return True if enabled, false if disabled
     */
    public boolean isEnabled() {
        return enabled;
    }
}
