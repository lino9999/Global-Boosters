package com.globalboosters.gui;

import com.globalboosters.GlobalBoosters;
import com.globalboosters.boosters.BoosterType;
import com.globalboosters.items.BoosterItem;
import com.globalboosters.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ConfirmPurchaseGUI {

    private final GlobalBoosters plugin;
    private final Player player;
    private final BoosterType boosterType;
    private final double price;
    private final Inventory inventory;

    public ConfirmPurchaseGUI(GlobalBoosters plugin, Player player, BoosterType boosterType, double price) {
        this.plugin = plugin;
        this.player = player;
        this.boosterType = boosterType;
        this.price = price;
        this.inventory = Bukkit.createInventory(null, 27, "§6Confirm Purchase");

        setupGUI();
    }

    private void setupGUI() {
        ItemStack grayGlass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("§7")
                .build();

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, grayGlass);
        }

        ItemStack confirmItem = new ItemBuilder(Material.LIME_WOOL)
                .setDisplayName("§a§lCONFIRM PURCHASE")
                .setLore(Arrays.asList(
                        "",
                        "§7Booster: §e" + boosterType.getDisplayName(),
                        "§7Price: §6$" + String.format("%.2f", price),
                        "",
                        "§aClick to purchase!"
                ))
                .build();

        ItemStack cancelItem = new ItemBuilder(Material.RED_WOOL)
                .setDisplayName("§c§lCANCEL")
                .setLore(Arrays.asList(
                        "",
                        "§7Return to shop",
                        "",
                        "§cClick to cancel!"
                ))
                .build();

        ItemStack infoItem = new ItemBuilder(boosterType.getIcon())
                .setDisplayName("§6" + boosterType.getDisplayName())
                .setLore(Arrays.asList(
                        "",
                        "§7This booster will be added",
                        "§7to your inventory as an item.",
                        "",
                        "§7Right-click the item to",
                        "§7activate the global booster!"
                ))
                .addGlow(true)
                .build();

        inventory.setItem(11, confirmItem);
        inventory.setItem(13, infoItem);
        inventory.setItem(15, cancelItem);
    }

    public void open() {
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
    }

    public void handleClick(int slot) {
        if (slot == 11) {
            purchaseBooster();
        } else if (slot == 15) {
            new BoosterShopGUI(plugin, player).open();
        }
    }

    private void purchaseBooster() {
        if (!plugin.getEconomy().has(player, price)) {
            player.sendMessage("§cYou don't have enough money!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.closeInventory();
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, price);

        ItemStack boosterItem = BoosterItem.createBoosterItem(
                boosterType,
                plugin.getConfigManager().getBoosterDuration(boosterType)
        );

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), boosterItem);
            player.sendMessage("§eYour inventory was full! The booster has been dropped on the ground.");
        } else {
            player.getInventory().addItem(boosterItem);
        }

        player.sendMessage("§aSuccessfully purchased " + boosterType.getDisplayName() + " for $" + String.format("%.2f", price));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.closeInventory();
    }

    public Inventory getInventory() {
        return inventory;
    }
}