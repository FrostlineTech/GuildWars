# GuildWars Custom Mobs Guide

This guide explains how to configure and use the custom mob system in the GuildWars plugin.

## Available Custom Mobs

### Corrupted Warden

A powerful hostile mob that spawns in plains biomes.

**Features:**

- Summons minions to defend itself
- Uses EMP pulse to damage nearby players
- Can teleport to evade attacks
- Drops special items when defeated

### Frost Giant

A dangerous hostile mob that spawns in cold biomes.

**Features:**

- Creates a frost aura that slows nearby players
- Performs ground pound attacks that damage an area
- Highly resistant to projectiles
- Drops rare frost-themed items when defeated

## Configuration

Custom mob settings are controlled in the `config.yml` file under the `general.custom-mobs` section:

```yaml
# Custom mob settings
custom-mobs:
  # Whether natural spawning of custom mobs is enabled
  natural-spawning: true
  
  # Corrupted Warden settings
  corrupted-warden:
    # Chance to replace natural mob spawns in plains biomes (0.0-1.0)
    spawn-rate: 0.15
    
    # Maximum number of Corrupted Wardens allowed in the world at once
    max-spawned: 5
    
    # Whether Corrupted Wardens should target players
    target-players: true
  
  # Frost Giant settings
  frost-giant:
    # Chance to replace natural mob spawns in cold biomes (0.0-1.0)
    spawn-rate: 0.10
    
    # Maximum number of Frost Giants allowed in the world at once
    max-spawned: 3
    
    # Whether Frost Giants should target players
    target-players: true
```

## Admin Commands

The following commands are available to control custom mob spawning:

- `/guildadmin mobspawn on` - Enable natural spawning of custom mobs
- `/guildadmin mobspawn off` - Disable natural spawning
- `/guildadmin mobspawn warden <rate>` - Set Corrupted Warden spawn rate (0.01-1.0)
- `/guildadmin mobspawn frost <rate>` - Set Frost Giant spawn rate (0.01-1.0)
- `/guildadmin summon corruptedwarden` - Summon a Corrupted Warden at your location
- `/guildadmin summon frostgiant` - Summon a Frost Giant at your location

## Permissions

- `guildwars.admin.mobspawn` - Allows controlling custom mob spawning
- `guildwars.admin.summon` - Allows summoning custom mobs

## Biome-Specific Spawning

- **Corrupted Wardens** spawn naturally in plains biomes (replacing zombies, skeletons, or creepers)
- **Frost Giants** spawn naturally in cold biomes like snowy plains, ice spikes, and frozen ocean (replacing zombies, skeletons, or creepers)

## Technical Notes

- Custom mobs will not exceed their maximum spawn limits
- Spawn rates are a percentage chance to replace natural spawns of standard hostile mobs
- Both mobs are based on Iron Golem entities with custom attributes and behaviors
- Custom mobs will target the nearest player on spawn
- Changes to spawn rates take effect immediately without requiring a server restart
