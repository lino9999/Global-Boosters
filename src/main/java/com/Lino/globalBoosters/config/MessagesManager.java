package com.Lino.globalBoosters.config;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.utils.GradientColor;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MessagesManager {

    private final GlobalBoosters plugin;
    private final File messagesFile;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messages;

    public MessagesManager(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.messages = new HashMap<>();

        loadMessages();
    }

    private void loadMessages() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        loadAllMessages();
    }

    private void loadAllMessages() {
        for (String key : messagesConfig.getKeys(true)) {
            if (!messagesConfig.isConfigurationSection(key)) {
                messages.put(key, colorize(messagesConfig.getString(key)));
            }
        }
    }

    public void reload() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        messages.clear();
        loadAllMessages();
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "&cMessage not found: " + key);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }

        return message;
    }

    public String getPrefix() {
        return getMessage("prefix");
    }

    private String colorize(String message) {
        // First apply gradient colors
        message = GradientColor.apply(message);
        // Then apply standard color codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}