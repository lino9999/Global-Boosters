package com.globalboosters.managers;

import com.globalboosters.GlobalBoosters;
import com.globalboosters.boosters.ActiveBooster;
import com.globalboosters.boosters.BoosterType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BoosterManager {

    private final GlobalBoosters plugin;
    private final Map<BoosterType, ActiveBooster> activeBoosters;

    public BoosterManager(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.activeBoosters = new ConcurrentHashMap<>();
    }

    public boolean activateBooster(BoosterType type, Player activator, int durationMinutes) {
        if (isBoosterActive(type)) {
            activator.sendMessage("§cThis booster type is already active!");
            return false;
        }

        ActiveBooster booster = new ActiveBooster(type, activator.getUniqueId(), activator.getName(), durationMinutes);
        activeBoosters.put(type, booster);

        plugin.getBossBarManager().createBossBar(booster);
        plugin.getDataManager().saveActiveBooster(booster);

        announceBoosterActivation(type, activator.getName(), durationMinutes);
        playGlobalSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);

        return true;
    }

    public void deactivateBooster(BoosterType type) {
        ActiveBooster booster = activeBoosters.remove(type);
        if (booster != null) {
            plugin.getBossBarManager().removeBossBar(type);
            plugin.getDataManager().removeActiveBooster(type);

            announceBoosterDeactivation(type);
            playGlobalSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
        }
    }

    public void tickAllBoosters() {
        Iterator<Map.Entry<BoosterType, ActiveBooster>> iterator = activeBoosters.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BoosterType, ActiveBooster> entry = iterator.next();
            ActiveBooster booster = entry.getValue();

            booster.tick();
            plugin.getBossBarManager().updateBossBar(booster);

            if (booster.isExpired()) {
                deactivateBooster(entry.getKey());
            }
        }
    }

    public void saveAllBoosters() {
        for (ActiveBooster booster : activeBoosters.values()) {
            plugin.getDataManager().saveActiveBooster(booster);
        }
    }

    public void loadBooster(ActiveBooster booster) {
        activeBoosters.put(booster.getType(), booster);
        plugin.getBossBarManager().createBossBar(booster);
    }

    public boolean isBoosterActive(BoosterType type) {
        return activeBoosters.containsKey(type);
    }

    public ActiveBooster getActiveBooster(BoosterType type) {
        return activeBoosters.get(type);
    }

    public Collection<ActiveBooster> getActiveBoosters() {
        return activeBoosters.values();
    }

    public double getMultiplier(BoosterType type) {
        if (isBoosterActive(type)) {
            return type.getMultiplier();
        }
        return 1.0;
    }

    private void announceBoosterActivation(BoosterType type, String activatorName, int durationMinutes) {
        String message = String.format("§6§l[BOOSTER] §e%s §ahas activated §e%s §afor §e%d minutes§a!",
                activatorName, type.getDisplayName(), durationMinutes);
        Bukkit.broadcastMessage(message);
    }

    private void announceBoosterDeactivation(BoosterType type) {
        String message = String.format("§6§l[BOOSTER] §e%s §chas expired!", type.getDisplayName());
        Bukkit.broadcastMessage(message);
    }

    private void playGlobalSound(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}