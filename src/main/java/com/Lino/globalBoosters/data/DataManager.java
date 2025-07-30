package com.globalboosters.data;

import com.globalboosters.GlobalBoosters;
import com.globalboosters.boosters.ActiveBooster;
import com.globalboosters.boosters.BoosterType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataManager {

    private final GlobalBoosters plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "active_boosters.yml");

        createDataFile();
    }

    private void createDataFile() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data file!");
                e.printStackTrace();
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveActiveBooster(ActiveBooster booster) {
        String path = "boosters." + booster.getType().name();

        dataConfig.set(path + ".activator_uuid", booster.getActivatorUUID().toString());
        dataConfig.set(path + ".activator_name", booster.getActivatorName());
        dataConfig.set(path + ".start_time", booster.getStartTime());
        dataConfig.set(path + ".duration_minutes", booster.getDurationMinutes());
        dataConfig.set(path + ".remaining_seconds", booster.getRemainingSeconds());

        saveDataFile();
    }

    public void removeActiveBooster(BoosterType type) {
        dataConfig.set("boosters." + type.name(), null);
        saveDataFile();
    }

    public void loadActiveBoosters() {
        if (!dataConfig.contains("boosters")) {
            return;
        }

        for (String typeName : dataConfig.getConfigurationSection("boosters").getKeys(false)) {
            try {
                BoosterType type = BoosterType.valueOf(typeName);
                String path = "boosters." + typeName;

                UUID activatorUUID = UUID.fromString(dataConfig.getString(path + ".activator_uuid"));
                String activatorName = dataConfig.getString(path + ".activator_name");
                long startTime = dataConfig.getLong(path + ".start_time");
                int durationMinutes = dataConfig.getInt(path + ".duration_minutes");
                long remainingSeconds = dataConfig.getLong(path + ".remaining_seconds");

                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                long totalDuration = durationMinutes * 60L;

                if (elapsedTime < totalDuration) {
                    long adjustedRemaining = totalDuration - elapsedTime;

                    ActiveBooster booster = new ActiveBooster(
                            type,
                            activatorUUID,
                            activatorName,
                            startTime,
                            durationMinutes,
                            adjustedRemaining
                    );

                    plugin.getBoosterManager().loadBooster(booster);
                } else {
                    removeActiveBooster(type);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load booster: " + typeName);
                e.printStackTrace();
            }
        }
    }

    private void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data file!");
            e.printStackTrace();
        }
    }
}