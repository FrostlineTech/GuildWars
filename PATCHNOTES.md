# GuildWars Plugin - Patch Notes

## Version 1.4.0 - May 23, 2025

### New Features
- **Combat Visual Enhancements**:
  - **Damage Counters**: Shows floating damage numbers above entities when hit
    - Regular damage shown in white text (e.g., "✧ 5.0 ✧")
    - Critical hits displayed in red for better visibility
    - Numbers float upward and fade out after a short time
  - **Health Bars**: Monsters and custom mobs display health bars above their heads
    - Shows percentage and visual heart display (e.g., "85% ❤❤❤❤❤❤❤❤❤")
    - Hearts change color based on remaining health (green/yellow/red)
    - Health bars follow mobs as they move

- **TreeFeller System**: 
  - Added ability to quickly cut down entire trees by breaking the bottom log
  - Can be enabled/disabled in config.yml with `general.treefeller` option
  - Automatically detects tree structures and only works on actual trees

- **Custom Enchantments System**:
  - Added rare custom enchantments that can be found in structure loot (desert temples, strongholds, etc.)
  - 1 in 1000 chance to get custom enchantments from enchantment tables
  - Added `/enchantments` command to view all available enchantments with descriptions
  - Enchanted items now have a visual glow effect for better identification
  - Added particle effects and sound effects for enchantment activations
  
  ⛏️ **Pickaxe Enchantments**:
  - **Tunneling**: Mines blocks in a 3x3 area when a block is mined
  - **Auto Smelt**: Automatically smelts ores when mined
  - **Treasure Hunter I-II**: Chance to find extra items when mining valuable ores
  - **Haste I-II**: Grants mining speed boost while held
  
  🌾 **Hoe Enchantments**:
  - **Harvester I-III**: Harvests crops in a radius (1-3 blocks) around the target crop
  
  🥄 **Shovel Enchantments**:
  - **Shovel Tunneling**: Digs blocks in a 3x3 area when a block is dug
  
  ⚔️ **Sword Enchantments**:
  - **Execute I-III**: Deal bonus damage to enemies below 30% health
  - **Vampiric Edge I-II**: Small chance to heal a portion of damage dealt
  - **Crippling Strike I-III**: Chance to apply Slowness on hit
  - **Frenzy I-III**: Grants increasing attack speed with each consecutive hit
  - **Mark of Death**: Final hit marks enemy — next strike deals extra damage
  
  🪓 **Axe Enchantments**:
  - **Armor Crack I-IV**: Hits have a chance to reduce enemy armor durability faster
  - **Shockwave I-II**: Rare chance to knock back nearby players in a radius
  - **Rupture I-III**: Applies Bleeding effect (damage over time) for 3 seconds
  
  🛡️ **Armor Enchantments**:
  - **Molten I-II**: Chance to set attacker on fire when hit
  - **Guardians**: Small chance to spawn an Iron Golem when attacked
  - **Second Wind**: Gain a burst of regeneration when dropping below 20% HP (60-second cooldown)
  - **Stability I-III**: Reduces knockback taken by 20-60%
  - **Thorn Burst I-II**: Reflects a small AoE burst of damage when hit
  
  🥾 **Boot-Specific Enchantments**:
  - **Climb**: Allows wall climbing like a spider
  - **Shock Absorb I-II**: Reduces fall damage by 30-60%
  - **Speed Boost I-II**: Grants a passive speed boost when sprinting
  - **Shadowstep**: 5% chance to teleport behind attacker (10-second cooldown)
  
  🏹 **Bow Enchantments**:
  - **Explosive Arrow I-III**: Creates an explosion when arrows hit targets (1.5-2.5 radius)
  - **Multi-Shot I-II**: Fires additional arrows in a spread pattern (2-4 extra arrows)
  - **Teleport Arrow**: Teleports player to arrow landing location
  - **Gravity Well I-II**: Creates a gravity well at arrow impact, pulling entities toward it
  - **Lightning Strike I-II**: Chance to strike lightning when arrow hits (50-75% chance)

- **Custom Mobs System**:
  - Added 4 unique custom mobs with special abilities:
    - **Debug Mob**: Test mob with Netherite armor and weapons
    - **Frost Giant**: Ice-themed Iron Golem with frost aura and ground pound attack
    - **Shadow Assassin**: Stealth-based mob that teleports behind players and applies poison
    - **Corrupted Warden**: Powerful boss-like mob with EMP blast, teleportation, and minion summoning
  - All custom mobs have unique particle effects and visual appearances
  - Custom mobs can be summoned by admins using `/guildadmin summon <mobType>`

### New Admin Features
- **Admin Commands**: 
  - Added `/guildadmin enchant <type> [level]` command for administrators to add custom enchantments to tools
  - Added `/guildadmin give <book_type> [level]` command for administrators to obtain enchanted books
  - Added `/guildadmin godmode` command for administrators to toggle godmode (full immunity and invisibility to mobs)
  - Supports tab completion for all commands and enchantment types
  - Added `/guildadmin summon <mob_type>` command for administrators to spawn custom mobs
  - Added custom mob system with Debug mob for testing
    - Debug mob spawns with full Netherite armor, Netherite sword, and shield

- **Player Commands**:
  - Added `/enchantments` command for players to view available enchantments
  - Includes category filtering with `/enchantments <category>` (pickaxe, sword, axe, armor, boots, etc.)
  - Provides detailed information about each enchantment's effects and max levels

- **Configuration Updates**:
  - Admins can toggle TreeFeller in config.yml with `general.treefeller` option
  - Custom mobs can be toggled in config.yml with `general.custom-mobs.enabled` option (enabled by default)
  - Custom enchantments can be configured in config.yml:
    - `general.custom-enchantments.enabled`: Toggle the entire system
    - `general.custom-enchantments.enchant-table-chance`: Adjust chance from enchanting tables
    - `general.custom-enchantments.loot-chest-chance`: Adjust chance from loot chests
    - `general.custom-enchantments.locations`: Configure which structure types can contain enchanted items

### Improvements
- **Performance Optimization**: 
  - Added cooldown system for high-impact enchantments like Tunneling to prevent server lag
  - Implemented optimized material category system for faster enchantment processing
  - Added automatic cleanup of player data on logout to prevent memory leaks
  - Improved error handling to prevent plugin crashes from enchantment operations
  - Reduced server load when multiple players are using enchanted tools

- **User Experience**:
  - Enhanced visual feedback for enchantments with distinct particle effects
  - Added sound effects for enchantment activations
  - Improved enchanted item appearance with subtle glow effect
  - Better organized enchantment lore for easier reading
  - Enhanced `/enchantments` command with comprehensive categorization including bow enchantments
  - Added visual combat system with damage numbers and health bars for more engaging gameplay
  - Improved Lightning Strike enchantment with better visual indicators and more consistent behavior

### Known Bugs 
 - no bugs to report at this time

### Bug Fixes
- Fixed bug in guild promotion system where leaders could attempt to promote themselves
- Fixed duplicate method declarations in enchantment system
- Fixed issues with Auto Smelt enchantment not properly handling certain ore types
- Fixed issue with custom enchantments not working on enchanted books
- Fixed issue with climb enchantment not working on armor
- Fixed inconsistent behavior with Lightning Strike enchantment
- Fixed missing bow category in `/enchantments` command
- Fixed syntax error in AdminCommand.java preventing compilation
- Replaced admin enchantments with god mode and invincibility (full immunity and invisibility to mobs)

- Report any bugs at https://discord.gg/FGUEEj6k7k

---

### Thank you for using a product of Frostline Solutions LLC - Powered by Frostlines Dev Team - Working around the clock for the best experience possible with our products