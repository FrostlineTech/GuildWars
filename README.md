# GuildWars

> *Forge alliances, claim territory, and wage epic wars in this comprehensive factions plugin for Minecraft servers.*
>
> **Note:** This GitHub repository is for showcase purposes only. The plugin is fully functional on Spigot servers.

## Overview

GuildWars is a powerful factions plugin for Minecraft servers that enables players to create and manage guilds, claim territory, form alliances, and engage in server-wide wars. Built with performance and scalability in mind, GuildWars uses an efficient data storage system to ensure your guild data remains secure and accessible.

## Features

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

### Diplomacy Commands
- `/guild ally <guild>` - Request an alliance with another guild
- `/guild enemy <guild>` - Mark another guild as an enemy
- `/guild war <guild> [duration]` - Declare war on another guild

### Other Commands
- `/guilds` - List all guilds on the server
- `/guildhelp` - Display help information

## Technical Details

- **Java Version**: Java 17+
- **API**: Paper/Spigot API
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
guilds:
  max-members: 20
  max-officers: 5
  
territory:
  max-claims: 10
  require-adjacent: true
  
war:
  min-duration: 30
  max-duration: 120
```

## Credits

Developed by Dakota Fryberger as a portfolio project showcasing Java development skills and Minecraft plugin architecture.

## License

This project is for portfolio demonstration purposes only. While the plugin is fully functional on Spigot servers, this GitHub repository is intended primarily for code showcase.


## you can find the plugin here for instalattion and more information
https://www.spigotmc.org/resources/guildwars-factions-plugin.124999/

## join the discord!! 
https://discord.gg/FGUEEj6k7k