package com.Lino.globalBoosters.boosters;

import org.bukkit.Material;

public enum BoosterType {
    PLANT_GROWTH("Plant Growth Booster", Material.WHEAT, 2.0, false),
    SPAWNER_RATE("Spawner Rate Booster", Material.SPAWNER, 2.0, false),
    EXP_MULTIPLIER("Experience Booster", Material.EXPERIENCE_BOTTLE, 2.0, false),
    MOB_DROP("Mob Drop Booster", Material.ROTTEN_FLESH, 2.0, false),
    MINING_SPEED("Mining Speed Booster", Material.DIAMOND_PICKAXE, 1.5, false),
    FISHING_LUCK("Fishing Luck Booster", Material.FISHING_ROD, 2.0, false),
    FARMING_FORTUNE("Farming Fortune Booster", Material.GOLDEN_HOE, 2.0, false),
    COMBAT_DAMAGE("Combat Damage Booster", Material.DIAMOND_SWORD, 1.5, false),
    HASTE("Haste Booster", Material.GOLDEN_PICKAXE, 1.0, true),
    RESISTANCE("Resistance Booster", Material.SHIELD, 1.0, true),
    JUMP_BOOST("Jump Boost Booster", Material.RABBIT_FOOT, 1.0, true),
    REGENERATION("Regeneration Booster", Material.GOLDEN_APPLE, 1.0, true),
    NIGHT_VISION("Night Vision Booster", Material.GOLDEN_CARROT, 1.0, true),
    FIRE_RESISTANCE("Fire Resistance Booster", Material.MAGMA_CREAM, 1.0, true),
    SPEED("Speed Booster", Material.SUGAR, 1.0, true),
    STRENGTH("Strength Booster", Material.BLAZE_POWDER, 1.0, true);

    private final String displayName;
    private final Material icon;
    private final double defaultMultiplier;
    private final boolean isEffectBooster;

    BoosterType(String displayName, Material icon, double defaultMultiplier, boolean isEffectBooster) {
        this.displayName = displayName;
        this.icon = icon;
        this.defaultMultiplier = defaultMultiplier;
        this.isEffectBooster = isEffectBooster;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public double getDefaultMultiplier() {
        return defaultMultiplier;
    }

    public boolean isEffectBooster() {
        return isEffectBooster;
    }
}