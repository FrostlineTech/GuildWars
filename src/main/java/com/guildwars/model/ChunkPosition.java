package com.guildwars.model;

import org.bukkit.Chunk;

import java.util.Objects;

/**
 * Represents a chunk position in the world.
 */
public class ChunkPosition {
    private final String world;
    private final int x;
    private final int z;

    /**
     * Creates a new chunk position.
     *
     * @param world The name of the world
     * @param x The x coordinate of the chunk
     * @param z The z coordinate of the chunk
     */
    public ChunkPosition(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    /**
     * Creates a new chunk position from a Bukkit chunk.
     *
     * @param chunk The Bukkit chunk
     */
    public ChunkPosition(Chunk chunk) {
        this.world = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    /**
     * Gets the name of the world.
     *
     * @return The world name
     */
    public String getWorld() {
        return world;
    }

    /**
     * Gets the x coordinate of the chunk.
     *
     * @return The x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the z coordinate of the chunk.
     *
     * @return The z coordinate
     */
    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPosition that = (ChunkPosition) o;
        return x == that.x && z == that.z && world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

    @Override
    public String toString() {
        return world + "," + x + "," + z;
    }

    /**
     * Creates a chunk position from a string.
     *
     * @param str The string representation of the chunk position
     * @return The chunk position
     */
    public static ChunkPosition fromString(String str) {
        String[] parts = str.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid chunk position string: " + str);
        }
        return new ChunkPosition(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
}
