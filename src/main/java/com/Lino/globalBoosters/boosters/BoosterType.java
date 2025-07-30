package com.globalboosters.boosters;

import org.bukkit.Material;

public enum BoosterType {
    PLANT_GROWTH("Plant Growth Booster", Material.WHEAT, 2.0),
    SPAWNER_RATE("Spawner Rate Booster", Material.SPAWNER, 2.0),
    EXP_MULTIPLIER("Experience Booster", Material.EXPERIENCE_BOTTLE, 2.0),
    MOB_DROP("Mob Drop Booster", Material.ROTTEN_FLESH, 2.0),
    MINING_SPEED("Mining Speed Booster", Material.DIAMOND_PICKAXE, 1.5),
    FISHING_LUCK("Fishing Luck Booster", Material.FISHING_ROD, 2.0),
    FARMING_FORTUNE("Farming Fortune Booster", Material.GOLDEN_HOE, 2.0),
    COMBAT_DAMAGE("Combat Damage Booster", Material.DIAMOND_SWORD, 1.5);

    private final String displayName;
    private final Material icon;
    private final double multiplier;

    BoosterType(String displayName, Material icon, double multiplier) {
        this.displayName = displayName;
        this.icon = icon;
        this.multiplier = multiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public double getMultiplier() {
        return multiplier;
    }
}