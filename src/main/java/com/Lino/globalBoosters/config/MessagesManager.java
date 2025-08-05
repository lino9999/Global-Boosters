package com.Lino.globalBoosters.config;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import com.Lino.globalBoosters.utils.GradientColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessagesManager {

    private final GlobalBoosters plugin;
    private final File messagesFile;
    private FileConfiguration messagesConfig;

    public MessagesManager(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        loadMessages();
    }

    private void loadMessages() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reload() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            return GradientColor.apply("<gradient:#FF0000:#FF6B6B>Message not found: " + key + "</gradient>");
        }
        return GradientColor.apply(message);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            return GradientColor.apply("<gradient:#FF0000:#FF6B6B>Message not found: " + key + "</gradient>");
        }

        // Replace placeholders BEFORE applying gradient
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String value = entry.getValue();
            // Strip any existing color codes from the value
            value = stripColorCodes(value);
            message = message.replace(entry.getKey(), value);
        }

        return GradientColor.apply(message);
    }

    public String getPrefix() {
        return getMessage("prefix");
    }

    public String getBoosterName(BoosterType type) {
        String key = "booster-names." + type.name().toLowerCase();
        return getMessage(key);
    }

    public String getBoosterNameRaw(BoosterType type) {
        String key = "booster-names." + type.name().toLowerCase();
        String name = messagesConfig.getString(key);
        if (name == null) {
            return type.getDisplayName();
        }
        // Extract only the text content without gradient tags
        return extractTextFromGradient(name);
    }

    private String extractTextFromGradient(String text) {
        // Remove gradient tags to get plain text
        return text.replaceAll("<gradient:#[A-Fa-f0-9]{6}:#[A-Fa-f0-9]{6}>", "")
                .replaceAll("</gradient>", "");
    }

    public String getRawMessage(String key) {
        return messagesConfig.getString(key);
    }

    private String stripColorCodes(String text) {
        if (text == null) return "";
        // Remove Minecraft color codes
        return text.replaceAll("§[0-9a-fk-orx]", "");
    }
}