package com.globalboosters.tasks;

import com.globalboosters.GlobalBoosters;
import org.bukkit.scheduler.BukkitRunnable;

public class BoosterTickTask extends BukkitRunnable {

    private final GlobalBoosters plugin;

    public BoosterTickTask(GlobalBoosters plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getBoosterManager().tickAllBoosters();
    }
}