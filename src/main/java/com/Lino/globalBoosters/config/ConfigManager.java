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

    public ConfigManager(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.boosterPrices = new HashMap<>();
        this.boosterDurations = new HashMap<>();

        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        for (BoosterType type : BoosterType.values()) {
            String path = "boosters." + type.name().toLowerCase();

            if (!config.contains(path + ".price")) {
                config.set(path + ".price", getDefaultPrice(type));
            }
            if (!config.contains(path + ".duration")) {
                config.set(path + ".duration", 30);
            }

            boosterPrices.put(type, config.getDouble(path + ".price"));
            boosterDurations.put(type, config.getInt(path + ".duration"));
        }

        plugin.saveConfig();
    }

    public double getBoosterPrice(BoosterType type) {
        return boosterPrices.getOrDefault(type, 1000.0);
    }

    public int getBoosterDuration(BoosterType type) {
        return boosterDurations.getOrDefault(type, 30);
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
            default:
                return 1000.0;
        }
    }
}