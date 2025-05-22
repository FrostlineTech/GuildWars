package com.guildwars.mobs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * A debug mob used for testing custom mob functionality.
 * This mob can only be spawned by admins using the /gadmin summon Debug command.
 */
public class DebugMob {
    
    private final JavaPlugin plugin;
    
    /**
     * Creates a new DebugMob instance.
     *
     * @param plugin The plugin instance
     */
    public DebugMob(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Spawns a Debug mob at the specified location.
     *
     * @param location The location to spawn the mob
     * @return The spawned entity
     */
    public LivingEntity spawn(Location location) {
        // Create a zombie as the base entity
        Zombie entity = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        
        // Set custom name
        entity.setCustomName(ChatColor.RED + "Debug Mob");
        entity.setCustomNameVisible(true);
        
        // Make it not burn in daylight
        // Using setBaby(false) to ensure it's an adult zombie and setting fire resistance
        entity.setBaby(false);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        
        // Set attributes
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
        entity.setHealth(100.0);
        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(20.0);
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
        
        // Equip with Netherite armor
        if (entity.getEquipment() != null) {
            // Create armor items
            ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
            ItemStack chestplate = new ItemStack(Material.NETHERITE_CHESTPLATE);
            ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
            ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
            ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
            ItemStack shield = new ItemStack(Material.SHIELD);
            
            // Apply armor
            entity.getEquipment().setHelmet(helmet);
            entity.getEquipment().setChestplate(chestplate);
            entity.getEquipment().setLeggings(leggings);
            entity.getEquipment().setBoots(boots);
            
            // Equip with Netherite sword and shield
            entity.getEquipment().setItemInMainHand(sword);
            entity.getEquipment().setItemInOffHand(shield);
        }
        
        // Set equipment drop chances to 0 so the items don't drop when the mob dies
        if (entity.getEquipment() != null) {
            entity.getEquipment().setHelmetDropChance(0.0f);
            entity.getEquipment().setChestplateDropChance(0.0f);
            entity.getEquipment().setLeggingsDropChance(0.0f);
            entity.getEquipment().setBootsDropChance(0.0f);
            entity.getEquipment().setItemInMainHandDropChance(0.0f);
            entity.getEquipment().setItemInOffHandDropChance(0.0f);
        }
        
        // Add potion effects
        entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
        
        // Store custom data to identify this as a debug mob
        entity.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, "debug_mob"),
            PersistentDataType.BYTE,
            (byte) 1
        );
        
        return entity;
    }
    
    /**
     * Checks if an entity is a Debug mob.
     *
     * @param entity The entity to check
     * @return True if the entity is a Debug mob
     */
    public static boolean isDebugMob(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        return entity.getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey(JavaPlugin.getProvidingPlugin(DebugMob.class), "debug_mob"),
            PersistentDataType.BYTE
        );
    }
}
