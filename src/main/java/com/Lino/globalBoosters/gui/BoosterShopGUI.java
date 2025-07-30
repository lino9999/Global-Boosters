package com.Lino.globalBoosters.gui;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import com.globalboosters.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class BoosterShopGUI {

    private final GlobalBoosters plugin;
    private final Player player;
    private final Inventory inventory;

    private static final int[] BOOSTER_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private static final int[] DECORATION_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35};

    public BoosterShopGUI(GlobalBoosters plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 36, "§6§lBooster Shop");

        setupGUI();
    }

    private void setupGUI() {
        fillDecoration();

        BoosterType[] types = BoosterType.values();
        for (int i = 0; i < types.length && i < BOOSTER_SLOTS.length; i++) {
            inventory.setItem(BOOSTER_SLOTS[i], createBoosterItem(types[i]));
        }
    }

    private void fillDecoration() {
        ItemStack decorationItem = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE)
                .setDisplayName("§7")
                .build();

        for (int slot : DECORATION_SLOTS) {
            inventory.setItem(slot, decorationItem);
        }
    }

    private ItemStack createBoosterItem(BoosterType type) {
        double price = plugin.getConfigManager().getBoosterPrice(type);
        int duration = plugin.getConfigManager().getBoosterDuration(type);
        boolean isActive = plugin.getBoosterManager().isBoosterActive(type);

        List<String> lore = Arrays.asList(
                "",
                "§7Multiplier: §e" + type.getMultiplier() + "x",
                "§7Duration: §e" + duration + " minutes",
                "§7Price: §6$" + String.format("%.2f", price),
                "",
                isActive ? "§c§lALREADY ACTIVE" : "§e§lCLICK TO PURCHASE"
        );

        return new ItemBuilder(type.getIcon())
                .setDisplayName("§6" + type.getDisplayName())
                .setLore(lore)
                .addGlow(!isActive)
                .build();
    }

    public void open() {
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
    }

    public void handleClick(int slot) {
        int boosterIndex = -1;
        for (int i = 0; i < BOOSTER_SLOTS.length; i++) {
            if (BOOSTER_SLOTS[i] == slot) {
                boosterIndex = i;
                break;
            }
        }

        if (boosterIndex == -1 || boosterIndex >= BoosterType.values().length) {
            return;
        }

        BoosterType type = BoosterType.values()[boosterIndex];

        if (plugin.getBoosterManager().isBoosterActive(type)) {
            player.sendMessage("§cThis booster is already active!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        double price = plugin.getConfigManager().getBoosterPrice(type);

        if (!plugin.getEconomy().has(player, price)) {
            player.sendMessage("§cYou don't have enough money! You need $" + String.format("%.2f", price));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        new ConfirmPurchaseGUI(plugin, player, type, price).open();
    }

    public Inventory getInventory() {
        return inventory;
    }
}