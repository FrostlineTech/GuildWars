package com.guildwars.model;

/**
 * Represents the relation between two guilds.
 */
public enum Relation {
    /**
     * Guilds are allied and cannot harm each other.
     */
    ALLY,
    
    /**
     * Guilds are neutral towards each other.
     */
    NEUTRAL,
    
    /**
     * Guilds are enemies and can engage in combat.
     */
    ENEMY,
    
    /**
     * Guilds are at war with each other.
     */
    WAR;
}
