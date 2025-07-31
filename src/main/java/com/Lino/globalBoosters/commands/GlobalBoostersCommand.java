package com.Lino.globalBoosters.commands;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.ActiveBooster;
import com.Lino.globalBoosters.boosters.BoosterType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GlobalBoostersCommand implements CommandExecutor {

    private final GlobalBoosters plugin;

    public GlobalBoostersCommand(GlobalBoosters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sendActiveBoostersList(sender);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§6§l==== GlobalBoosters Help ====");
        sender.sendMessage("");
        sender.sendMessage("§e/boostshop §7- Open the booster shop");
        sender.sendMessage("§e/globalboosters help §7- Show this help message");
        sender.sendMessage("§e/globalboosters list §7- List all active boosters");

        if (sender.hasPermission("globalboosters.admin")) {
            sender.sendMessage("");
            sender.sendMessage("§c§lAdmin Commands:");
            sender.sendMessage("§e/booster give <player> <type> [duration] §7- Give a booster");
            sender.sendMessage("§e/booster reload §7- Reload configuration");
        }

        sender.sendMessage("");
        sender.sendMessage("§6§lAvailable Boosters:");
        sender.sendMessage("");

        for (BoosterType type : BoosterType.values()) {
            String multiplier = type.isEffectBooster() ? "" : " §7(" + plugin.getConfigManager().getBoosterMultiplier(type) + "x)";
            sender.sendMessage("§e" + type.name().toLowerCase() + " §7- " + type.getDisplayName() + multiplier);
        }

        sender.sendMessage("");
        sender.sendMessage("§6§l=========================");
    }

    private void sendActiveBoostersList(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§6§l==== Active Boosters ====");
        sender.sendMessage("");

        if (plugin.getBoosterManager().getActiveBoosters().isEmpty()) {
            sender.sendMessage("§cNo boosters are currently active!");
        } else {
            for (ActiveBooster booster : plugin.getBoosterManager().getActiveBoosters()) {
                sender.sendMessage("§e" + booster.getType().getDisplayName());
                sender.sendMessage("  §7Activated by: §f" + booster.getActivatorName());
                sender.sendMessage("  §7Time remaining: §f" + booster.getTimeRemaining());
                sender.sendMessage("");
            }
        }

        sender.sendMessage("§6§l========================");
    }
}