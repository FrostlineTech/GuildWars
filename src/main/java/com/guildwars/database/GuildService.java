package com.guildwars.database;

import com.guildwars.GuildWars;
import com.guildwars.model.ChunkPosition;
import com.guildwars.model.Guild;
import com.guildwars.model.Relation;
import com.guildwars.storage.YamlStorageService;
import org.bukkit.Location;

import java.util.*;

/**
 * Service class for guild-related operations using YAML storage.
 */
public class GuildService {
    private final YamlStorageService storageService;

    public GuildService(GuildWars plugin, YamlStorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Creates a new guild.
     *
     * @param name        The name of the guild
     * @param description The description of the guild
     * @param leader      The UUID of the guild leader
     * @return The created guild, or null if creation failed
     */
    public Guild createGuild(String name, String description, UUID leader) {
        // Check if guild name already exists
        if (storageService.getGuildByName(name) != null) {
            return null;
        }
        
        // Create guild using the Guild constructor
        Guild guild = new Guild(name, leader);
        guild.setDescription(description);
        
        // Add to storage
        return storageService.createGuild(name, description, leader);
    }
    
    /**
     * Gets a guild by its ID.
     *
     * @param guildId The ID of the guild
     * @return The guild, or null if not found
     */
    public Guild getGuild(UUID guildId) {
        return storageService.getGuild(guildId);
    }
    
    /**
     * Gets a guild by its name.
     *
     * @param name The name of the guild
     * @return The guild, or null if not found
     */
    public Guild getGuildByName(String name) {
        return storageService.getGuildByName(name);
    }
    
    /**
     * Gets a guild by a player's UUID.
     *
     * @param playerId The UUID of the player
     * @return The guild, or null if the player is not in a guild
     */
    public Guild getGuildByPlayer(UUID playerId) {
        return storageService.getGuildByPlayer(playerId);
    }
    
    /**
     * Gets all guilds.
     *
     * @return A collection of all guilds
     */
    public Collection<Guild> getAllGuilds() {
        return storageService.getAllGuilds();
    }
    
    /**
     * Deletes a guild.
     *
     * @param guildId The ID of the guild to delete
     * @return True if the guild was deleted, false otherwise
     */
    public boolean deleteGuild(UUID guildId) {
        return storageService.deleteGuild(guildId);
    }
    
    /**
     * Adds a player to a guild.
     *
     * @param guild    The guild
     * @param playerId The UUID of the player
     * @return True if the player was added, false otherwise
     */
    public boolean addPlayerToGuild(Guild guild, UUID playerId) {
        return storageService.addPlayerToGuild(guild, playerId);
    }
    
    /**
     * Removes a player from a guild.
     *
     * @param guild    The guild
     * @param playerId The UUID of the player
     * @return True if the player was removed, false otherwise
     */
    public boolean removePlayerFromGuild(Guild guild, UUID playerId) {
        return storageService.removePlayerFromGuild(guild, playerId);
    }
    
    /**
     * Promotes a player to officer in a guild.
     *
     * @param guild    The guild
     * @param playerId The UUID of the player
     * @return True if the player was promoted, false otherwise
     */
    public boolean promotePlayer(Guild guild, UUID playerId) {
        return storageService.promotePlayer(guild, playerId);
    }
    
    /**
     * Demotes a player from officer in a guild.
     *
     * @param guild    The guild
     * @param playerId The UUID of the player
     * @return True if the player was demoted, false otherwise
     */
    public boolean demotePlayer(Guild guild, UUID playerId) {
        return storageService.demotePlayer(guild, playerId);
    }
    
    /**
     * Sets the leader of a guild.
     *
     * @param guild    The guild
     * @param playerId The UUID of the new leader
     * @return True if the leader was set, false otherwise
     */
    public boolean setGuildLeader(Guild guild, UUID playerId) {
        return storageService.setGuildLeader(guild, playerId);
    }
    
    /**
     * Sets the home location of a guild.
     *
     * @param guild    The guild
     * @param location The home location
     * @return True if the home was set, false otherwise
     */
    public boolean setGuildHome(Guild guild, Location location) {
        return storageService.setGuildHome(guild, location);
    }
    
    /**
     * Claims a chunk for a guild.
     *
     * @param guild    The guild
     * @param position The chunk position
     * @return True if the claim was successful, false otherwise
     */
    public boolean claimChunk(Guild guild, ChunkPosition position) {
        return storageService.claimChunk(guild, position);
    }
    
    /**
     * Unclaims a chunk for a guild.
     *
     * @param guild    The guild
     * @param position The chunk position
     * @return True if the unclaim was successful, false otherwise
     */
    public boolean unclaimChunk(Guild guild, ChunkPosition position) {
        return storageService.unclaimChunk(guild, position);
    }
    
    /**
     * Check if a chunk is claimed by any guild.
     *
     * @param position The chunk position
     * @return True if the chunk is claimed, false otherwise
     */
    public boolean isChunkClaimed(ChunkPosition position) {
        return storageService.isChunkClaimed(position);
    }
    
    /**
     * Get the guild that owns a chunk.
     *
     * @param position The chunk position
     * @return The guild, or null if the chunk is not claimed
     */
    public Guild getChunkOwner(ChunkPosition position) {
        return storageService.getChunkOwner(position);
    }
    
    /**
     * Get the guild ID that owns a chunk.
     *
     * @param position The chunk position
     * @return The guild ID, or null if the chunk is not claimed
     */
    public UUID getChunkOwnerId(ChunkPosition position) {
        return storageService.getChunkOwnerId(position);
    }
    
    /**
     * Check if a chunk is adjacent to a guild's claims.
     *
     * @param guild    The guild
     * @param position The chunk position
     * @return True if the chunk is adjacent to the guild's claims, false otherwise
     */
    public boolean isChunkAdjacentToClaim(Guild guild, ChunkPosition position) {
        return storageService.isChunkAdjacentToClaim(guild, position);
    }
    
    /**
     * Get all claims for a guild.
     *
     * @param guild The guild
     * @return A list of chunk positions claimed by the guild
     */
    public List<ChunkPosition> getGuildClaims(Guild guild) {
        return storageService.getGuildClaims(guild);
    }
    
    /**
     * Count the number of claims for a guild.
     *
     * @param guild The guild
     * @return The number of claims
     */
    public int countGuildClaims(Guild guild) {
        return storageService.countGuildClaims(guild);
    }
    
    /**
     * Set a relation between two guilds.
     *
     * @param guild1   The first guild
     * @param guild2Id The UUID of the second guild
     * @param relation The relation to set
     * @return True if the relation was set, false otherwise
     */
    public boolean setRelation(Guild guild1, UUID guild2Id, Relation relation) {
        UUID guild1Id = UUID.fromString(guild1.getId());
        return storageService.setRelation(guild1Id, guild2Id, relation);
    }
    
    /**
     * Remove a relation between two guilds.
     *
     * @param guild1   The first guild
     * @param guild2Id The UUID of the second guild
     * @return True if the relation was removed, false otherwise
     */
    public boolean removeRelation(Guild guild1, UUID guild2Id) {
        UUID guild1Id = UUID.fromString(guild1.getId());
        return storageService.removeRelation(guild1Id, guild2Id);
    }
    
    /**
     * Get the relation between two guilds.
     *
     * @param guild1Id The UUID of the first guild
     * @param guild2Id The UUID of the second guild
     * @return The relation type, or null if no relation exists
     */
    public Relation getRelation(UUID guild1Id, UUID guild2Id) {
        return storageService.getRelation(guild1Id, guild2Id);
    }
    
    /**
     * Add a member to a guild.
     *
     * @param guildId  The ID of the guild
     * @param playerId The UUID of the player
     * @return True if the player was added, false otherwise
     */
    public boolean addGuildMember(String guildId, UUID playerId) {
        try {
            UUID id = UUID.fromString(guildId);
            Guild guild = getGuild(id);
            if (guild == null) return false;
            return addPlayerToGuild(guild, playerId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Remove a member from a guild.
     *
     * @param guildId  The ID of the guild
     * @param playerId The UUID of the player
     * @return True if the player was removed, false otherwise
     */
    public boolean removeGuildMember(String guildId, UUID playerId) {
        try {
            UUID id = UUID.fromString(guildId);
            Guild guild = getGuild(id);
            if (guild == null) return false;
            return removePlayerFromGuild(guild, playerId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Set the leader of a guild.
     *
     * @param guildId  The ID of the guild
     * @param playerId The UUID of the new leader
     * @return True if the leader was set, false otherwise
     */
    public boolean setGuildLeader(String guildId, UUID playerId) {
        try {
            UUID id = UUID.fromString(guildId);
            Guild guild = getGuild(id);
            if (guild == null) return false;
            return setGuildLeader(guild, playerId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Add an officer to a guild.
     *
     * @param guildId  The ID of the guild
     * @param playerId The UUID of the player
     * @return True if the officer was added, false otherwise
     */
    public boolean addGuildOfficer(String guildId, UUID playerId) {
        try {
            UUID id = UUID.fromString(guildId);
            Guild guild = getGuild(id);
            if (guild == null) return false;
            return promotePlayer(guild, playerId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Remove an officer from a guild.
     *
     * @param guildId  The ID of the guild
     * @param playerId The UUID of the player
     * @return True if the officer was removed, false otherwise
     */
    public boolean removeGuildOfficer(String guildId, UUID playerId) {
        try {
            UUID id = UUID.fromString(guildId);
            Guild guild = getGuild(id);
            if (guild == null) return false;
            return demotePlayer(guild, playerId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Check if a chunk is claimed by a guild.
     *
     * @param position The chunk position
     * @param guildId  The ID of the guild
     * @return True if the chunk is claimed by the guild, false otherwise
     */
    public boolean isChunkClaimed(ChunkPosition position, String guildId) {
        try {
            UUID id = UUID.fromString(guildId);
            UUID ownerId = getChunkOwnerId(position);
            if (ownerId == null) return false;
            return ownerId.equals(id);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Get a guild by its ID.
     *
     * @param guildId The ID of the guild as a string
     * @return The guild, or null if not found
     */
    public Guild getGuildById(String guildId) {
        try {
            UUID id = UUID.fromString(guildId);
            return getGuild(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Check if a chunk is adjacent to a guild's claims.
     *
     * @param position The chunk position
     * @param guildId  The ID of the guild
     * @return True if the chunk is adjacent to the guild's claims, false otherwise
     */
    public boolean isChunkAdjacentToClaim(ChunkPosition position, String guildId) {
        try {
            UUID id = UUID.fromString(guildId);
            Guild guild = getGuild(id);
            if (guild == null) return false;
            return isChunkAdjacentToClaim(guild, position);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Claim a chunk for a guild.
     *
     * @param guildId  The ID of the guild
     * @param position The chunk position
     * @return True if the claim was successful, false otherwise
     */
    public boolean claimChunk(String guildId, ChunkPosition position) {
        try {
            UUID id = UUID.fromString(guildId);
            Guild guild = getGuild(id);
            if (guild == null) return false;
            return claimChunk(guild, position);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Unclaim a chunk for a guild.
     *
     * @param guildId  The ID of the guild
     * @param position The chunk position
     * @return True if the unclaim was successful, false otherwise
     */
    public boolean unclaimChunk(String guildId, ChunkPosition position) {
        try {
            UUID id = UUID.fromString(guildId);
            Guild guild = getGuild(id);
            if (guild == null) return false;
            return unclaimChunk(guild, position);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Set the home location of a guild.
     *
     * @param guildId  The ID of the guild
     * @param location The home location
     * @return True if the home was set, false otherwise
     */
    public boolean setGuildHome(String guildId, Location location) {
        try {
            UUID id = UUID.fromString(guildId);
            Guild guild = getGuild(id);
            if (guild == null) return false;
            return setGuildHome(guild, location);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Check if a player is invited to a guild.
     *
     * @param guildId  The ID of the guild
     * @param playerId The UUID of the player
     * @return True if the player is invited, false otherwise
     */
    public boolean isPlayerInvited(String guildId, UUID playerId) {
        try {
            Guild guild = getGuildById(guildId);
            if (guild == null) return false;
            return guild.isInvited(playerId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Add an invite to a guild for a player.
     *
     * @param guildId  The ID of the guild
     * @param playerId The UUID of the player
     * @return True if the invite was added, false otherwise
     */
    public boolean addGuildInvite(String guildId, UUID playerId) {
        try {
            Guild guild = getGuildById(guildId);
            if (guild == null) return false;
            boolean result = guild.invite(playerId);
            if (result) {
                // Save changes
                storageService.saveAllData();
            }
            return result;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Remove an invite from a guild for a player.
     *
     * @param guildId  The ID of the guild
     * @param playerId The UUID of the player
     * @return True if the invite was removed, false otherwise
     */
    public boolean removeGuildInvite(String guildId, UUID playerId) {
        try {
            Guild guild = getGuildById(guildId);
            if (guild == null) return false;
            boolean result = guild.removeInvite(playerId);
            if (result) {
                // Save changes
                storageService.saveAllData();
            }
            return result;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Set a relation between two guilds using their string IDs.
     *
     * @param guild1Id The ID of the first guild as a string
     * @param guild2Id The ID of the second guild as a string
     * @param relation The relation to set
     * @return True if the relation was set, false otherwise
     */
    public boolean setGuildRelation(String guild1Id, String guild2Id, Relation relation) {
        try {
            UUID id1 = UUID.fromString(guild1Id);
            UUID id2 = UUID.fromString(guild2Id);
            return storageService.setRelation(id1, id2, relation);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
