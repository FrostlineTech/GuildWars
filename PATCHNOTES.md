# GuildWars Plugin - Patch Notes

## Version 1.3 - May 15, 2025

### New Features
- **Guild Chat System**: 
  - Added `/gchat` command (aliases: `/gc`, `/guildchat`) for guild-only communication
  - Configurable chat format with guild name, role, and player name
  - Role-specific colors (Leader: red, Officer: blue, Member: green)

- **Customizable Terminology**:
  - Added ability to rename "guilds" to other terms like "factions", "clans", etc.
  - New configuration option: `general.group-term` in config.yml
  - Messages automatically update to use the custom term

- **Enhanced Player Experience**:
  - Guild information displayed when players join the server
  - Role-specific tags shown in chat

### Improvements
- **Minecraft Version Support**:
  - Added compatibility with Minecraft versions 1.17 through 1.21.5
  - Updated API dependencies to support a wider range of servers

- **Security Enhancements**:
  - Improved .gitignore to better protect sensitive configuration files

## Version 1.2 - Previous Release

- Initial public release with core guild functionality
- Guild management system (create, join, leave, etc.)
- Territory claiming and protection
- Diplomacy system (alliances, enemies, wars)
- Basic permission system
