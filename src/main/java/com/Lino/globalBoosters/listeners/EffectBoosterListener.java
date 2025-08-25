package com.Lino.globalBoosters.listeners;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class EffectBoosterListener implements Listener {

    private final GlobalBoosters plugin;
    private final Map<BoosterType, PotionEffectType> effectMap;

    public EffectBoosterListener(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.effectMap = new HashMap<>();
        initializeEffectMap();
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        applyActiveEffects(player);
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
        for (Map.Entry<BoosterType, PotionEffectType> entry : effectMap.entrySet()) {
            if (plugin.getBoosterManager().isBoosterActive(entry.getKey())) {
                int amplifier = (int) (plugin.getConfigManager().getBoosterMultiplier(entry.getKey()) - 1);
                player.addPotionEffect(new PotionEffect(entry.getValue(), Integer.MAX_VALUE, amplifier, false, false));
            }
        }
    }

    public void applyEffectToAll(BoosterType type) {
        PotionEffectType effectType = effectMap.get(type);
        if (effectType != null) {
            int amplifier = (int) (plugin.getConfigManager().getBoosterMultiplier(type) - 1);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, amplifier, false, false));
            }
        }
    }

    public void removeEffectFromAll(BoosterType type) {
        PotionEffectType effectType = effectMap.get(type);
        if (effectType != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.removePotionEffect(effectType);
            }
        }
    }
}