package com.Lino.globalBoosters.commands;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import com.Lino.globalBoosters.items.BoosterItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class BoosterCommand implements CommandExecutor, TabCompleter {

    private final GlobalBoosters plugin;

    public BoosterCommand(GlobalBoosters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give":
                return handleGive(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("globalboosters.admin.give")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("general.no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /booster give <player> <booster_type> [duration]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        BoosterType type;
        try {
            type = BoosterType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid booster type! Available types:");
            for (BoosterType boosterType : BoosterType.values()) {
                sender.sendMessage("§7- §e" + boosterType.name().toLowerCase());
            }
            return true;
        }

        int duration = plugin.getConfigManager().getBoosterDuration(type);
        if (args.length >= 4) {
            try {
                duration = Integer.parseInt(args[3]);
                if (duration <= 0) {
                    sender.sendMessage("§cDuration must be a positive number!");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid duration! Must be a number.");
                return true;
            }
        }

        ItemStack boosterItem = BoosterItem.createBoosterItem(type, duration);

        if (target.getInventory().firstEmpty() == -1) {
            target.getWorld().dropItem(target.getLocation(), boosterItem);
            target.sendMessage(plugin.getMessagesManager().getMessage("purchase.inventory-full"));
        } else {
            target.getInventory().addItem(boosterItem);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%booster%", type.getDisplayName());
        placeholders.put("%player%", target.getName());
        placeholders.put("%duration%", String.valueOf(duration));

        sender.sendMessage("§aGave " + target.getName() + " a " + type.getDisplayName() + " for " + duration + " minutes!");
        target.sendMessage("§aYou received a " + type.getDisplayName() + " for " + duration + " minutes!");

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("globalboosters.admin.reload")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("general.no-permission"));
            return true;
        }

        plugin.reloadConfig();
        plugin.getConfigManager().reload();
        plugin.getMessagesManager().reload();

        sender.sendMessage(plugin.getMessagesManager().getMessage("general.reload-success"));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lGlobalBoosters Commands:");
        if (sender.hasPermission("globalboosters.admin.give")) {
            sender.sendMessage("§e/booster give <player> <booster_type> [duration] §7- Give a booster to a player");
        }
        if (sender.hasPermission("globalboosters.admin.reload")) {
            sender.sendMessage("§e/booster reload §7- Reload configuration files");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            if (sender.hasPermission("globalboosters.admin.give")) {
                subCommands.add("give");
            }
            if (sender.hasPermission("globalboosters.admin.reload")) {
                subCommands.add("reload");
            }
            return filterStartingWith(subCommands, args[0]);
        }

        if (args[0].equalsIgnoreCase("give") && sender.hasPermission("globalboosters.admin.give")) {
            if (args.length == 2) {
                return filterStartingWith(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()), args[1]);
            } else if (args.length == 3) {
                return filterStartingWith(Arrays.stream(BoosterType.values())
                        .map(type -> type.name().toLowerCase())
                        .collect(Collectors.toList()), args[2]);
            } else if (args.length == 4) {
                return Arrays.asList("30", "60", "120");
            }
        }

        return new ArrayList<>();
    }

    private List<String> filterStartingWith(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}