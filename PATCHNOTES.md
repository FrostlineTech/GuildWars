# GuildWars Plugin - Patch Notes

## Version 1.4 - May 22, 2025

### New Features
- **TreeFeller System**: 
  - Added ability to quickly cut down entire trees by breaking the bottom log
  - Can be enabled/disabled in config.yml with `general.treefeller` option
  - Automatically detects tree structures and only works on actual trees

  ### New Admin Features
- **Admin Commands**: 
  - Added `/guildadmin enchant <type> [level]` command for administrators to add custom enchantments to tools
  - Supports tab completion for enchantment types and levels
  - Admins can toggle treefeller in config.yml 
  
  
- **Custom Enchantments System**:
  - Added rare custom enchantments that can be found in structure loot (desert temples, strongholds, etc.)
  - 1 in 1000 chance to get custom enchantments from enchantment tables
  - **Harvester I-III**: For hoes only, harvests crops in a radius (1-3 blocks) around the target crop
  - **Tunneling**: For pickaxes only, mines blocks in a 3x3 area when a block is mined
  

### Improvements
- **Performance Optimization**: 
  - Improved performance of the Tunneling enchantment by optimizing block detection
  - Reduced server load when multiple players are using enchanted tools


### Bug Fixes
- Fixed bug in guild promotion system where leaders could attempt to promote themselves
- Report any bugs at https://discord.gg/FGUEEj6k7k


