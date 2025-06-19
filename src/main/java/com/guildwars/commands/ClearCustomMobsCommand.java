package com.guildwars.commands;

import com.guildwars.GuildWars;
import com.guildwars.mobs.CorruptedWarden;
import com.guildwars.mobs.FrostGiant;
import com.guildwars.mobs.ShadowAssassin;
import com.guildwars.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command to clear all custom mobs from the server or world.
 */
public class ClearCustomMobsCommand implements CommandExecutor, TabCompleter {

    private final GuildWars plugin;

    /**
     * Constructor for the clear custom mobs command.
     *
     * @param plugin The main plugin instance
     */
    public ClearCustomMobsCommand(GuildWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("guildwars.admin.clearmobs")) {
            MessageUtil.sendError(sender, "You don't have permission to use this command.");
            return true;
        }

        // Check if we have a scope argument (all, world)
        String scope = "all";
        if (args.length > 0) {
            scope = args[0].toLowerCase();
        }

        // Default to all worlds
        List<World> worlds = new ArrayList<>(Bukkit.getWorlds());
        
        // If scope is "world", restrict to sender's world
        if (scope.equals("world") && sender instanceof Player) {
            worlds.clear();
            worlds.add(((Player) sender).getWorld());
        }
        
        // For tracking numbers of mobs removed
        int removedCount = removeCustomMobs(worlds);
        
        // Send success message
        if (scope.equals("world")) {
            MessageUtil.sendSuccess(sender, "Removed " + removedCount + " custom mobs from this world.");
        } else {
            MessageUtil.sendSuccess(sender, "Removed " + removedCount + " custom mobs from all worlds.");
        }

        return true;
    }

    /**
     * Removes all custom mobs from the given worlds.
     *
     * @param worlds The worlds to clear mobs from
     * @return The number of mobs removed
     */
    private int removeCustomMobs(List<World> worlds) {
        int count = 0;

        for (World world : worlds) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    
                    // Check if it's a custom mob from our plugin
                    if (isCustomMob(livingEntity)) {
                        entity.remove();
                        count++;
                    }
                }
            }
        }

        // Reset tracked counts after removing all mobs
        if (plugin.getCustomMobSpawnManager() != null) {
            // Since we can't directly reset the counters, we'll use reflection 
            // or wait for the count checker task to update them
        }

        return count;
    }

    /**
     * Checks if an entity is a custom mob from our plugin.
     *
     * @param entity The entity to check
     * @return True if it's a custom mob
     */
    private boolean isCustomMob(LivingEntity entity) {
        // Check various custom mob types
        if (CorruptedWarden.isCorruptedWarden(entity)) {
            return true;
        }
        
        if (FrostGiant.isFrostGiant(entity)) {
            return true;
        }
        
        if (ShadowAssassin.isShadowAssassin(entity)) {
            return true;
        }
        
        // Check for minions
        if (entity instanceof Zombie) {
            org.bukkit.NamespacedKey minionKey = new org.bukkit.NamespacedKey(plugin, "warden_minion");
            return entity.getPersistentDataContainer().has(minionKey, PersistentDataType.BYTE);
        }
        
        // Add other custom mob types as needed
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = Arrays.asList("all", "world");
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], options, completions);
            return completions;
        }
        return Collections.emptyList();
    }
}
