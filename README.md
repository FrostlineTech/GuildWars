# GuildWars (v1.1-SNAPSHOT)

> *Forge alliances, claim territory, and wage epic wars in this comprehensive factions plugin for Minecraft servers.*
>
> **Note:** This GitHub repository is for showcase purposes only. The plugin is fully functional on Spigot servers.

## Overview

GuildWars is a powerful factions plugin for Minecraft servers that enables players to create and manage guilds, claim territory, form alliances, and engage in server-wide wars. Built with performance and scalability in mind, GuildWars uses an efficient data storage system to ensure your guild data remains secure and accessible.

## Features

### Performance Optimizations

- **Mob Merging** - Intelligent system that merges nearby similar mobs to reduce entity count
- **Automatic Lag Cleanup** - Configurable system that removes excess entities during lag spikes
- **Enhanced Health Bars** - Custom boss health bar system with formatting and colors

### Custom Mobs

- **Corrupted Warden** - A powerful hostile mob that spawns in plains biomes
  - Summons minions to defend itself
  - Uses EMP pulse to damage nearby players
  - Can teleport to evade attacks
  - Drops special items when defeated

- **Frost Giant** - A dangerous hostile mob that spawns in cold biomes
  - Creates a frost aura that slows nearby players
  - Performs ground pound attacks that damage an area
  - Highly resistant to projectiles
  - Drops rare frost-themed items when defeated

### Guild Management

- **Create & Customize**: Start your own guild with a unique name and tag
- **Hierarchical Roles**: Manage your guild with leaders, officers, and members
- **Invite System**: Grow your guild by inviting other players
- **Guild Home**: Set a central location for your guild members

### Territory Control

- **Land Claiming**: Claim chunks of land for your guild
- **Secure Borders**: Protect your builds and resources from enemies
- **Strategic Expansion**: Claims must be adjacent to existing territory

### Diplomacy & Warfare

- **Alliance System**: Form alliances with other guilds
- **Enemy Declarations**: Mark rival guilds as enemies
- **War Mechanics**: Declare war with customizable durations
- **Server-wide Announcements**: Keep everyone informed of major guild events

### Administration

- **Data Storage**: Efficient file-based storage system
- **Configuration Options**: Customize guild sizes, claim limits, and more
- **Command System**: Comprehensive command set with proper permission handling
- **Custom Aliases**: Configure custom command aliases in the config
- **Placeholders**: Support for guild-related placeholders in chat
- **Tab Completion**: Intelligent tab completion for all commands
- **Admin Tools**: Comprehensive admin commands for server management

## Commands

### Basic Commands

- `/guild` - Display your guild information
- `/guild create <name> <tag>` - Create a new guild
- `/guild join <guild>` - Join a guild that invited you
- `/guild leave` - Leave your current guild
- `/guild info <guild>` - View information about any guild

### Management Commands

- `/guild invite <player>` - Invite a player to your guild
- `/guild kick <player>` - Remove a player from your guild
- `/guild promote <player>` - Promote a member to officer
- `/guild demote <player>` - Demote an officer to member
- `/guild disband` - Permanently delete your guild

### Territory Commands

- `/guild claim` - Claim the chunk you're standing in
- `/guild unclaim` - Unclaim the chunk you're standing in
- `/guild home` - Teleport to your guild's home
- `/guild sethome` - Set your guild's home location

### Custom Enchantments

- **Auto Smelt** - Automatically smelts ores when mined
- **Haste** - Grants mining speed boost
- **Harvester** - Increased drops from crops
- **Treasure Hunter** - Chance to find extra items when mining ores
- **Tunneling** - Break multiple blocks at once

### Diplomacy Commands

- `/guild ally <guild>` - Request an alliance with another guild
- `/guild enemy <guild>` - Mark another guild as an enemy
- `/guild war <guild> [duration]` - Declare war on another guild

### Other Commands

- `/guilds` - List all guilds on the server
- `/guildhelp` - Display help information

### Admin Commands

- `/guildadmin delete <guild>` - Delete a guild (requires permission)
- `/guildadmin reload` - Reload the plugin configuration (requires permission)
- `/guildadmin about` - Display detailed plugin information
- `/guildadmin enchant <type> <level> [player]` - Apply custom enchantment
- `/guildadmin give <item> [player] [amount]` - Give special items
- `/guildadmin summon <mob> [location]` - Summon custom mobs
- `/guildadmin godmode` - Toggle godmode
- `/guildadmin healthbar <on/off>` - Toggle health bars
- `/guildadmin mobmerge <on/off>` - Toggle mob merging
- `/guildadmin clearlag <on/off/now>` - Toggle clear lag or run now
- `/guildadmin mobspawn <on/off/warden/frost> [value]` - Control custom mob spawning

## Technical Details

- **Java Version**: Java 21
- **API**: Paper/Spigot API 1.20.4
- **Build Tool**: Maven

## Installation

1. Place the GuildWars.jar file in your server's plugins folder
2. Start the server to generate the configuration files
3. Configure settings in the config.yml file
4. Restart the server

## Configuration

The plugin uses a combination of config.yml and environment variables for configuration:

```yaml
# Example config.yml
# Command aliases
commands:
  main-command-alias: 'clan'  # Use /clan instead of /guild

# Placeholders
placeholders:
  tags:
    guild: '[G]'
    leader: '[Leader]'
    officer: '[Officer]'

guilds:
  max-members: 20
  max-officers: 5
  
territory:
  max-claims: 10
  require-adjacent: true
  
war:
  min-duration: 30
  max-duration: 120

# Custom Mob Spawning Configuration
custom-mobs:
  natural-spawning: true  # Enable or disable natural spawning of custom mobs
  corrupted-warden:
    spawn-rate: 0.15      # 15% chance to replace natural spawns in plains biomes
    max-spawned: 5        # Maximum number of Corrupted Wardens in the world
    target-players: true  # Target players instead of other mobs
  frost-giant:
    spawn-rate: 0.10      # 10% chance to replace natural spawns in cold biomes
    max-spawned: 3        # Maximum number of Frost Giants in the world
    target-players: true  # Target players instead of other mobs
```

## Credits

Developed by Dakota Fryberger as a portfolio project showcasing Java development skills and Minecraft plugin architecture.

## License

This project is for portfolio demonstration purposes only. While the plugin is fully functional on Spigot servers, this GitHub repository is intended primarily for code showcase.

## Permissions

### Basic Permissions

- `guildwars.create` - Allows creating guilds (default: true)
- `guildwars.join` - Allows joining guilds (default: true)
- `guildwars.admin` - Gives access to all GuildWars commands (default: op)

### Admin Permissions

- `guildwars.admin.delete` - Allows deleting guilds as an administrator (default: op)
- `guildwars.admin.reload` - Allows reloading the plugin configuration (default: op)
- `guildwars.admin.enchant` - Allows applying custom enchantments (default: op)
- `guildwars.admin.give` - Allows giving special items (default: op)
- `guildwars.admin.summon` - Allows summoning custom mobs (default: op)
- `guildwars.admin.godmode` - Allows toggling godmode (default: op)
- `guildwars.admin.healthbar` - Allows toggling health bars (default: op)
- `guildwars.admin.mobmerge` - Allows controlling mob merging (default: op)
- `guildwars.admin.clearlag` - Allows controlling clear lag (default: op)
- `guildwars.admin.mobspawn` - Allows controlling custom mob spawning (default: op)

## Placeholders

The following placeholders are available for use in chat plugins or other integrations:

- `%guild_name%` - The name of the player's guild
- `%guild_tag%` - The guild tag configured in the config
- `%guild_role%` - The player's role tag in their guild (Leader/Officer)
- `%guild_leader%` - The name of the guild's leader

## Support

You can find the plugin on SpigotMC for installation and more information: [https://www.spigotmc.org/resources/guildwars-factions-plugin.124999/](https://www.spigotmc.org/resources/guildwars-factions-plugin.124999/)

Join the Discord server for support and community discussion: [https://discord.gg/FGUEEj6k7k](https://discord.gg/FGUEEj6k7k)