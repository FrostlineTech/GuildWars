package com.guildwars.storage;

import com.guildwars.GuildWars;
import com.guildwars.model.ChunkPosition;
import com.guildwars.model.Guild;
import com.guildwars.model.Relation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Service class for guild-related YAML file operations.
 */
public class YamlStorageService {
    private final GuildWars plugin;
    private final File guildsFile;
    private final File claimsFile;
    private final File relationsFile;
    private final File invitesFile;
    
    private FileConfiguration guildsConfig;
    private FileConfiguration claimsConfig;
    private FileConfiguration relationsConfig;
    private FileConfiguration invitesConfig;
    
    // Cache for guilds
    private final Map<UUID, Guild> guildCache = new HashMap<>();
    private final Map<UUID, UUID> playerGuildCache = new HashMap<>();
    private final Map<ChunkPosition, UUID> claimCache = new HashMap<>();
    
    public YamlStorageService(GuildWars plugin) {
        this.plugin = plugin;
        
        // Create data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Initialize files
        guildsFile = new File(plugin.getDataFolder(), "guilds.yml");
        claimsFile = new File(plugin.getDataFolder(), "claims.yml");
        relationsFile = new File(plugin.getDataFolder(), "relations.yml");
        invitesFile = new File(plugin.getDataFolder(), "invites.yml");
        
        // Load configurations
        loadConfigurations();
        
        // Initialize caches
        loadCaches();
    }
    
    /**
     * Load all configuration files.
     */
    private void loadConfigurations() {
        try {
            // Create files if they don't exist
            if (!guildsFile.exists()) guildsFile.createNewFile();
            if (!claimsFile.exists()) claimsFile.createNewFile();
            if (!relationsFile.exists()) relationsFile.createNewFile();
            if (!invitesFile.exists()) invitesFile.createNewFile();
            
            // Load configurations
            guildsConfig = YamlConfiguration.loadConfiguration(guildsFile);
            claimsConfig = YamlConfiguration.loadConfiguration(claimsFile);
            relationsConfig = YamlConfiguration.loadConfiguration(relationsFile);
            invitesConfig = YamlConfiguration.loadConfiguration(invitesFile);
            
            plugin.getLogger().info("YAML configurations loaded successfully.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create YAML files", e);
        }
    }
    
    /**
     * Load all caches from configuration files.
     */
    private void loadCaches() {
        // Clear caches
        guildCache.clear();
        playerGuildCache.clear();
        claimCache.clear();
        
        // Load guilds
        ConfigurationSection guildsSection = guildsConfig.getConfigurationSection("guilds");
        if (guildsSection != null) {
            for (String guildIdStr : guildsSection.getKeys(false)) {
                UUID guildId = UUID.fromString(guildIdStr);
                ConfigurationSection guildSection = guildsSection.getConfigurationSection(guildIdStr);
                
                if (guildSection != null) {
                    String name = guildSection.getString("name");
                    String description = guildSection.getString("description", "");
                    UUID leaderId = UUID.fromString(guildSection.getString("leader"));
                    
                    // Create guild using the available constructor
                    Guild guild = new Guild(name, leaderId);
                    guild.setDescription(description);
                    
                    // Load members
                    List<String> memberList = guildSection.getStringList("members");
                    for (String memberIdStr : memberList) {
                        UUID memberId = UUID.fromString(memberIdStr);
                        guild.addMember(memberId);
                    }
                    
                    // Load officers
                    List<String> officerList = guildSection.getStringList("officers");
                    for (String officerIdStr : officerList) {
                        UUID officerId = UUID.fromString(officerIdStr);
                        guild.addOfficer(officerId);
                    }
                    
                    // Load home
                    if (guildSection.contains("home")) {
                        ConfigurationSection homeSection = guildSection.getConfigurationSection("home");
                        if (homeSection != null) {
                            String worldName = homeSection.getString("world");
                            double x = homeSection.getDouble("x");
                            double y = homeSection.getDouble("y");
                            double z = homeSection.getDouble("z");
                            float yaw = (float) homeSection.getDouble("yaw");
                            float pitch = (float) homeSection.getDouble("pitch");
                            
                            Location home = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                            guild.setHome(home);
                        }
                    }
                    
                    // Add to cache
                    guildCache.put(guildId, guild);
                    
                    // Add members to player-guild cache
                    for (UUID memberId : guild.getMembers()) {
                        playerGuildCache.put(memberId, guildId);
                    }
                }
            }
        }
        
        // Load claims
        ConfigurationSection claimsSection = claimsConfig.getConfigurationSection("claims");
        if (claimsSection != null) {
            for (String worldName : claimsSection.getKeys(false)) {
                ConfigurationSection worldSection = claimsSection.getConfigurationSection(worldName);
                if (worldSection != null) {
                    for (String chunkKey : worldSection.getKeys(false)) {
                        String[] coords = chunkKey.split(",");
                        if (coords.length == 2) {
                            try {
                                int x = Integer.parseInt(coords[0]);
                                int z = Integer.parseInt(coords[1]);
                                String guildIdStr = worldSection.getString(chunkKey);
                                
                                if (guildIdStr != null) {
                                    UUID guildId = UUID.fromString(guildIdStr);
                                    ChunkPosition position = new ChunkPosition(worldName, x, z);
                                    
                                    // Add to claim cache
                                    claimCache.put(position, guildId);
                                    
                                    // Add to guild claims
                                    Guild guild = guildCache.get(guildId);
                                    if (guild != null) {
                                        guild.claim(position);
                                    }
                                }
                            } catch (NumberFormatException e) {
                                plugin.getLogger().warning("Invalid chunk coordinates: " + chunkKey);
                            }
                        }
                    }
                }
            }
        }
        
        // Load relations
        ConfigurationSection relationsSection = relationsConfig.getConfigurationSection("relations");
        if (relationsSection != null) {
            for (String guild1IdStr : relationsSection.getKeys(false)) {
                ConfigurationSection guild1Section = relationsSection.getConfigurationSection(guild1IdStr);
                if (guild1Section != null) {
                    UUID guild1Id = UUID.fromString(guild1IdStr);
                    Guild guild1 = guildCache.get(guild1Id);
                    
                    if (guild1 != null) {
                        for (String guild2IdStr : guild1Section.getKeys(false)) {
                            String relationStr = guild1Section.getString(guild2IdStr);
                            if (relationStr != null) {
                                try {
                                    Relation relation = Relation.valueOf(relationStr);
                                    guild1.setRelation(guild2IdStr, relation);
                                } catch (IllegalArgumentException e) {
                                    plugin.getLogger().warning("Invalid relation type: " + relationStr);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Load invites
        ConfigurationSection invitesSection = invitesConfig.getConfigurationSection("invites");
        if (invitesSection != null) {
            for (String guildIdStr : invitesSection.getKeys(false)) {
                UUID guildId = UUID.fromString(guildIdStr);
                Guild guild = guildCache.get(guildId);
                
                if (guild != null) {
                    List<String> inviteList = invitesSection.getStringList(guildIdStr);
                    for (String playerIdStr : inviteList) {
                        UUID playerId = UUID.fromString(playerIdStr);
                        guild.invite(playerId);
                    }
                }
            }
        }
    }
    
    /**
     * Save all data to configuration files.
     */
    private void saveData() {
        // Save guilds
        guildsConfig.set("guilds", null);
        for (Map.Entry<UUID, Guild> entry : guildCache.entrySet()) {
            UUID guildId = entry.getKey();
            Guild guild = entry.getValue();
            
            String path = "guilds." + guildId.toString();
            
            // Save basic info
            guildsConfig.set(path + ".name", guild.getName());
            guildsConfig.set(path + ".description", guild.getDescription());
            guildsConfig.set(path + ".leader", guild.getLeader().toString());
            
            // Save members
            List<String> memberList = new ArrayList<>();
            for (UUID memberId : guild.getMembers()) {
                memberList.add(memberId.toString());
            }
            guildsConfig.set(path + ".members", memberList);
            
            // Save officers
            List<String> officerList = new ArrayList<>();
            for (UUID officerId : guild.getOfficers()) {
                officerList.add(officerId.toString());
            }
            guildsConfig.set(path + ".officers", officerList);
            
            // Save home
            Location home = guild.getHome();
            if (home != null) {
                guildsConfig.set(path + ".home.world", home.getWorld().getName());
                guildsConfig.set(path + ".home.x", home.getX());
                guildsConfig.set(path + ".home.y", home.getY());
                guildsConfig.set(path + ".home.z", home.getZ());
                guildsConfig.set(path + ".home.yaw", home.getYaw());
                guildsConfig.set(path + ".home.pitch", home.getPitch());
            }
        }
        
        // Save claims
        claimsConfig.set("claims", null);
        for (Map.Entry<ChunkPosition, UUID> entry : claimCache.entrySet()) {
            ChunkPosition position = entry.getKey();
            UUID guildId = entry.getValue();
            
            String path = "claims." + position.getWorld() + "." + position.getX() + "," + position.getZ();
            claimsConfig.set(path, guildId.toString());
        }
        
        // Save relations
        relationsConfig.set("relations", null);
        for (Map.Entry<UUID, Guild> entry : guildCache.entrySet()) {
            UUID guildId = entry.getKey();
            Guild guild = entry.getValue();
            
            Map<String, Relation> relations = guild.getRelations();
            if (!relations.isEmpty()) {
                for (Map.Entry<String, Relation> relationEntry : relations.entrySet()) {
                    String targetGuildId = relationEntry.getKey();
                    Relation relation = relationEntry.getValue();
                    
                    String path = "relations." + guildId.toString() + "." + targetGuildId;
                    relationsConfig.set(path, relation.name());
                }
            }
        }
        
        // Save invites
        invitesConfig.set("invites", null);
        for (Map.Entry<UUID, Guild> entry : guildCache.entrySet()) {
            UUID guildId = entry.getKey();
            Guild guild = entry.getValue();
            
            Set<UUID> invites = guild.getInvites();
            if (!invites.isEmpty()) {
                List<String> inviteList = new ArrayList<>();
                for (UUID playerId : invites) {
                    inviteList.add(playerId.toString());
                }
                invitesConfig.set("invites." + guildId.toString(), inviteList);
            }
        }
        
        // Save configurations
        try {
            guildsConfig.save(guildsFile);
            claimsConfig.save(claimsFile);
            relationsConfig.save(relationsFile);
            invitesConfig.save(invitesFile);
            
            plugin.getLogger().info("YAML data saved successfully.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save YAML files", e);
        }
    }
    
    /**
     * Public method to save all data to configuration files.
     */
    public void saveAllData() {
        saveData();
    }
    
    /**
     * Create a new guild.
     *
     * @param name        The name of the guild
     * @param description The description of the guild
     * @param leader      The UUID of the guild leader
     * @return The created guild
     */
    public Guild createGuild(String name, String description, UUID leader) {
        // Check if guild name already exists
        if (getGuildByName(name) != null) {
            return null;
        }
        
        // Create new guild using the available constructor
        Guild guild = new Guild(name, leader);
        guild.setDescription(description);
        
        // The leader is already added as a member in the Guild constructor
        
        // Add to caches
        UUID guildId = UUID.fromString(guild.getId());
        guildCache.put(guildId, guild);
        playerGuildCache.put(leader, guildId);
        
        // Save data
        saveData();
        
        return guild;
    }
    
    /**
     * Get a guild by its UUID.
     *
     * @param guildId The UUID of the guild
     * @return The guild, or null if not found
     */
    public Guild getGuild(UUID guildId) {
        return guildCache.get(guildId);
    }
    
    /**
     * Get a guild by its name.
     *
     * @param name The name of the guild
     * @return The guild, or null if not found
     */
    public Guild getGuildByName(String name) {
        for (Guild guild : guildCache.values()) {
            if (guild.getName().equalsIgnoreCase(name)) {
                return guild;
            }
        }
        return null;
    }
    
    /**
     * Get a guild by a player's UUID.
     *
     * @param playerId The UUID of the player
     * @return The guild, or null if the player is not in a guild
     */
    public Guild getGuildByPlayer(UUID playerId) {
        UUID guildId = playerGuildCache.get(playerId);
        if (guildId == null) {
            return null;
        }
        return guildCache.get(guildId);
    }
    
    /**
     * Get all guilds.
     *
     * @return A collection of all guilds
     */
    public Collection<Guild> getAllGuilds() {
        return guildCache.values();
    }
    
    /**
     * Delete a guild.
     *
     * @param guildId The UUID of the guild to delete
     * @return True if the guild was deleted, false otherwise
     */
    public boolean deleteGuild(UUID guildId) {
        Guild guild = guildCache.remove(guildId);
        if (guild == null) {
            return false;
        }
        
        // Remove all members from player-guild cache
        for (UUID memberId : guild.getMembers()) {
            playerGuildCache.remove(memberId);
        }
        
        // Remove all claims from claim cache
        for (ChunkPosition claim : guild.getClaims()) {
            claimCache.remove(claim);
        }
        
        // Save data
        saveData();
        
        return true;
    }
    
    /**
     * Add a player to a guild.
     *
     * @param guild    The guild
     * @param playerId The UUID of the player
     * @return True if the player was added, false otherwise
     */
    public boolean addPlayerToGuild(Guild guild, UUID playerId) {
        // Check if player is already in a guild
        if (playerGuildCache.containsKey(playerId)) {
            return false;
        }
        
        // Add player to guild
        guild.addMember(playerId);
        
        // Add to player-guild cache
        playerGuildCache.put(playerId, UUID.fromString(guild.getId()));
        
        // Save data
        saveData();
        
        return true;
    }
    
    /**
     * Remove a player from a guild.
     *
     * @param guild    The guild
     * @param playerId The UUID of the player
     * @return True if the player was removed, false otherwise
     */
    public boolean removePlayerFromGuild(Guild guild, UUID playerId) {
        // Check if player is in the guild
        if (!guild.isMember(playerId)) {
            return false;
        }
        
        // Remove player from guild
        guild.removeMember(playerId);
        
        // Remove from player-guild cache
        playerGuildCache.remove(playerId);
        
        // Save data
        saveData();
        
        return true;
    }
    
    /**
     * Promote a player to officer in a guild.
     *
     * @param guild    The guild
     * @param playerId The UUID of the player
     * @return True if the player was promoted, false otherwise
     */
    public boolean promotePlayer(Guild guild, UUID playerId) {
        // Check if player is in the guild and not already an officer
        if (!guild.isMember(playerId) || guild.isOfficer(playerId)) {
            return false;
        }
        
        // Promote player
        guild.addOfficer(playerId);
        
        // Save data
        saveData();
        
        return true;
    }
    
    /**
     * Demote a player from officer in a guild.
     *
     * @param guild    The guild
     * @param playerId The UUID of the player
     * @return True if the player was demoted, false otherwise
     */
    public boolean demotePlayer(Guild guild, UUID playerId) {
        // Check if player is an officer
        if (!guild.isOfficer(playerId)) {
            return false;
        }
        
        // Demote player
        guild.removeOfficer(playerId);
        
        // Save data
        saveData();
        
        return true;
    }
    
    /**
     * Set the leader of a guild.
     *
     * @param guild    The guild
     * @param playerId The UUID of the new leader
     * @return True if the leader was set, false otherwise
     */
    public boolean setGuildLeader(Guild guild, UUID playerId) {
        // Check if player is in the guild
        if (!guild.isMember(playerId)) {
            return false;
        }
        
        // Set leader
        guild.setLeader(playerId);
        
        // Save data
        saveData();
        
        return true;
    }
    
    /**
     * Set the home location of a guild.
     *
     * @param guild    The guild
     * @param location The home location
     * @return True if the home was set, false otherwise
     */
    public boolean setGuildHome(Guild guild, Location location) {
        // Set home
        guild.setHome(location);
        
        // Save data
        saveData();
        
        return true;
    }
    
    /**
     * Check if a chunk is claimed by any guild.
     *
     * @param position The chunk position
     * @return True if the chunk is claimed, false otherwise
     */
    public boolean isChunkClaimed(ChunkPosition position) {
        return claimCache.containsKey(position);
    }
    
    /**
     * Get the guild that owns a chunk.
     *
     * @param position The chunk position
     * @return The guild, or null if the chunk is not claimed
     */
    public Guild getChunkOwner(ChunkPosition position) {
        UUID guildId = claimCache.get(position);
        if (guildId == null) {
            return null;
        }
        return guildCache.get(guildId);
    }
    
    /**
     * Get the guild ID that owns a chunk.
     *
     * @param position The chunk position
     * @return The guild ID, or null if the chunk is not claimed
     */
    public UUID getChunkOwnerId(ChunkPosition position) {
        return claimCache.get(position);
    }
    
    /**
     * Check if a chunk is adjacent to a guild's claims.
     *
     * @param guild    The guild
     * @param position The chunk position
     * @return True if the chunk is adjacent to the guild's claims, false otherwise
     */
    public boolean isChunkAdjacentToClaim(Guild guild, ChunkPosition position) {
        // Check adjacent chunks
        int x = position.getX();
        int z = position.getZ();
        String world = position.getWorld();
        
        ChunkPosition[] adjacent = {
            new ChunkPosition(world, x + 1, z),
            new ChunkPosition(world, x - 1, z),
            new ChunkPosition(world, x, z + 1),
            new ChunkPosition(world, x, z - 1)
        };
        
        for (ChunkPosition adj : adjacent) {
            UUID owner = claimCache.get(adj);
            if (owner != null && owner.equals(UUID.fromString(guild.getId()))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all claims for a guild.
     *
     * @param guild The guild
     * @return A list of chunk positions claimed by the guild
     */
    public List<ChunkPosition> getGuildClaims(Guild guild) {
        UUID guildId = UUID.fromString(guild.getId());
        return claimCache.entrySet().stream()
                .filter(entry -> entry.getValue().equals(guildId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Count the number of claims for a guild.
     *
     * @param guild The guild
     * @return The number of claims
     */
    public int countGuildClaims(Guild guild) {
        return getGuildClaims(guild).size();
    }
    
    /**
     * Claim a chunk for a guild.
     *
     * @param guild    The guild
     * @param position The chunk position
     * @return True if the claim was successful, false otherwise
     */
    public boolean claimChunk(Guild guild, ChunkPosition position) {
        // Check if chunk is already claimed
        if (claimCache.containsKey(position)) {
            return false;
        }
        
        // Add to guild claims
        if (!guild.claim(position)) {
            return false;
        }
        
        // Add to claim cache
        claimCache.put(position, UUID.fromString(guild.getId()));
        
        // Save data
        saveData();
        
        return true;
    }
    
    /**
     * Unclaim a chunk for a guild.
     *
     * @param guild    The guild
     * @param position The chunk position
     * @return True if the unclaim was successful, false otherwise
     */
    public boolean unclaimChunk(Guild guild, ChunkPosition position) {
        // Check if chunk is claimed by this guild
        UUID owner = claimCache.get(position);
        if (owner == null || !owner.equals(UUID.fromString(guild.getId()))) {
            return false;
        }
        
        // Remove from guild claims
        if (!guild.unclaim(position)) {
            return false;
        }
        
        // Remove from claim cache
        claimCache.remove(position);
        
        // Save data
        saveData();
        
        return true;
    }
    
    /**
     * Set the relation between two guilds.
     *
     * @param guild1Id The ID of the first guild
     * @param guild2Id The ID of the second guild
     * @param relation The relation to set
     * @return True if the relation was set, false otherwise
     */
    public boolean setRelation(UUID guild1Id, UUID guild2Id, Relation relation) {
        Guild guild1 = guildCache.get(guild1Id);
        Guild guild2 = guildCache.get(guild2Id);
        
        if (guild1 == null || guild2 == null) {
            return false;
        }
        
        // Set relation
        guild1.setRelation(guild2.getId(), relation);
        guild2.setRelation(guild1.getId(), relation);
        
        // Save data
        saveData();
        
        return true;
    }
    
    /**
     * Remove the relation between two guilds.
     *
     * @param guild1Id The ID of the first guild
     * @param guild2Id The ID of the second guild
     * @return True if the relation was removed, false otherwise
     */
    public boolean removeRelation(UUID guild1Id, UUID guild2Id) {
        Guild guild1 = guildCache.get(guild1Id);
        Guild guild2 = guildCache.get(guild2Id);
        
        if (guild1 == null || guild2 == null) {
            return false;
        }
        
        // Remove relation
        guild1.setRelation(guild2.getId(), Relation.NEUTRAL);
        guild2.setRelation(guild1.getId(), Relation.NEUTRAL);
        
        // Save data
        saveData();
        
        return true;
    }
    
    /**
     * Get the relation between two guilds.
     *
     * @param guild1Id The UUID of the first guild
     * @param guild2Id The UUID of the second guild
     * @return The relation type, or null if no relation exists
     */
    public Relation getRelation(UUID guild1Id, UUID guild2Id) {
        Guild guild1 = guildCache.get(guild1Id);
        Guild guild2 = guildCache.get(guild2Id);
        
        if (guild1 == null || guild2 == null) {
            return null;
        }
        
        return guild1.getRelation(guild2.getId());
    }
}
