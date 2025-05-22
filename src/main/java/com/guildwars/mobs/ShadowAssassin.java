package com.guildwars.mobs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * A Shadow Assassin custom mob.
 * This mob teleports behind players when attacked and applies potion effects on hit.
 */
public class ShadowAssassin implements Listener {
    
    private final JavaPlugin plugin;
    private final Random random = new Random();
    
    /**
     * Creates a new ShadowAssassin instance.
     *
     * @param plugin The plugin instance
     */
    public ShadowAssassin(JavaPlugin plugin) {
        this.plugin = plugin;
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Spawns a Shadow Assassin at the specified location.
     *
     * @param location The location to spawn the mob
     * @return The spawned entity
     */
    public LivingEntity spawn(Location location) {
        // Create a Stray as the base entity (skeleton variant with icy appearance)
        Stray entity = (Stray) location.getWorld().spawnEntity(location, EntityType.STRAY);
        
        // Set custom name
        entity.setCustomName(ChatColor.DARK_PURPLE + "Shadow Assassin");
        entity.setCustomNameVisible(true);
        
        // Set attributes
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(80.0);
        entity.setHealth(80.0);
        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10.0);
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.4);
        
        // Equip with enchanted sword and armor
        if (entity.getEquipment() != null) {
            // Create enchanted sword
            ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
            ItemMeta swordMeta = sword.getItemMeta();
            swordMeta.addEnchant(Enchantment.DAMAGE_ALL, 5, true);
            swordMeta.setDisplayName(ChatColor.DARK_PURPLE + "Assassin's Blade");
            sword.setItemMeta(swordMeta);
            
            // Create black leather armor
            ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
            ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
            ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            
            // Apply armor
            entity.getEquipment().setHelmet(helmet);
            entity.getEquipment().setChestplate(chestplate);
            entity.getEquipment().setLeggings(leggings);
            entity.getEquipment().setBoots(boots);
            
            // Equip with sword
            entity.getEquipment().setItemInMainHand(sword);
            
            // Set equipment drop chances to very low
            entity.getEquipment().setHelmetDropChance(0.05f);
            entity.getEquipment().setChestplateDropChance(0.05f);
            entity.getEquipment().setLeggingsDropChance(0.05f);
            entity.getEquipment().setBootsDropChance(0.05f);
            entity.getEquipment().setItemInMainHandDropChance(0.05f);
        }
        
        // Apply permanent invisibility in low light
        entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        
        // Store custom data to identify this as a shadow assassin
        entity.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, "shadow_assassin"),
            PersistentDataType.BYTE,
            (byte) 1
        );
        
        // Create shadow particle effect
        new BukkitRunnable() {
            @Override
            public void run() {
                if (entity == null || entity.isDead()) {
                    this.cancel();
                    return;
                }
                
                // Create shadow particles around the entity
                entity.getWorld().spawnParticle(
                    Particle.SMOKE_NORMAL, 
                    entity.getLocation().add(0, 0.5, 0), 
                    5, 0.2, 0.5, 0.2, 0.01
                );
            }
        }.runTaskTimer(plugin, 0L, 10L);
        
        return entity;
    }
    
    /**
     * Handles entities being damaged to implement the teleport-behind ability.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if a Shadow Assassin is being damaged
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        
        if (!isShadowAssassin(entity)) {
            return;
        }
        
        // Check if the damage was caused by a player
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        
        // 30% chance to teleport behind the player
        if (random.nextDouble() < 0.3) {
            // Calculate position behind the player
            Location playerLoc = player.getLocation();
            double yaw = Math.toRadians(playerLoc.getYaw());
            double x = -Math.sin(yaw);
            double z = Math.cos(yaw);
            
            Location behindLoc = playerLoc.clone().subtract(x * 2, 0, z * 2);
            
            // Teleport the assassin
            entity.teleport(behindLoc);
            
            // Play effect
            entity.getWorld().spawnParticle(
                Particle.PORTAL, 
                playerLoc, 
                20, 0.5, 0.5, 0.5, 0.5
            );
            entity.getWorld().playSound(playerLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
            
            // Notify the player
            player.sendMessage(ChatColor.DARK_PURPLE + "The Shadow Assassin vanishes from sight!");
            
            // Cancel the damage
            event.setCancelled(true);
        }
    }
    
    /**
     * Handles the Shadow Assassin attacking players to apply potion effects.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        // Check if a player is being damaged by a Shadow Assassin
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        if (!(event.getDamager() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity damager = (LivingEntity) event.getDamager();
        
        if (!isShadowAssassin(damager)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // 50% chance to apply poison
        if (random.nextDouble() < 0.5) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
            player.sendMessage(ChatColor.DARK_GREEN + "The Shadow Assassin's blade was poisoned!");
        }
        
        // 30% chance to apply weakness
        if (random.nextDouble() < 0.3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
            player.sendMessage(ChatColor.GRAY + "You feel your strength draining away...");
        }
        
        // Create particle effect
        player.getWorld().spawnParticle(
            Particle.CRIT, 
            player.getLocation().add(0, 1, 0), 
            10, 0.5, 0.5, 0.5, 0.2
        );
    }
    
    /**
     * Checks if an entity is a Shadow Assassin.
     *
     * @param entity The entity to check
     * @return True if the entity is a Shadow Assassin
     */
    public static boolean isShadowAssassin(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        return entity.getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey(JavaPlugin.getProvidingPlugin(ShadowAssassin.class), "shadow_assassin"),
            PersistentDataType.BYTE
        );
    }
}
