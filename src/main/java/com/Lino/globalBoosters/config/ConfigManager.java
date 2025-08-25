package com.Lino.globalBoosters.config;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final GlobalBoosters plugin;
    private final Map<BoosterType, Double> boosterPrices;
    private final Map<BoosterType, Integer> boosterDurations;
    private final Map<BoosterType, Double> boosterMultipliers;
    private final Map<BoosterType, Boolean> boosterEnabled;
    private int maxActiveBoosters;
    private boolean limitedSupplyEnabled;
    private int resetHour;
    private int maxSupplyPerBooster;
    private boolean keepEffectsOnDeath;

    public ConfigManager(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.boosterPrices = new HashMap<>();
        this.boosterDurations = new HashMap<>();
        this.boosterMultipliers = new HashMap<>();
        this.boosterEnabled = new HashMap<>();

        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        maxActiveBoosters = config.getInt("max_active_boosters", 3);
        limitedSupplyEnabled = config.getBoolean("limited_supply_mode", false);
        resetHour = config.getInt("supply_reset_hour", 0);
        maxSupplyPerBooster = config.getInt("max_supply_per_booster", 10);
        keepEffectsOnDeath = config.getBoolean("keep_effects_on_death", true);

        for (BoosterType type : BoosterType.values()) {
            String path = "boosters." + type.name().toLowerCase();

            if (!config.contains(path + ".enabled")) {
                config.set(path + ".enabled", true);
            }
            if (!config.contains(path + ".price")) {
                config.set(path + ".price", getDefaultPrice(type));
            }
            if (!config.contains(path + ".duration")) {
                config.set(path + ".duration", 30);
            }

            boosterEnabled.put(type, config.getBoolean(path + ".enabled", true));
            boosterPrices.put(type, config.getDouble(path + ".price"));
            boosterDurations.put(type, config.getInt(path + ".duration"));

            if (!type.isEffectBooster() && !isNoMultiplierBooster(type)) {
                if (!config.contains(path + ".multiplier")) {
                    config.set(path + ".multiplier", type.getDefaultMultiplier());
                }
                boosterMultipliers.put(type, config.getDouble(path + ".multiplier"));
            }
        }

        plugin.saveConfig();
    }

    public double getBoosterPrice(BoosterType type) {
        return boosterPrices.getOrDefault(type, 1000.0);
    }

    public int getBoosterDuration(BoosterType type) {
        return boosterDurations.getOrDefault(type, 30);
    }

    public double getBoosterMultiplier(BoosterType type) {
        return boosterMultipliers.getOrDefault(type, type.getDefaultMultiplier());
    }

    public int getMaxActiveBoosters() {
        return maxActiveBoosters;
    }

    public boolean isLimitedSupplyEnabled() {
        return limitedSupplyEnabled;
    }

    public int getResetHour() {
        return resetHour;
    }

    public boolean isBoosterEnabled(BoosterType type) {
        return boosterEnabled.getOrDefault(type, true);
    }

    public int getMaxSupplyPerBooster() {
        return maxSupplyPerBooster;
    }

    public boolean isKeepEffectsOnDeath() {
        return keepEffectsOnDeath;
    }

    private boolean isNoMultiplierBooster(BoosterType type) {
        switch (type) {
            case NO_FALL_DAMAGE:
            case KEEP_INVENTORY:
            case FLY:
            case PLANT_GROWTH:
                return true;
            default:
                return false;
        }
    }

    public void reload() {
        boosterPrices.clear();
        boosterDurations.clear();
        boosterMultipliers.clear();
        boosterEnabled.clear();
        loadConfig();
    }

    private double getDefaultPrice(BoosterType type) {
        switch (type) {
            case PLANT_GROWTH:
                return 500.0;
            case SPAWNER_RATE:
                return 1500.0;
            case EXP_MULTIPLIER:
                return 1000.0;
            case MOB_DROP:
                return 1200.0;
            case MINING_SPEED:
                return 800.0;
            case FISHING_LUCK:
                return 600.0;
            case FARMING_FORTUNE:
                return 700.0;
            case COMBAT_DAMAGE:
                return 2000.0;
            case HASTE:
            case RESISTANCE:
            case JUMP_BOOST:
            case REGENERATION:
            case NIGHT_VISION:
            case FIRE_RESISTANCE:
            case SPEED:
            case STRENGTH:
                return 1000.0;
            case NO_FALL_DAMAGE:
                return 800.0;
            case HUNGER_SAVER:
                return 600.0;
            case ARMOR_DURABILITY:
                return 1200.0;
            case KEEP_INVENTORY:
                return 3000.0;
            case FLY:
                return 2500.0;
            default:
                return 1000.0;
        }
    }
}