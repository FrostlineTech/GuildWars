package com.guildwars.model;

import org.bukkit.Location;

import java.util.*;

/**
 * Represents a guild in the GuildWars plugin.
 */
public class Guild {
    private String id;
    private String name;
    private String tag;
    private UUID leader;
    private Set<UUID> officers;
    private Set<UUID> members;
    private Set<UUID> invites;
    private Location home;
    private Set<ChunkPosition> claims;
    private Map<String, Relation> relations;
    private Date creationDate;
    private int level;
    private double balance;
    private String description;
    private String motd;

    /**
     * Creates a new guild.
     *
     * @param name The name of the guild
     * @param leader The UUID of the guild leader
     */
    public Guild(String name, UUID leader) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.tag = name.substring(0, Math.min(name.length(), 4)).toUpperCase();
        this.leader = leader;
        this.officers = new HashSet<>();
        this.members = new HashSet<>();
        this.members.add(leader); // Leader is also a member
        this.invites = new HashSet<>();
        this.claims = new HashSet<>();
        this.relations = new HashMap<>();
        this.creationDate = new Date();
        this.level = 1;
        this.balance = 0;
    }

    /**
     * Gets the unique ID of the guild.
     *
     * @return The guild ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the name of the guild.
     *
     * @return The guild name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the guild.
     *
     * @param name The new guild name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the tag of the guild.
     *
     * @return The guild tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Sets the tag of the guild.
     *
     * @param tag The new guild tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Gets the UUID of the guild leader.
     *
     * @return The leader's UUID
     */
    public UUID getLeader() {
        return leader;
    }

    /**
     * Sets the leader of the guild.
     *
     * @param leader The UUID of the new leader
     */
    public void setLeader(UUID leader) {
        this.leader = leader;
        if (!members.contains(leader)) {
            members.add(leader);
        }
    }

    /**
     * Gets the set of officer UUIDs.
     *
     * @return The set of officers
     */
    public Set<UUID> getOfficers() {
        return Collections.unmodifiableSet(officers);
    }

    /**
     * Adds an officer to the guild.
     *
     * @param player The UUID of the player to add as an officer
     * @return True if the player was added as an officer, false if they were already an officer
     */
    public boolean addOfficer(UUID player) {
        if (!members.contains(player)) {
            members.add(player);
        }
        return officers.add(player);
    }

    /**
     * Removes an officer from the guild.
     *
     * @param player The UUID of the player to remove as an officer
     * @return True if the player was removed as an officer, false if they were not an officer
     */
    public boolean removeOfficer(UUID player) {
        return officers.remove(player);
    }

    /**
     * Gets the set of member UUIDs.
     *
     * @return The set of members
     */
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Adds a member to the guild.
     *
     * @param player The UUID of the player to add as a member
     * @return True if the player was added as a member, false if they were already a member
     */
    public boolean addMember(UUID player) {
        invites.remove(player); // Remove from invites if they were invited
        return members.add(player);
    }

    /**
     * Removes a member from the guild.
     *
     * @param player The UUID of the player to remove as a member
     * @return True if the player was removed as a member, false if they were not a member
     */
    public boolean removeMember(UUID player) {
        if (player.equals(leader)) {
            return false; // Can't remove the leader
        }
        officers.remove(player); // Remove from officers if they were an officer
        return members.remove(player);
    }

    /**
     * Checks if a player is a member of the guild.
     *
     * @param player The UUID of the player to check
     * @return True if the player is a member, false otherwise
     */
    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    /**
     * Checks if a player is an officer of the guild.
     *
     * @param player The UUID of the player to check
     * @return True if the player is an officer, false otherwise
     */
    public boolean isOfficer(UUID player) {
        return officers.contains(player);
    }

    /**
     * Checks if a player is the leader of the guild.
     *
     * @param player The UUID of the player to check
     * @return True if the player is the leader, false otherwise
     */
    public boolean isLeader(UUID player) {
        return leader.equals(player);
    }

    /**
     * Gets the set of invited player UUIDs.
     *
     * @return The set of invited players
     */
    public Set<UUID> getInvites() {
        return Collections.unmodifiableSet(invites);
    }

    /**
     * Invites a player to the guild.
     *
     * @param player The UUID of the player to invite
     * @return True if the player was invited, false if they were already invited
     */
    public boolean invite(UUID player) {
        if (members.contains(player)) {
            return false; // Already a member
        }
        return invites.add(player);
    }

    /**
     * Removes an invite for a player.
     *
     * @param player The UUID of the player to remove the invite for
     * @return True if the invite was removed, false if the player was not invited
     */
    public boolean removeInvite(UUID player) {
        return invites.remove(player);
    }

    /**
     * Checks if a player is invited to the guild.
     *
     * @param player The UUID of the player to check
     * @return True if the player is invited, false otherwise
     */
    public boolean isInvited(UUID player) {
        return invites.contains(player);
    }

    /**
     * Gets the home location of the guild.
     *
     * @return The guild home location
     */
    public Location getHome() {
        return home;
    }

    /**
     * Sets the home location of the guild.
     *
     * @param home The new guild home location
     */
    public void setHome(Location home) {
        this.home = home;
    }

    /**
     * Gets the set of claimed chunks.
     *
     * @return The set of claimed chunks
     */
    public Set<ChunkPosition> getClaims() {
        return Collections.unmodifiableSet(claims);
    }

    /**
     * Claims a chunk for the guild.
     *
     * @param chunk The chunk position to claim
     * @return True if the chunk was claimed, false if it was already claimed
     */
    public boolean claim(ChunkPosition chunk) {
        return claims.add(chunk);
    }

    /**
     * Unclaims a chunk from the guild.
     *
     * @param chunk The chunk position to unclaim
     * @return True if the chunk was unclaimed, false if it was not claimed
     */
    public boolean unclaim(ChunkPosition chunk) {
        return claims.remove(chunk);
    }

    /**
     * Checks if a chunk is claimed by the guild.
     *
     * @param chunk The chunk position to check
     * @return True if the chunk is claimed by the guild, false otherwise
     */
    public boolean isClaimed(ChunkPosition chunk) {
        return claims.contains(chunk);
    }

    /**
     * Gets the relations with other guilds.
     *
     * @return The map of guild IDs to relations
     */
    public Map<String, Relation> getRelations() {
        return Collections.unmodifiableMap(relations);
    }

    /**
     * Sets the relation with another guild.
     *
     * @param guildId The ID of the other guild
     * @param relation The relation to set
     */
    public void setRelation(String guildId, Relation relation) {
        relations.put(guildId, relation);
    }

    /**
     * Gets the relation with another guild.
     *
     * @param guildId The ID of the other guild
     * @return The relation with the other guild, or NEUTRAL if no relation is set
     */
    public Relation getRelation(String guildId) {
        return relations.getOrDefault(guildId, Relation.NEUTRAL);
    }

    /**
     * Gets the creation date of the guild.
     *
     * @return The creation date
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Gets the level of the guild.
     *
     * @return The guild level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the level of the guild.
     *
     * @param level The new guild level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Gets the balance of the guild.
     *
     * @return The guild balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Sets the balance of the guild.
     *
     * @param balance The new guild balance
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Gets the description of the guild.
     *
     * @return The guild description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the guild.
     *
     * @param description The new guild description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the message of the day of the guild.
     *
     * @return The guild message of the day
     */
    public String getMotd() {
        return motd;
    }

    /**
     * Sets the message of the day of the guild.
     *
     * @param motd The new guild message of the day
     */
    public void setMotd(String motd) {
        this.motd = motd;
    }

    /**
     * Gets the total number of members in the guild.
     *
     * @return The number of members
     */
    public int getMemberCount() {
        return members.size();
    }

    /**
     * Gets the total number of claims by the guild.
     *
     * @return The number of claims
     */
    public int getClaimCount() {
        return claims.size();
    }
}
