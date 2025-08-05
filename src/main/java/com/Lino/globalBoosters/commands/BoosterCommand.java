package com.Lino.globalBoosters.commands;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import com.Lino.globalBoosters.items.BoosterItem;
import com.Lino.globalBoosters.gui.BoosterShopGUI;
import com.Lino.globalBoosters.utils.GradientColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
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
            case "stats":
                return handleStats(sender);
            case "shop":
                return handleShop(sender);
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
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.booster.usage-give"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.booster.player-not-found"));
            return true;
        }

        BoosterType type;
        try {
            type = BoosterType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.booster.invalid-booster"));
            for (BoosterType boosterType : BoosterType.values()) {
                sender.sendMessage(GradientColor.apply("<gradient:#808080:#A9A9A9>- </gradient><gradient:#FFA500:#FFD700>" + boosterType.name().toLowerCase() + "</gradient>"));
            }
            return true;
        }

        int duration = plugin.getConfigManager().getBoosterDuration(type);
        if (args.length >= 4) {
            try {
                duration = Integer.parseInt(args[3]);
                if (duration <= 0) {
                    sender.sendMessage(plugin.getMessagesManager().getMessage("commands.booster.duration-positive"));
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("commands.booster.invalid-duration"));
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
        placeholders.put("%booster%", plugin.getMessagesManager().getBoosterNameRaw(type));
        placeholders.put("%player%", target.getName());
        placeholders.put("%duration%", String.valueOf(duration));

        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.booster.gave-booster", placeholders));
        target.sendMessage(plugin.getMessagesManager().getMessage("commands.booster.received-booster", placeholders));

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

    private boolean handleShop(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("globalboosters.shop")) {
            player.sendMessage(plugin.getMessagesManager().getMessage("general.no-permission"));
            return true;
        }

        new BoosterShopGUI(plugin, player).open();
        return true;
    }

    private boolean handleStats(CommandSender sender) {
        if (!sender.hasPermission("globalboosters.admin.stats")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("general.no-permission"));
            return true;
        }

        sender.sendMessage("");
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.stats.header"));
        sender.sendMessage("");

        ResultSet rs = plugin.getDataManager().getTopBoosters(10);
        if (rs != null) {
            try {
                int position = 1;
                while (rs.next()) {
                    String boosterType = rs.getString("booster_type");
                    int usageCount = rs.getInt("usage_count");

                    if (usageCount > 0) {
                        try {
                            BoosterType type = BoosterType.valueOf(boosterType);
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%position%", String.valueOf(position));
                            placeholders.put("%booster%", plugin.getMessagesManager().getBoosterNameRaw(type));
                            placeholders.put("%count%", String.valueOf(usageCount));

                            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.stats.entry", placeholders));
                            position++;
                        } catch (IllegalArgumentException e) {
                            continue;
                        }
                    }
                }

                if (position == 1) {
                    sender.sendMessage(plugin.getMessagesManager().getMessage("commands.stats.no-data"));
                }

                rs.close();
            } catch (SQLException e) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("commands.stats.error"));
                e.printStackTrace();
            }
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.stats.error"));
        }

        sender.sendMessage("");
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.stats.footer"));

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.booster.help-header"));
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.booster-shop"));
        if (sender.hasPermission("globalboosters.admin.give")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.booster-give"));
        }
        if (sender.hasPermission("globalboosters.admin.reload")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.booster-reload"));
        }
        if (sender.hasPermission("globalboosters.admin.stats")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.booster-stats"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("shop");
            if (sender.hasPermission("globalboosters.admin.give")) {
                subCommands.add("give");
            }
            if (sender.hasPermission("globalboosters.admin.reload")) {
                subCommands.add("reload");
            }
            if (sender.hasPermission("globalboosters.admin.stats")) {
                subCommands.add("stats");
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