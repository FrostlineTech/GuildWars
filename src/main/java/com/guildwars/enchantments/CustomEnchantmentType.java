package com.guildwars.enchantments;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a type of custom enchantment.
 */
public enum CustomEnchantmentType {
    
    /**
     * Harvester enchantment for hoes.
     * Harvests crops in an area around the target crop.
     * Level 1-3 determines the radius of effect.
     */
    HARVESTER("Harvester", 3, "Harvests crops in a radius around the target crop"),
    
    /**
     * Tunneling enchantment for pickaxes.
     * Mines blocks in a 3x3 area when a block is mined.
     */
    TUNNELING("Tunneling", 1, "Mines blocks in a 3x3 area"),
    
    /**
     * Shovel Tunneling enchantment for shovels.
     * Digs blocks in a 3x3 area when a block is dug.
     */
    SHOVEL_TUNNELING("Shovel Tunneling", 1, "Digs blocks in a 3x3 area"),
    
    /**
     * Execute enchantment for swords.
     * Deal bonus damage to enemies below 30% health.
     * Level 1-3 determines the damage multiplier.
     */
    EXECUTE("Execute", 3, "Deal bonus damage to enemies below 30% health"),
    
    /**
     * Vampiric Edge enchantment for swords.
     * Small chance to heal a portion of damage dealt.
     * Level 1-2 determines the healing amount.
     */
    VAMPIRIC_EDGE("Vampiric Edge", 2, "Small chance to heal a portion of damage dealt"),
    
    /**
     * Crippling Strike enchantment for swords.
     * Chance to apply Slowness on hit.
     * Level 1-3 determines the slowness duration and level.
     */
    CRIPPLING_STRIKE("Crippling Strike", 3, "Chance to apply Slowness on hit"),
    
    /**
     * Frenzy enchantment for swords.
     * Grants increasing attack speed with each consecutive hit.
     * Level 1-3 determines the attack speed bonus and maximum stacks.
     */
    FRENZY("Frenzy", 3, "Grants increasing attack speed with each consecutive hit"),
    
    /**
     * Mark of Death enchantment for swords.
     * Final hit marks enemy — next strike deals extra damage.
     */
    MARK_OF_DEATH("Mark of Death", 1, "Final hit marks enemy — next strike deals extra damage"),
    
    /**
     * Armor Crack enchantment for axes.
     * Hits have a chance to reduce enemy armor durability faster.
     * Level 1-4 determines the chance and amount of extra durability damage.
     */
    ARMOR_CRACK("Armor Crack", 4, "Hits have a chance to reduce enemy armor durability faster"),
    
    /**
     * Shockwave enchantment for axes.
     * Rare chance to knock back nearby players in a radius.
     * Level 1-2 determines the radius and knockback strength.
     */
    SHOCKWAVE("Shockwave", 2, "Rare chance to knock back nearby players in a radius"),
    
    /**
     * Rupture enchantment for axes.
     * Applies Bleeding effect (damage over time) for 3 seconds.
     * Level 1-3 determines the damage amount and duration.
     */
    RUPTURE("Rupture", 3, "Applies Bleeding effect (damage over time) for 3 seconds"),
    
    /**
     * Molten enchantment for armor.
     * Chance to set attacker on fire when hit.
     * Level 1-2 determines the fire duration.
     */
    MOLTEN("Molten", 2, "Chance to set attacker on fire when hit"),
    
    /**
     * Guardians enchantment for armor.
     * Small chance to spawn an Iron Golem when attacked.
     */
    GUARDIANS("Guardians", 1, "Small chance to spawn an Iron Golem when attacked"),
    
    /**
     * Second Wind enchantment for armor.
     * Gain a burst of regeneration when dropping below 20% HP (cooldown).
     */
    SECOND_WIND("Second Wind", 1, "Gain a burst of regeneration when dropping below 20% HP (cooldown)"),
    
    /**
     * Stability enchantment for armor.
     * Reduces knockback taken.
     * Level 1-3 determines the knockback reduction amount.
     */
    STABILITY("Stability", 3, "Reduces knockback taken"),
    
    /**
     * Thorn Burst enchantment for armor.
     * Reflects a small AoE burst of damage when hit.
     * Level 1-2 determines the damage and radius.
     */
    THORN_BURST("Thorn Burst", 2, "Reflects a small AoE burst of damage when hit"),
    
    /**
     * Climb enchantment for boots only.
     * Allows wall climbing like a spider.
     */
    CLIMB("Climb", 1, "Allows wall climbing like a spider"),
    
    /**
     * Shock Absorb enchantment for boots only.
     * Reduces fall damage slightly.
     * Level 1-2 determines the damage reduction amount.
     */
    SHOCK_ABSORB("Shock Absorb", 2, "Reduces fall damage slightly"),
    
    /**
     * Speed Boost enchantment for boots only.
     * Grants a passive speed boost when sprinting.
     * Level 1-2 determines the speed boost amount.
     */
    SPEED_BOOST("Speed Boost", 2, "Grants a passive speed boost when sprinting"),
    
    /**
     * Shadowstep enchantment for boots only.
     * 5% chance to teleport behind attacker (short range).
     */
    SHADOWSTEP("Shadowstep", 1, "5% chance to teleport behind attacker (short range)"),
    
    /**
     * Auto Smelt enchantment for pickaxes only.
     * Automatically smelts ores when mined.
     */
    AUTO_SMELT("Auto Smelt", 1, "Automatically smelts ores when mined"),
    
    /**
     * Haste enchantment for pickaxes only.
     * Grants a mining speed boost while holding the tool.
     * Level 1-2 determines the speed boost amount.
     */
    HASTE("Haste", 2, "Grants a mining speed boost while holding the tool"),
    
    /**
     * Treasure Hunter enchantment for pickaxes.
     * Slightly increases chance of finding rare drops.
     * Level 1-2 determines the chance increase amount.
     */
    TREASURE_HUNTER("Treasure Hunter", 2, "Slightly increases chance of finding rare drops"),
    
    /**
     * Explosive Arrow enchantment for bows.
     * Creates an explosion when arrows hit their target.
     * Level 1-3 determines explosion radius.
     */
    EXPLOSIVE_ARROW("Explosive Arrow", 3, "Creates an explosion when arrows hit targets"),
    
    /**
     * Multi-Shot enchantment for bows.
     * Fires additional arrows in a spread pattern.
     * Level 1-2 determines number of additional arrows.
     */
    MULTI_SHOT("Multi-Shot", 2, "Fires additional arrows in a spread pattern"),
    
    /**
     * Teleport Arrow enchantment for bows.
     * Teleports player to arrow landing location.
     */
    TELEPORT_ARROW("Teleport Arrow", 1, "Teleports player to arrow landing location"),
    
    /**
     * Gravity Well enchantment for bows.
     * Creates a gravity well at arrow impact, pulling entities toward it.
     * Level 1-2 determines radius and strength.
     */
    GRAVITY_WELL("Gravity Well", 2, "Creates a gravity well at arrow impact"),
    
    /**
     * Lightning Strike enchantment for bows.
     * Chance to strike lightning when arrow hits.
     * Level 1-2 determines chance and number of strikes.
     */
    LIGHTNING_STRIKE("Lightning Strike", 2, "Chance to strike lightning when arrow hits");
    
    private final String displayName;
    private final int maxLevel;
    private final String description;
    
    CustomEnchantmentType(String displayName, int maxLevel, String description) {
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.description = description;
    }
    
    /**
     * Gets the display name of this enchantment.
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the maximum level of this enchantment.
     *
     * @return The maximum level
     */
    public int getMaxLevel() {
        return maxLevel;
    }
    
    /**
     * Gets the description of this enchantment.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the formatted display name of this enchantment.
     *
     * @param level The level of the enchantment
     * @return The formatted display name
     */
    public String getFormattedName(int level) {
        return ChatColor.GRAY + displayName + " " + getRomanNumeral(level);
    }
    
    /**
     * Converts an integer to a Roman numeral.
     *
     * @param number The number to convert
     * @return The Roman numeral representation
     */
    public String getRomanNumeral(int number) {
        switch (number) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            default:
                return String.valueOf(number);
        }
    }
    
    /**
     * Checks if this enchantment can be applied to the given item.
     *
     * @param item The item to check
     * @return True if the enchantment can be applied
     */
    public boolean canEnchantItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        
        Material type = item.getType();
        
        switch (this) {
            case HARVESTER:
                return type == Material.WOODEN_HOE || 
                       type == Material.STONE_HOE || 
                       type == Material.IRON_HOE || 
                       type == Material.GOLDEN_HOE || 
                       type == Material.DIAMOND_HOE || 
                       type == Material.NETHERITE_HOE;
                
            case TUNNELING:
                return type == Material.WOODEN_PICKAXE || 
                       type == Material.STONE_PICKAXE || 
                       type == Material.IRON_PICKAXE || 
                       type == Material.GOLDEN_PICKAXE || 
                       type == Material.DIAMOND_PICKAXE || 
                       type == Material.NETHERITE_PICKAXE;
                
            // Admin enchantments have been removed
                
            case SHOVEL_TUNNELING:
                return type == Material.WOODEN_SHOVEL || 
                       type == Material.STONE_SHOVEL || 
                       type == Material.IRON_SHOVEL || 
                       type == Material.GOLDEN_SHOVEL || 
                       type == Material.DIAMOND_SHOVEL || 
                       type == Material.NETHERITE_SHOVEL;
            
            // All sword enchantments use the same check
            case EXECUTE:
            case VAMPIRIC_EDGE:
            case CRIPPLING_STRIKE:
            case FRENZY:
            case MARK_OF_DEATH:
                return type == Material.WOODEN_SWORD || 
                       type == Material.STONE_SWORD || 
                       type == Material.IRON_SWORD || 
                       type == Material.GOLDEN_SWORD || 
                       type == Material.DIAMOND_SWORD || 
                       type == Material.NETHERITE_SWORD;
            
            // All axe enchantments use the same check
            case ARMOR_CRACK:
            case SHOCKWAVE:
            case RUPTURE:
                return type == Material.WOODEN_AXE || 
                       type == Material.STONE_AXE || 
                       type == Material.IRON_AXE || 
                       type == Material.GOLDEN_AXE || 
                       type == Material.DIAMOND_AXE || 
                       type == Material.NETHERITE_AXE;
                
            // All armor enchantments use the same check
            case MOLTEN:
            case GUARDIANS:
            case SECOND_WIND:
            case STABILITY:
            case THORN_BURST:
                return type == Material.LEATHER_HELMET || 
                       type == Material.LEATHER_CHESTPLATE || 
                       type == Material.LEATHER_LEGGINGS || 
                       type == Material.LEATHER_BOOTS || 
                       type == Material.CHAINMAIL_HELMET || 
                       type == Material.CHAINMAIL_CHESTPLATE || 
                       type == Material.CHAINMAIL_LEGGINGS || 
                       type == Material.CHAINMAIL_BOOTS || 
                       type == Material.IRON_HELMET || 
                       type == Material.IRON_CHESTPLATE || 
                       type == Material.IRON_LEGGINGS || 
                       type == Material.IRON_BOOTS || 
                       type == Material.GOLDEN_HELMET || 
                       type == Material.GOLDEN_CHESTPLATE || 
                       type == Material.GOLDEN_LEGGINGS || 
                       type == Material.GOLDEN_BOOTS || 
                       type == Material.DIAMOND_HELMET || 
                       type == Material.DIAMOND_CHESTPLATE || 
                       type == Material.DIAMOND_LEGGINGS || 
                       type == Material.DIAMOND_BOOTS || 
                       type == Material.NETHERITE_HELMET || 
                       type == Material.NETHERITE_CHESTPLATE || 
                       type == Material.NETHERITE_LEGGINGS || 
                       type == Material.NETHERITE_BOOTS;
                
            // Boot-specific enchantments
            case CLIMB:
            case SHOCK_ABSORB:
            case SPEED_BOOST:
            case SHADOWSTEP:
                return type == Material.LEATHER_BOOTS || 
                       type == Material.CHAINMAIL_BOOTS || 
                       type == Material.IRON_BOOTS || 
                       type == Material.GOLDEN_BOOTS || 
                       type == Material.DIAMOND_BOOTS || 
                       type == Material.NETHERITE_BOOTS;
                
            // Pickaxe-specific enchantments
            case AUTO_SMELT:
            case HASTE:
            case TREASURE_HUNTER:
                return type == Material.WOODEN_PICKAXE || 
                       type == Material.STONE_PICKAXE || 
                       type == Material.IRON_PICKAXE || 
                       type == Material.GOLDEN_PICKAXE || 
                       type == Material.DIAMOND_PICKAXE || 
                       type == Material.NETHERITE_PICKAXE;
                
            // Bow-specific enchantments
            case EXPLOSIVE_ARROW:
            case MULTI_SHOT:
            case TELEPORT_ARROW:
            case GRAVITY_WELL:
            case LIGHTNING_STRIKE:
                return type == Material.BOW || type == Material.CROSSBOW;
                
            default:
                return false;
        }
    }
    
    /**
     * Gets the key for this enchantment.
     *
     * @return The key
     */
    public String getKey() {
        return name().toLowerCase();
    }
}
