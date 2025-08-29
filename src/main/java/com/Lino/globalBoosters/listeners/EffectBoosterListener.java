package com.Lino.globalBoosters.listeners;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.ActiveBooster;
import com.Lino.globalBoosters.boosters.BoosterType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EffectBoosterListener implements Listener {

    private final GlobalBoosters plugin;
    private final Map<BoosterType, PotionEffectType> effectMap;
    private final Map<BoosterType, Set<UUID>> activeEffectPlayers;

    public EffectBoosterListener(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.effectMap = new HashMap<>();
        this.activeEffectPlayers = new HashMap<>();
        initializeEffectMap();
        initializeActiveEffectPlayers();
    }

    private void initializeEffectMap() {
        effectMap.put(BoosterType.HASTE, PotionEffectType.HASTE);
        effectMap.put(BoosterType.RESISTANCE, PotionEffectType.RESISTANCE);
        effectMap.put(BoosterType.JUMP_BOOST, PotionEffectType.JUMP_BOOST);
        effectMap.put(BoosterType.REGENERATION, PotionEffectType.REGENERATION);
        effectMap.put(BoosterType.NIGHT_VISION, PotionEffectType.NIGHT_VISION);
        effectMap.put(BoosterType.FIRE_RESISTANCE, PotionEffectType.FIRE_RESISTANCE);
        effectMap.put(BoosterType.SPEED, PotionEffectType.SPEED);
        effectMap.put(BoosterType.STRENGTH, PotionEffectType.STRENGTH);
    }

    private void initializeActiveEffectPlayers() {
        for (BoosterType type : effectMap.keySet()) {
            activeEffectPlayers.put(type, new HashSet<>());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        applyActiveEffects(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        for (Map.Entry<BoosterType, PotionEffectType> entry : effectMap.entrySet()) {
            BoosterType boosterType = entry.getKey();
            PotionEffectType effectType = entry.getValue();

            if (player.hasPotionEffect(effectType) && plugin.getBoosterManager().isBoosterActive(boosterType)) {
                activeEffectPlayers.get(boosterType).add(playerId);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (plugin.getConfigManager().isKeepEffectsOnDeath()) {
            Player player = event.getPlayer();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                applyActiveEffects(player);
                if (plugin.getBoosterManager().isBoosterActive(BoosterType.FLY)) {
                    if (!player.getAllowFlight()) {
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    }
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().isKeepEffectsOnDeath()) {
            Player player = event.getEntity();
            for (Map.Entry<BoosterType, PotionEffectType> entry : effectMap.entrySet()) {
                if (plugin.getBoosterManager().isBoosterActive(entry.getKey())) {
                    player.removePotionEffect(entry.getValue());
                }
            }
        }
    }

    public void applyActiveEffects(Player player) {
        UUID playerId = player.getUniqueId();

        for (Map.Entry<BoosterType, PotionEffectType> entry : effectMap.entrySet()) {
            BoosterType boosterType = entry.getKey();
            PotionEffectType effectType = entry.getValue();

            if (plugin.getBoosterManager().isBoosterActive(boosterType)) {
                ActiveBooster booster = plugin.getBoosterManager().getActiveBooster(boosterType);
                if (booster != null && !booster.isExpired()) {
                    int amplifier = (int) (plugin.getConfigManager().getBoosterMultiplier(boosterType) - 1);
                    int duration = (int) (booster.getRemainingSeconds() * 20);
                    duration = Math.min(duration, Integer.MAX_VALUE - 1000);

                    player.addPotionEffect(new PotionEffect(effectType, duration, amplifier, false, false));
                    activeEffectPlayers.get(boosterType).add(playerId);
                } else {
                    player.removePotionEffect(effectType);
                    activeEffectPlayers.get(boosterType).remove(playerId);
                }
            } else {
                if (activeEffectPlayers.get(boosterType).contains(playerId)) {
                    player.removePotionEffect(effectType);
                    activeEffectPlayers.get(boosterType).remove(playerId);
                }
            }
        }
    }

    public void applyEffectToAll(BoosterType type) {
        PotionEffectType effectType = effectMap.get(type);
        if (effectType != null) {
            ActiveBooster booster = plugin.getBoosterManager().getActiveBooster(type);
            if (booster != null && !booster.isExpired()) {
                int amplifier = (int) (plugin.getConfigManager().getBoosterMultiplier(type) - 1);
                int duration = (int) (booster.getRemainingSeconds() * 20);
                duration = Math.min(duration, Integer.MAX_VALUE - 1000);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.addPotionEffect(new PotionEffect(effectType, duration, amplifier, false, false));
                    activeEffectPlayers.get(type).add(player.getUniqueId());
                }
            }
        }
    }

    public void removeEffectFromAll(BoosterType type) {
        PotionEffectType effectType = effectMap.get(type);
        if (effectType != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.removePotionEffect(effectType);
            }
            activeEffectPlayers.get(type).clear();
        }
    }

    public void updateEffectDuration(BoosterType type) {
        PotionEffectType effectType = effectMap.get(type);
        if (effectType != null) {
            ActiveBooster booster = plugin.getBoosterManager().getActiveBooster(type);
            if (booster != null && !booster.isExpired()) {
                int amplifier = (int) (plugin.getConfigManager().getBoosterMultiplier(type) - 1);
                int duration = (int) (booster.getRemainingSeconds() * 20);
                duration = Math.min(duration, Integer.MAX_VALUE - 1000);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (activeEffectPlayers.get(type).contains(player.getUniqueId())) {
                        player.removePotionEffect(effectType);
                        player.addPotionEffect(new PotionEffect(effectType, duration, amplifier, false, false));
                    }
                }
            }
        }
    }
}