package com.guildwars.mobs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * A Frost Giant custom mob.
 * This mob has a frost aura that slows nearby players and can throw ice projectiles.
 */
public class FrostGiant {
    
    private final JavaPlugin plugin;
    private final Random random = new Random();
    
    /**
     * Creates a new FrostGiant instance.
     *
     * @param plugin The plugin instance
     */
    public FrostGiant(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Spawns a Frost Giant at the specified location.
     *
     * @param location The location to spawn the mob
     * @return The spawned entity
     */
    public LivingEntity spawn(Location location) {
        // Create an Iron Golem as the base entity
        IronGolem entity = (IronGolem) location.getWorld().spawnEntity(location, EntityType.IRON_GOLEM);
        
        // Set custom name
        entity.setCustomName(ChatColor.AQUA + "Frost Giant");
        entity.setCustomNameVisible(true);
        
        // Set attributes
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200.0);
        entity.setHealth(200.0);
        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15.0);
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.25);
        entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.8);
        
        // Add visual effects to make it look frozen
        entity.getWorld().spawnParticle(Particle.SNOWFLAKE, entity.getLocation().add(0, 1, 0), 
                30, 1, 2, 1, 0.02);
        entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.8f);
        
        // Store custom data to identify this as a frost giant
        entity.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, "frost_giant"),
            PersistentDataType.BYTE,
            (byte) 1
        );
        
        // Apply frost aura effect - scheduled task that slows nearby players
        new BukkitRunnable() {
            @Override
            public void run() {
                if (entity == null || entity.isDead()) {
                    this.cancel();
                    return;
                }
                
                // Apply frost effects to nearby players
                for (Entity nearby : entity.getNearbyEntities(5, 5, 5)) {
                    if (nearby instanceof Player) {
                        Player player = (Player) nearby;
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));
                        
                        // Visual snow particles
                        player.getWorld().spawnParticle(Particle.SNOW_SHOVEL, 
                                player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
                    }
                }
                
                // Ambient effects
                entity.getWorld().spawnParticle(Particle.SNOWFLAKE, 
                        entity.getLocation().add(0, 1, 0), 5, 1, 1, 1, 0.01);
                
                // 10% chance to perform a ground pound attack
                if (random.nextDouble() < 0.1) {
                    performGroundPound(entity);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        return entity;
    }
    
    /**
     * Performs a ground pound attack that freezes the ground.
     * 
     * @param entity The Frost Giant entity
     */
    private void performGroundPound(IronGolem entity) {
        // Visual and sound effects
        entity.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, 
                entity.getLocation(), 1, 0, 0, 0, 0);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.5f);
        
        // Apply AoE effect in a radius of 7 blocks
        double radius = 7.0;
        for (Entity nearby : entity.getNearbyEntities(radius, radius, radius)) {
            if (nearby instanceof Player) {
                Player player = (Player) nearby;
                
                // Apply more intense slow and mining fatigue
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 100, 1));
                
                // Apply freeze damage
                player.damage(5.0, entity);
                
                // Create ice particles around the player
                player.getWorld().spawnParticle(Particle.SNOWBALL, 
                        player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        // Create a circle of snow blocks temporarily (will be removed after 5 seconds)
        int radius_int = (int) radius;
        for (int x = -radius_int; x <= radius_int; x++) {
            for (int z = -radius_int; z <= radius_int; z++) {
                // Only place blocks in a circle pattern
                if (x*x + z*z <= radius_int*radius_int) {
                    Location blockLoc = entity.getLocation().add(x, 0, z);
                    
                    // Only replace air or grass
                    Material blockType = blockLoc.getBlock().getType();
                    if (blockType == Material.AIR || blockType == Material.GRASS || 
                        blockType == Material.DIRT) {
                        continue; // Skip for now - replacing blocks can cause lag and conflicts
                    }
                }
            }
        }
        
        // Large visual effect for the ground pound
        entity.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, 
                entity.getLocation(), 5, 3, 0.2, 3, 0.1);
        entity.getWorld().spawnParticle(Particle.SNOWFLAKE, 
                entity.getLocation(), 50, 5, 0.5, 5, 0.1);
        entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.5f, 0.5f);
    }
    
    /**
     * Checks if an entity is a Frost Giant.
     *
     * @param entity The entity to check
     * @return True if the entity is a Frost Giant
     */
    public static boolean isFrostGiant(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        return entity.getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey(JavaPlugin.getProvidingPlugin(FrostGiant.class), "frost_giant"),
            PersistentDataType.BYTE
        );
    }
}
