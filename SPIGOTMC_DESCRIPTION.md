# GuildWars Plugin v1.4.0


## 🔮 Enhanced Custom Enchantment System

**GuildWars 1.4** now features a powerful custom enchantment system with unique effects you won't find in vanilla Minecraft!

### ✨ Key Features

- **28+ Custom Enchantments** across all tool and armor types
- **Rare Drop System** - Find enchantments in structure loot or from enchanting tables
- **Visual Effects** - Unique particle effects and sounds for each enchantment
- **Performance Optimized** - Minimal server impact even with many players
- **Fully Configurable** - Adjust rarity, locations, and more in config

### 🛠️ Available Enchantments

[spoiler="Pickaxe Enchantments"]
- **Tunneling** - Mines blocks in a 3x3 area
- **Auto Smelt** - Automatically smelts ores when mined
- **Treasure Hunter I-II** - Chance to find extra items when mining valuable ores
- **Haste I-II** - Grants mining speed boost while held
[/spoiler]

[spoiler="Hoe & Shovel Enchantments"]
- **Harvester I-III** - Harvests crops in a radius around the target crop
- **Shovel Tunneling** - Digs blocks in a 3x3 area
[/spoiler]

[spoiler="Bow Enchantments"]
- **Explosive Arrow I-III** - Creates an explosion when arrows hit targets
- **Multi-Shot I-II** - Fires additional arrows in a spread pattern
- **Teleport Arrow** - Teleports player to arrow landing location
- **Gravity Well I-II** - Creates a gravity well at arrow impact
- **Lightning Strike I-II** - Chance to strike lightning when arrow hits (improved reliability)
[/spoiler]

[spoiler="Combat Enchantments"]
- **⚔️ Sword Enchantments**
  - **Execute I-III** - Deal bonus damage to enemies below 30% health
  - **Vampiric Edge I-II** - Small chance to heal from damage dealt
  - **Crippling Strike I-III** - Chance to apply Slowness on hit
  - **Frenzy I-III** - Grants increasing attack speed with consecutive hits
  - **Mark of Death** - Final hit marks enemy for extra damage on next strike

- **🪓 Axe Enchantments**
  - **Armor Crack I-IV** - Chance to damage enemy armor faster
  - **Shockwave I-II** - Rare chance to knock back nearby players
  - **Rupture I-III** - Applies Bleeding effect (damage over time)
[/spoiler]

[spoiler="Armor Enchantments"]
- **🛡️ All Armor**
  - **Molten I-II** - Chance to set attacker on fire
  - **Guardians** - Small chance to spawn an Iron Golem when attacked
  - **Second Wind** - Gain regeneration when health drops below 20%
  - **Stability I-III** - Reduces knockback taken
  - **Thorn Burst I-II** - Reflects AoE damage when hit

- **👢 Boots Only**
  - **Climb** - Allows wall climbing like a spider
  - **Shock Absorb I-II** - Reduces fall damage
  - **Speed Boost I-II** - Grants passive speed boost when sprinting
  - **Shadowstep** - Chance to teleport behind attacker
[/spoiler]

### 🎮 User-Friendly Commands

- **/enchantments** - View all available enchantments
- **/enchantments [category]** - Browse enchantments by category (including bow, pickaxe, etc.)
- **/guildadmin enchant [type] [level]** - Add enchantments to items (admin only)
- **/guildadmin give [book_type] [level]** - Get enchanted books (admin only)
- **/guildadmin godmode** - Toggle invincibility and invisibility to mobs (admin only)

### ⚙️ Performance Improvements

- Optimized material categories for faster processing
- Cooldown system for high-impact enchantments
- Automatic cleanup of player data to prevent memory leaks
- Enhanced error handling to prevent plugin crashes

### 🖼️ Visual Enhancements

- **Combat Visuals**:
  - Floating damage numbers show exactly how much damage you deal
  - Health bars display above monsters showing remaining health percentage
  - Critical hits highlighted in red for better visibility
  - Health bars change color based on monster health (green/yellow/red)

- **Enchantment Visuals**:
  - Enchanted items have a subtle glow effect
  - Distinct particle effects for each enchantment type
  - Custom sound effects for enchantment activations
  - Clear and organized item lore for easy reading
  - Improved visual feedback for Lightning Strike enchantment

## 🔄 Installation & Setup

1. Drop the plugin JAR into your plugins folder
2. Restart your server
3. Configure options in config.yml if desired
4. Enjoy the enhanced gameplay!

## 📋 Other Features

- Guild system with territory control
- TreeFeller system for quick tree harvesting
- Admin commands for server management

## 🧟 Custom Mobs System

- **Frost Giant** - Ice-themed Iron Golem with frost aura that slows nearby players
- **Shadow Assassin** - Stealth assassin that teleports behind players and applies potion effects
- **Corrupted Warden** - Powerful boss mob with EMP blast, teleportation, and minion summoning
- Admins can summon custom mobs with `/guildadmin summon <mobType>`
- Each mob has unique abilities, particle effects, and attributes

[Discord Support](https://discord.gg/FGUEEj6k7k)

**Made with ❤️ by Frostline Solutions LLC**
