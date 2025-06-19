package com.guildwars.mobs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * A Corrupted Warden custom mob.
 * This mob is a powerful, corrupted Iron Golem that has an EMP pulse that blinds players,
 * can teleport when at low health, and summons helper mobs when damaged.
 */
public class CorruptedWarden implements Listener {
    
    private final JavaPlugin plugin;
    private final Random random = new Random();
    private final String MINION_TAG = "warden_minion";
    
    /**
     * Creates a new CorruptedWarden instance.
     *
     * @param plugin The plugin instance
     */
    public CorruptedWarden(JavaPlugin plugin) {
        this.plugin = plugin;
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Spawns a Corrupted Warden at the specified location.
     *
     * @param location The location to spawn the mob
     * @return The spawned entity
     */
    public LivingEntity spawn(Location location) {
        // Create an Iron Golem as the base entity
        IronGolem entity = (IronGolem) location.getWorld().spawnEntity(location, EntityType.IRON_GOLEM);
        
        // Set entity to target players only using Bukkit NMS to override targeting goals
        entity.setPlayerCreated(false); // Ensure it's not treated as a player-created golem
        
        // Set custom name
        entity.setCustomName(ChatColor.DARK_RED + "Corrupted Warden");
        entity.setCustomNameVisible(true);
        
        // Set attributes
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(300.0);
        entity.setHealth(300.0);
        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(25.0);
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35);
        entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
        entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40.0); // Increase detection range
        
        // Force target nearest player when spawned
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (Player player : entity.getWorld().getPlayers()) {
            double distance = player.getLocation().distanceSquared(entity.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }
        
        if (nearestPlayer != null && nearestDistance < 1600) { // 40 blocks squared
            entity.setTarget(nearestPlayer);
        }
        
        // Add visual effects to show corruption
        entity.getWorld().spawnParticle(
            Particle.FLAME, 
            entity.getLocation().add(0, 1, 0), 
            50, 1, 2, 1, 0.02
        );
        entity.getWorld().playSound(
            entity.getLocation(), 
            Sound.ENTITY_IRON_GOLEM_HURT, 
            1.0f, 0.5f
        );
        
        // Store custom data to identify this as a corrupted warden
        entity.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, "corrupted_warden"),
            PersistentDataType.BYTE,
            (byte) 1
        );
        
        // Schedule periodic EMP pulse
        new BukkitRunnable() {
            @Override
            public void run() {
                if (entity == null || entity.isDead()) {
                    this.cancel();
                    return;
                }
                
                // 25% chance to trigger an EMP pulse
                if (random.nextDouble() < 0.25) {
                    performEmpPulse(entity);
                }
            }
        }.runTaskTimer(plugin, 200L, 200L); // Every 10 seconds with initial delay
        
        return entity;
    }
    
    /**
     * Performs an EMP pulse that blinds nearby players.
     * 
     * @param entity The Corrupted Warden entity
     */
    private void performEmpPulse(IronGolem entity) {
        // Visual and sound effects
        entity.getWorld().spawnParticle(
            Particle.EXPLOSION_LARGE, 
            entity.getLocation().add(0, 1, 0), 
            1, 0, 0, 0, 0
        );
        entity.getWorld().playSound(
            entity.getLocation(), 
            Sound.ENTITY_GENERIC_EXPLODE, 
            1.5f, 0.8f
        );
        
        // Affect players in a 15 block radius
        for (Entity nearby : entity.getNearbyEntities(15, 15, 15)) {
            if (nearby instanceof Player) {
                Player player = (Player) nearby;
                
                // Apply blindness and slowness effects
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 1));
                
                // Notify the player
                player.sendMessage(ChatColor.DARK_RED + "The Corrupted Warden emits a blinding pulse!");
                
                // Play sound at player's location
                player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 1.0f);
            }
        }
        
        // Dramatic visual effects
        entity.getWorld().spawnParticle(
            Particle.ELECTRIC_SPARK, 
            entity.getLocation(), 
            100, 8, 8, 8, 0.1
        );
    }
    
    /**
     * Handles the Corrupted Warden being damaged.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof IronGolem)) {
            return;
        }
        
        IronGolem warden = (IronGolem) event.getEntity();
        
        if (!isCorruptedWarden(warden)) {
            return;
        }
        
        // Check if health is low (below 30%)
        if (warden.getHealth() - event.getFinalDamage() < warden.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 0.3) {
            // 40% chance to teleport when health is low
            if (random.nextDouble() < 0.4) {
                teleportAwayFromDamage(warden);
                
                // Cancel the damage
                event.setCancelled(true);
                return;
            }
        }
        
        // 20% chance to summon helper mobs when damaged
        if (random.nextDouble() < 0.2) {
            summonHelperMobs(warden);
        }
    }
    
    /**
     * Teleports the Corrupted Warden away from damage.
     * 
     * @param warden The Corrupted Warden entity
     */
    private void teleportAwayFromDamage(IronGolem warden) {
        // Find a safe location 10-20 blocks away
        Location currentLoc = warden.getLocation();
        Location newLoc = currentLoc.clone();
        
        // Try to find a safe location by checking in random directions
        for (int i = 0; i < 10; i++) { // Try up to 10 times
            double distance = 10 + random.nextDouble() * 10; // 10-20 blocks
            double angle = random.nextDouble() * Math.PI * 2; // Random angle
            
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;
            
            newLoc = currentLoc.clone().add(x, 0, z);
            
            // Find a safe Y position
            newLoc.setY(currentLoc.getY());
            for (int y = -5; y <= 5; y++) {
                Location checkLoc = newLoc.clone().add(0, y, 0);
                if (!checkLoc.getBlock().getType().isSolid() && 
                    !checkLoc.clone().add(0, 1, 0).getBlock().getType().isSolid() && 
                    checkLoc.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                    newLoc = checkLoc;
                    break;
                }
            }
            
            // If we found a good location, break out
            if (!newLoc.getBlock().getType().isSolid() && 
                !newLoc.clone().add(0, 1, 0).getBlock().getType().isSolid() && 
                newLoc.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                break;
            }
        }
        
        // Create teleport effects
        warden.getWorld().spawnParticle(
            Particle.PORTAL, 
            warden.getLocation(), 
            50, 0.5, 1, 0.5, 0.5
        );
        warden.getWorld().playSound(
            warden.getLocation(), 
            Sound.ENTITY_ENDERMAN_TELEPORT, 
            1.0f, 0.5f
        );
        
        // Teleport
        warden.teleport(newLoc);
        
        // Heal a bit after teleporting
        double maxHealth = warden.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        warden.setHealth(Math.min(warden.getHealth() + (maxHealth * 0.1), maxHealth));
        
        // Arrival effects
        warden.getWorld().spawnParticle(
            Particle.PORTAL, 
            newLoc, 
            50, 0.5, 1, 0.5, 0.5
        );
        warden.getWorld().playSound(
            newLoc, 
            Sound.ENTITY_ENDERMAN_TELEPORT, 
            1.0f, 0.5f
        );
        
        // Notify nearby players
        for (Entity nearby : warden.getNearbyEntities(30, 30, 30)) {
            if (nearby instanceof Player) {
                Player player = (Player) nearby;
                player.sendMessage(ChatColor.DARK_RED + "The Corrupted Warden teleports away!");
            }
        }
    }
    
    /**
     * Summons helper mobs around the Corrupted Warden.
     * 
     * @param warden The Corrupted Warden entity
     */
    private void summonHelperMobs(IronGolem warden) {
        // Number of mobs to summon (2-3)
        int count = 2 + random.nextInt(2);
        
        // Summon effect
        warden.getWorld().spawnParticle(
            Particle.SOUL, 
            warden.getLocation(), 
            50, 3, 0.5, 3, 0.1
        );
        warden.getWorld().playSound(
            warden.getLocation(), 
            Sound.ENTITY_WITHER_SPAWN, 
            1.0f, 1.5f
        );
        
        // Summon zombies with enhanced abilities
        for (int i = 0; i < count; i++) {
            // Calculate spawn position (random within 3 blocks)
            Location spawnLoc = warden.getLocation().clone().add(
                random.nextDouble() * 6 - 3,
                0,
                random.nextDouble() * 6 - 3
            );
            
            // Summon a zombie
            Zombie zombie = (Zombie) warden.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            
            // Enhance the zombie
            zombie.setCustomName(ChatColor.RED + "Warden's Minion");
            zombie.setCustomNameVisible(true);
            
            zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
            zombie.setHealth(40.0);
            zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(8.0);
            zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
            
            // Mark this zombie as a minion of the Corrupted Warden
            zombie.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, MINION_TAG),
                PersistentDataType.BYTE,
                (byte) 1
            );
            
            // Add glow effect and prevent burning in daylight
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
            
            // Spawn effect
            zombie.getWorld().spawnParticle(
                Particle.SOUL, 
                zombie.getLocation(), 
                20, 0.5, 1, 0.5, 0.1
            );
            
            // Schedule for despawn after 30 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!zombie.isDead()) {
                        zombie.getWorld().spawnParticle(
                            Particle.SOUL, 
                            zombie.getLocation(), 
                            20, 0.5, 1, 0.5, 0.1
                        );
                        zombie.remove();
                    }
                }
            }.runTaskLater(plugin, 30 * 20L);
        }
        
        // Notify nearby players
        for (Entity nearby : warden.getNearbyEntities(20, 20, 20)) {
            if (nearby instanceof Player) {
                Player player = (Player) nearby;
                player.sendMessage(ChatColor.DARK_RED + "The Corrupted Warden summons minions to its aid!");
            }
        }
    }
    
    /**
     * Handles entity targeting to ensure proper targeting behavior:
     * - Prevents Corrupted Wardens from targeting their own minions
     * - Ensures Iron Golems target custom mobs
     * 
     * @param event The targeting event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        Entity entity = event.getEntity();
        LivingEntity target = event.getTarget();
        
        if (entity instanceof IronGolem) {
            // If this is a Corrupted Warden
            if (isCorruptedWarden((LivingEntity) entity)) {
                // If the target is one of the warden's minions, cancel the targeting
                if (target instanceof Zombie && isWardenMinion(target)) {
                    event.setCancelled(true);
                }
            } 
            // If this is a regular Iron Golem (not a Corrupted Warden)
            else if (target != null && isCorruptedWarden(target)) {
                // Make sure Golems are hostile to Corrupted Wardens
                // The targeting is preserved (not cancelled) and priority increased
                if (event.getReason() == EntityTargetLivingEntityEvent.TargetReason.CLOSEST_ENTITY) {
                    // Boost targeting priority when golem notices a Corrupted Warden
                    ((IronGolem) entity).setTarget(target);
                }
            }
        }
    }
    
    /**
     * Prevents damage between Corrupted Wardens and their minions.
     * 
     * @param event The damage event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Prevent Corrupted Warden from damaging its minions
        if (event.getDamager() instanceof LivingEntity && event.getEntity() instanceof LivingEntity) {
            LivingEntity damager = (LivingEntity) event.getDamager();
            LivingEntity victim = (LivingEntity) event.getEntity();
            
            // Warden attacking minion scenario
            if (isCorruptedWarden(damager) && isWardenMinion(victim)) {
                event.setCancelled(true);
            }
            
            // Minion attacking warden scenario
            if (isWardenMinion(damager) && isCorruptedWarden(victim)) {
                event.setCancelled(true);
            }
            
            // Minion attacking another minion scenario
            if (isWardenMinion(damager) && isWardenMinion(victim)) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Checks if an entity is a Warden's minion.
     * 
     * @param entity The entity to check
     * @return True if the entity is a Warden's minion
     */
    public boolean isWardenMinion(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        return entity.getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey(plugin, MINION_TAG),
            PersistentDataType.BYTE
        );
    }
    
    /**
     * Prevents wolves from following players too close to Corrupted Wardens.
     * When a tamed wolf is within 100 blocks of a warden, it will refuse to follow
     * the player any closer to the warden.
     * 
     * @param event The player move event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check significant movements to reduce performance impact
        if (event.getFrom().distanceSquared(event.getTo()) < 0.3) {
            return;
        }
        
        Player player = event.getPlayer();
        Location playerLoc = player.getLocation();
        
        // Check if there's a Corrupted Warden within 100 blocks
        for (Entity entity : player.getNearbyEntities(100, 100, 100)) {
            if (entity instanceof LivingEntity && isCorruptedWarden((LivingEntity) entity)) {
                Location wardenLoc = entity.getLocation();
                double distanceToWarden = playerLoc.distanceSquared(wardenLoc);
                
                // Now check for tamed wolves that belong to this player
                for (Entity nearbyEntity : player.getNearbyEntities(20, 10, 20)) {
                    if (nearbyEntity instanceof Wolf) {
                        Wolf wolf = (Wolf) nearbyEntity;
                        
                        // Only affect tamed wolves that belong to this player
                        if (wolf.isTamed() && player.equals(wolf.getOwner())) {
                            Location wolfLoc = wolf.getLocation();
                            double wolfDistanceToWarden = wolfLoc.distanceSquared(wardenLoc);
                            
                            // If the wolf is getting closer to the warden, stop it
                            if (wolfDistanceToWarden < 10000) { // 100 blocks squared
                                // If the player is moving closer to the warden
                                // and the wolf would be following, stop the wolf
                                if (distanceToWarden < wolfDistanceToWarden) {
                                    // Make wolf sit to prevent it from following
                                    if (!wolf.isSitting()) {
                                        wolf.setSitting(true);
                                        player.sendMessage(ChatColor.RED + "Your wolf refuses to approach the Corrupted Warden!");
                                        
                                        // Visual effect to show wolf's fear
                                        wolf.getWorld().spawnParticle(
                                            Particle.SMOKE_NORMAL,
                                            wolf.getLocation().add(0, 0.7, 0),
                                            10, 0.2, 0.2, 0.2, 0.02
                                        );
                                    }
                                } else if (distanceToWarden > 10000 && wolf.isSitting()) {
                                    // Allow wolf to follow again when far enough away
                                    wolf.setSitting(false);
                                }
                            }
                        }
                    }
                }
                
                // We only need to check for the first Corrupted Warden found
                break;
            }
        }
    }
    
    /**
     * Checks if an entity is a Corrupted Warden.
     *
     * @param entity The entity to check
     * @return True if the entity is a Corrupted Warden
     */
    public static boolean isCorruptedWarden(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        return entity.getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey(JavaPlugin.getProvidingPlugin(CorruptedWarden.class), "corrupted_warden"),
            PersistentDataType.BYTE
        );
    }
}
