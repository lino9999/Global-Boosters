package com.Lino.globalBoosters.managers;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.ActiveBooster;
import com.Lino.globalBoosters.boosters.BoosterType;
import com.Lino.globalBoosters.utils.GradientColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarManager {

    private final GlobalBoosters plugin;
    private final Map<BoosterType, BossBar> bossBars;
    private final UUID serverUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

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
        Map<String, String> placeholders = new HashMap<>();

        String boosterNameRaw = plugin.getMessagesManager().getRawMessage("booster-names." + booster.getType().name().toLowerCase());
        if (boosterNameRaw == null) {
            boosterNameRaw = "<gradient:#FFD700:#FFA500>" + booster.getType().getDisplayName() + "</gradient>";
        }

        if (!booster.getType().isEffectBooster() && !isNoMultiplierBooster(booster.getType())) {
            double multiplier = plugin.getConfigManager().getBoosterMultiplier(booster.getType());
            boosterNameRaw += " <gradient:#808080:#A9A9A9>(" + multiplier + "x)</gradient>";
        } else if (booster.getType() == BoosterType.PLANT_GROWTH) {
            double multiplier = plugin.getConfigManager().getBoosterMultiplier(booster.getType());
            boosterNameRaw += " <gradient:#808080:#A9A9A9>(" + multiplier + "x)</gradient>";
        }

        placeholders.put("%booster%", boosterNameRaw);
        placeholders.put("%time%", booster.getTimeRemaining());
        placeholders.put("%player%", booster.getActivatorName());

        boolean isScheduled = booster.getActivatorUUID().equals(serverUUID);
        boolean showActivator = plugin.getConfigManager().isShowActivatorName();

        String messageKey;
        if (isScheduled) {
            messageKey = "bossbar.format-scheduled";
        } else if (showActivator) {
            messageKey = "bossbar.format";
        } else {
            messageKey = "bossbar.format-no-player";
        }

        return plugin.getMessagesManager().getMessage(messageKey, placeholders);
    }

    private boolean isNoMultiplierBooster(BoosterType type) {
        switch (type) {
            case NO_FALL_DAMAGE:
            case KEEP_INVENTORY:
            case FLY:
                return true;
            default:
                return false;
        }
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