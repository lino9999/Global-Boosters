package com.Lino.globalBoosters.managers;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.ActiveBooster;
import com.Lino.globalBoosters.boosters.BoosterType;
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
            activator.sendMessage(plugin.getMessagesManager().getMessage("booster.already-active"));
            return false;
        }

        int maxActive = plugin.getConfigManager().getMaxActiveBoosters();
        if (maxActive != -1 && activeBoosters.size() >= maxActive) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%max%", String.valueOf(maxActive));
            activator.sendMessage(plugin.getMessagesManager().getMessage("booster.max-active-reached", placeholders));
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

    public int getActiveBoosterCount() {
        return activeBoosters.size();
    }

    public double getMultiplier(BoosterType type) {
        if (isBoosterActive(type)) {
            return type.getMultiplier();
        }
        return 1.0;
    }

    private void announceBoosterActivation(BoosterType type, String activatorName, int durationMinutes) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%prefix%", plugin.getMessagesManager().getPrefix());
        placeholders.put("%player%", activatorName);
        placeholders.put("%booster%", type.getDisplayName());
        placeholders.put("%duration%", String.valueOf(durationMinutes));

        String message = plugin.getMessagesManager().getMessage("booster.activated", placeholders);
        Bukkit.broadcastMessage(message);
    }

    private void announceBoosterDeactivation(BoosterType type) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%prefix%", plugin.getMessagesManager().getPrefix());
        placeholders.put("%booster%", type.getDisplayName());

        String message = plugin.getMessagesManager().getMessage("booster.expired", placeholders);
        Bukkit.broadcastMessage(message);
    }

    private void playGlobalSound(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}