package com.Lino.globalBoosters;

import com.Lino.globalBoosters.commands.BoostShopCommand;
import com.Lino.globalBoosters.commands.BoosterCommand;
import com.Lino.globalBoosters.commands.GlobalBoostersCommand;
import com.Lino.globalBoosters.config.ConfigManager;
import com.Lino.globalBoosters.config.MessagesManager;
import com.Lino.globalBoosters.data.DataManager;
import com.Lino.globalBoosters.listeners.BoosterItemListener;
import com.Lino.globalBoosters.listeners.EffectBoosterListener;
import com.Lino.globalBoosters.listeners.FlyBoosterListener;
import com.Lino.globalBoosters.listeners.GameEventListener;
import com.Lino.globalBoosters.managers.BoosterManager;
import com.Lino.globalBoosters.managers.BossBarManager;
import com.Lino.globalBoosters.tasks.BoosterTickTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class GlobalBoosters extends JavaPlugin {

    private static GlobalBoosters instance;
    private Economy economy;
    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private DataManager dataManager;
    private BoosterManager boosterManager;
    private BossBarManager bossBarManager;
    private BoosterItemListener boosterItemListener;
    private EffectBoosterListener effectBoosterListener;
    private FlyBoosterListener flyBoosterListener;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Vault dependency not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initializeManagers();
        registerCommands();
        registerListeners();
        startTasks();

        getLogger().info("GlobalBoosters has been enabled!");
    }

    @Override
    public void onDisable() {
        if (boosterManager != null) {
            boosterManager.saveAllBoosters();
        }
        if (bossBarManager != null) {
            bossBarManager.removeAllBossBars();
        }

        getLogger().info("GlobalBoosters has been disabled!");
    }

    private void initializeManagers() {
        configManager = new ConfigManager(this);
        messagesManager = new MessagesManager(this);
        dataManager = new DataManager(this);
        boosterManager = new BoosterManager(this);
        bossBarManager = new BossBarManager(this);

        dataManager.loadActiveBoosters();
    }

    private void registerCommands() {
        getCommand("boostshop").setExecutor(new BoostShopCommand(this));
        getCommand("globalboosters").setExecutor(new GlobalBoostersCommand(this));

        BoosterCommand boosterCommand = new BoosterCommand(this);
        getCommand("booster").setExecutor(boosterCommand);
        getCommand("booster").setTabCompleter(boosterCommand);
    }

    private void registerListeners() {
        boosterItemListener = new BoosterItemListener(this);
        effectBoosterListener = new EffectBoosterListener(this);
        flyBoosterListener = new FlyBoosterListener(this);

        getServer().getPluginManager().registerEvents(boosterItemListener, this);
        getServer().getPluginManager().registerEvents(effectBoosterListener, this);
        getServer().getPluginManager().registerEvents(flyBoosterListener, this);
        getServer().getPluginManager().registerEvents(new GameEventListener(this), this);
    }

    private void startTasks() {
        new BoosterTickTask(this).runTaskTimer(this, 20L, 20L);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static GlobalBoosters getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public BoosterManager getBoosterManager() {
        return boosterManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public BoosterItemListener getBoosterItemListener() {
        return boosterItemListener;
    }

    public EffectBoosterListener getEffectBoosterListener() {
        return effectBoosterListener;
    }

    public FlyBoosterListener getFlyBoosterListener() {
        return flyBoosterListener;
    }
}