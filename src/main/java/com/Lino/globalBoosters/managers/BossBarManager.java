package com.Lino.globalBoosters.managers;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.ActiveBooster;
import com.Lino.globalBoosters.boosters.BoosterType;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarManager {

    private final GlobalBoosters plugin;
    private final Map<BoosterType, BossBar> bossBars;

    public BossBarManager(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.bossBars = new ConcurrentHashMap<>();
    }

    public void createBossBar(ActiveBooster booster) {
        BossBar bossBar = Bukkit.createBossBar(
                formatBossBarTitle(booster),
                getBarColor(booster.getType()),
                BarStyle.SOLID
        );

        bossBar.setProgress(booster.getProgress());
        bossBar.setVisible(true);

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

        bossBars.put(booster.getType(), bossBar);
    }

    public void updateBossBar(ActiveBooster booster) {
        BossBar bossBar = bossBars.get(booster.getType());
        if (bossBar != null) {
            bossBar.setTitle(formatBossBarTitle(booster));
            bossBar.setProgress(Math.max(0, Math.min(1, booster.getProgress())));
        }
    }

    public void removeBossBar(BoosterType type) {
        BossBar bossBar = bossBars.remove(type);
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
    }

    public void removeAllBossBars() {
        for (BossBar bossBar : bossBars.values()) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
        bossBars.clear();
    }

    public void addPlayerToBossBars(Player player) {
        for (BossBar bossBar : bossBars.values()) {
            bossBar.addPlayer(player);
        }
    }

    private String formatBossBarTitle(ActiveBooster booster) {
        return String.format("§e%s §7- §a%s §7sponsored by §b%s",
                booster.getType().getDisplayName(),
                booster.getTimeRemaining(),
                booster.getActivatorName()
        );
    }

    private BarColor getBarColor(BoosterType type) {
        switch (type) {
            case PLANT_GROWTH:
            case FARMING_FORTUNE:
                return BarColor.GREEN;
            case SPAWNER_RATE:
            case MOB_DROP:
                return BarColor.RED;
            case EXP_MULTIPLIER:
                return BarColor.YELLOW;
            case MINING_SPEED:
                return BarColor.BLUE;
            case FISHING_LUCK:
                return BarColor.WHITE;
            case COMBAT_DAMAGE:
                return BarColor.PURPLE;
            default:
                return BarColor.PINK;
        }
    }
}