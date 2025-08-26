package com.Lino.globalBoosters.commands;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.ActiveBooster;
import com.Lino.globalBoosters.boosters.BoosterType;
import com.Lino.globalBoosters.config.ConfigManager;
import com.Lino.globalBoosters.utils.GradientColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

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

        if (args[0].equalsIgnoreCase("schedule")) {
            sendScheduleInfo(sender);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.header"));
        sender.sendMessage("");
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.boostshop"));
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.globalboosters-help"));
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.globalboosters-list"));

        if (sender.hasPermission("globalboosters.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.globalboosters-schedule"));
        }

        if (sender.hasPermission("globalboosters.admin")) {
            sender.sendMessage("");
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.admin-header"));
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.booster-give"));
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.booster-reload"));
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.booster-stats"));
        }

        sender.sendMessage("");
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.available-boosters"));
        sender.sendMessage("");

        for (BoosterType type : BoosterType.values()) {
            String boosterName = plugin.getMessagesManager().getBoosterName(type);
            String multiplier = "";
            if (!type.isEffectBooster() && !isNoMultiplierBooster(type)) {
                multiplier = GradientColor.apply(" <gradient:#808080:#A9A9A9>(" + plugin.getConfigManager().getBoosterMultiplier(type) + "x)</gradient>");
            }
            String typeName = GradientColor.apply("<gradient:#FFA500:#FFD700>" + type.name().toLowerCase() + "</gradient>");
            String separator = GradientColor.apply("<gradient:#808080:#A9A9A9> - </gradient>");
            sender.sendMessage(typeName + separator + boosterName + multiplier);
        }

        sender.sendMessage("");
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help.footer"));
    }

    private void sendActiveBoostersList(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.list.header"));
        sender.sendMessage("");

        if (plugin.getBoosterManager().getActiveBoosters().isEmpty()) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.list.no-active"));
        } else {
            for (ActiveBooster booster : plugin.getBoosterManager().getActiveBoosters()) {
                String boosterName = plugin.getMessagesManager().getBoosterName(booster.getType());
                sender.sendMessage(boosterName);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%player%", booster.getActivatorName());
                placeholders.put("%time%", booster.getTimeRemaining());

                sender.sendMessage("  " + plugin.getMessagesManager().getMessage("commands.list.activated-by", placeholders));
                sender.sendMessage("  " + plugin.getMessagesManager().getMessage("commands.list.time-remaining", placeholders));
                sender.sendMessage("");
            }
        }

        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.list.footer"));
    }

    private void sendScheduleInfo(CommandSender sender) {
        if (!sender.hasPermission("globalboosters.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("general.no-permission"));
            return;
        }

        sender.sendMessage("");
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.schedule.header"));
        sender.sendMessage("");

        if (!plugin.getConfigManager().isScheduledBoostersEnabled()) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.schedule.disabled"));
            sender.sendMessage("");
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.schedule.footer"));
            return;
        }

        String timezoneStr = plugin.getConfigManager().getScheduledBoostersTimezone();
        ZoneId timezone;

        try {
            timezone = ZoneId.of(timezoneStr);
        } catch (Exception e) {
            timezone = ZoneId.systemDefault();
            timezoneStr = timezone.getId();
        }

        ZonedDateTime zonedNow = ZonedDateTime.now(timezone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String currentTime = zonedNow.format(formatter);
        String currentDay = zonedNow.getDayOfWeek().toString();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%timezone%", timezoneStr);
        placeholders.put("%time%", currentTime);
        placeholders.put("%day%", currentDay);

        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.schedule.current-info", placeholders));
        sender.sendMessage("");

        if (plugin.getConfigManager().getScheduledBoosters().isEmpty()) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.schedule.no-schedules"));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.schedule.list-header"));
            for (ConfigManager.ScheduledBooster schedule : plugin.getConfigManager().getScheduledBoosters()) {
                placeholders.clear();
                placeholders.put("%booster%", plugin.getMessagesManager().getBoosterNameRaw(schedule.getType()));
                placeholders.put("%hour%", String.format("%02d", schedule.getHour()));
                placeholders.put("%minute%", String.format("%02d", schedule.getMinute()));
                placeholders.put("%duration%", String.valueOf(schedule.getDuration()));

                StringBuilder daysStr = new StringBuilder();
                for (DayOfWeek day : schedule.getDays()) {
                    if (daysStr.length() > 0) daysStr.append(", ");
                    daysStr.append(day.toString().substring(0, 3));
                }
                placeholders.put("%days%", daysStr.toString());

                sender.sendMessage(plugin.getMessagesManager().getMessage("commands.schedule.entry", placeholders));
            }
        }

        sender.sendMessage("");
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.schedule.footer"));
    }

    private boolean isNoMultiplierBooster(BoosterType type) {
        switch (type) {
            case NO_FALL_DAMAGE:
            case KEEP_INVENTORY:
            case FLY:
            case PLANT_GROWTH:
                return true;
            default:
                return false;
        }
    }
}