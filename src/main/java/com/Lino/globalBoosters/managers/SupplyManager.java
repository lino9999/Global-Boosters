package com.Lino.globalBoosters.managers;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class SupplyManager {

    private final GlobalBoosters plugin;
    private final Map<BoosterType, Integer> purchaseCounts;
    private final Map<BoosterType, Long> lastResetTime;
    private Connection connection;
    private static final int MAX_PURCHASES = 10;

    public SupplyManager(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.purchaseCounts = new ConcurrentHashMap<>();
        this.lastResetTime = new ConcurrentHashMap<>();

        initializeDatabase();
        loadData();
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/supply_data.db");

            String createTable = "CREATE TABLE IF NOT EXISTS supply_data (" +
                    "booster_type VARCHAR(50) PRIMARY KEY," +
                    "purchase_count INTEGER DEFAULT 0," +
                    "last_reset BIGINT DEFAULT 0)";

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTable);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Could not initialize supply database!");
            e.printStackTrace();
        }
    }

    private void loadData() {
        String query = "SELECT * FROM supply_data";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                try {
                    BoosterType type = BoosterType.valueOf(rs.getString("booster_type"));
                    purchaseCounts.put(type, rs.getInt("purchase_count"));
                    lastResetTime.put(type, rs.getLong("last_reset"));
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (BoosterType type : BoosterType.values()) {
            if (!purchaseCounts.containsKey(type)) {
                purchaseCounts.put(type, 0);
                lastResetTime.put(type, System.currentTimeMillis());
                saveBoosterData(type);
            }
            checkAndResetIfNeeded(type);
        }
    }

    private void saveBoosterData(BoosterType type) {
        String query = "INSERT OR REPLACE INTO supply_data (booster_type, purchase_count, last_reset) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, type.name());
            pstmt.setInt(2, purchaseCounts.get(type));
            pstmt.setLong(3, lastResetTime.get(type));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean canPurchase(BoosterType type) {
        if (!plugin.getConfigManager().isLimitedSupplyEnabled()) {
            return true;
        }

        checkAndResetIfNeeded(type);
        int currentCount = purchaseCounts.get(type);
        return currentCount < MAX_PURCHASES;
    }

    public synchronized int getRemainingPurchases(BoosterType type) {
        if (!plugin.getConfigManager().isLimitedSupplyEnabled()) {
            return -1;
        }

        checkAndResetIfNeeded(type);
        int remaining = MAX_PURCHASES - purchaseCounts.get(type);
        return Math.max(0, remaining);
    }

    public synchronized boolean recordPurchase(BoosterType type) {
        if (!plugin.getConfigManager().isLimitedSupplyEnabled()) {
            return true;
        }

        checkAndResetIfNeeded(type);

        int currentCount = purchaseCounts.get(type);
        if (currentCount >= MAX_PURCHASES) {
            return false;
        }

        purchaseCounts.put(type, currentCount + 1);
        saveBoosterData(type);
        return true;
    }

    public String getTimeUntilReset(BoosterType type) {
        if (!plugin.getConfigManager().isLimitedSupplyEnabled()) {
            return "";
        }

        long nextReset = getNextResetTime();
        long timeRemaining = nextReset - System.currentTimeMillis();

        if (timeRemaining <= 0) {
            return "Resetting soon...";
        }

        long hours = timeRemaining / (1000 * 60 * 60);
        long minutes = (timeRemaining % (1000 * 60 * 60)) / (1000 * 60);

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    private void checkAndResetIfNeeded(BoosterType type) {
        long lastReset = lastResetTime.get(type);
        long nextReset = getNextResetTime();
        long currentTime = System.currentTimeMillis();

        if (currentTime >= nextReset && lastReset < getPreviousResetTime()) {
            purchaseCounts.put(type, 0);
            lastResetTime.put(type, currentTime);
            saveBoosterData(type);
        }
    }

    private long getNextResetTime() {
        int resetHour = plugin.getConfigManager().getResetHour();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime nextReset = now.withHour(resetHour).withMinute(0).withSecond(0).withNano(0);

        if (now.isAfter(nextReset)) {
            nextReset = nextReset.plusDays(1);
        }

        return nextReset.toInstant().toEpochMilli();
    }

    private long getPreviousResetTime() {
        int resetHour = plugin.getConfigManager().getResetHour();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime previousReset = now.withHour(resetHour).withMinute(0).withSecond(0).withNano(0);

        if (now.isBefore(previousReset)) {
            previousReset = previousReset.minusDays(1);
        }

        return previousReset.toInstant().toEpochMilli();
    }

    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}