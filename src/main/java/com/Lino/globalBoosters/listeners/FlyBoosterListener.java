package com.Lino.globalBoosters.listeners;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FlyBoosterListener implements Listener {

    private final GlobalBoosters plugin;
    private final Set<UUID> originallyFlying;

    public FlyBoosterListener(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.originallyFlying = new HashSet<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getBoosterManager().isBoosterActive(BoosterType.FLY)) {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }
    }

    public void enableFlyForAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getAllowFlight()) {
                originallyFlying.add(player.getUniqueId());
            } else {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }
    }

    public void disableFlyForAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!originallyFlying.contains(player.getUniqueId()) && !player.hasPermission("globalboosters.fly.bypass")) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
        originallyFlying.clear();
    }
}