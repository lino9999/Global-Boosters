package com.globalboosters.items;

import com.globalboosters.boosters.BoosterType;
import com.globalboosters.utils.ItemBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class BoosterItem {

    public static final NamespacedKey BOOSTER_TYPE_KEY = new NamespacedKey("globalboosters", "booster_type");
    public static final NamespacedKey BOOSTER_DURATION_KEY = new NamespacedKey("globalboosters", "booster_duration");

    public static ItemStack createBoosterItem(BoosterType type, int durationMinutes) {
        ItemStack item = new ItemBuilder(type.getIcon())
                .setDisplayName("§6§l" + type.getDisplayName())
                .setLore(Arrays.asList(
                        "",
                        "§7A magical booster that enhances",
                        "§7server-wide activities!",
                        "",
                        "§eMultiplier: §f" + type.getMultiplier() + "x",
                        "§eDuration: §f" + durationMinutes + " minutes",
                        "",
                        "§7Effects:",
                        getEffectDescription(type),
                        "",
                        "§a§lRIGHT-CLICK TO ACTIVATE"
                ))
                .addGlow(true)
                .build();

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(BOOSTER_TYPE_KEY, PersistentDataType.STRING, type.name());
        container.set(BOOSTER_DURATION_KEY, PersistentDataType.INTEGER, durationMinutes);
        item.setItemMeta(meta);

        return item;
    }

    public static boolean isBoosterItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(BOOSTER_TYPE_KEY, PersistentDataType.STRING);
    }

    public static BoosterType getBoosterType(ItemStack item) {
        if (!isBoosterItem(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String typeName = container.get(BOOSTER_TYPE_KEY, PersistentDataType.STRING);

        try {
            return BoosterType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static int getBoosterDuration(ItemStack item) {
        if (!isBoosterItem(item)) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(BOOSTER_DURATION_KEY, PersistentDataType.INTEGER, 30);
    }

    private static String getEffectDescription(BoosterType type) {
        switch (type) {
            case PLANT_GROWTH:
                return "§7• §fDoubles crop growth speed";
            case SPAWNER_RATE:
                return "§7• §fDoubles spawner spawn rate";
            case EXP_MULTIPLIER:
                return "§7• §fDoubles experience gained";
            case MOB_DROP:
                return "§7• §fDoubles mob drops";
            case MINING_SPEED:
                return "§7• §fIncreases mining speed";
            case FISHING_LUCK:
                return "§7• §fDoubles fishing rewards";
            case FARMING_FORTUNE:
                return "§7• §fDoubles crop drops";
            case COMBAT_DAMAGE:
                return "§7• §fIncreases combat damage";
            default:
                return "§7• §fEnhances server activities";
        }
    }
}