package com.Lino.globalBoosters.gui;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import com.Lino.globalBoosters.items.BoosterItem;
import com.Lino.globalBoosters.listeners.BoosterItemListener;
import com.Lino.globalBoosters.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        this.inventory = Bukkit.createInventory(null, 27, plugin.getMessagesManager().getMessage("shop.confirm-title"));

        setupGUI();
    }

    private void setupGUI() {
        ItemStack grayGlass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("§7")
                .build();

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, grayGlass);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%booster%", boosterType.getDisplayName());
        placeholders.put("%price%", String.format("%.2f", price));

        ItemStack confirmItem = new ItemBuilder(Material.LIME_WOOL)
                .setDisplayName(plugin.getMessagesManager().getMessage("shop.confirm.button"))
                .setLore(Arrays.asList(
                        "",
                        plugin.getMessagesManager().getMessage("shop.confirm.lore-booster", placeholders),
                        plugin.getMessagesManager().getMessage("shop.confirm.lore-price", placeholders),
                        "",
                        plugin.getMessagesManager().getMessage("shop.confirm.click-confirm")
                ))
                .build();

        ItemStack cancelItem = new ItemBuilder(Material.RED_WOOL)
                .setDisplayName(plugin.getMessagesManager().getMessage("shop.confirm.cancel"))
                .setLore(Arrays.asList(
                        "",
                        plugin.getMessagesManager().getMessage("shop.confirm.return-shop"),
                        "",
                        plugin.getMessagesManager().getMessage("shop.confirm.click-cancel")
                ))
                .build();

        ItemStack infoItem = new ItemBuilder(boosterType.getIcon())
                .setDisplayName(plugin.getMessagesManager().getMessage("shop.confirm.info-title", placeholders))
                .setLore(Arrays.asList(
                        "",
                        plugin.getMessagesManager().getMessage("shop.confirm.info-lore.line1"),
                        plugin.getMessagesManager().getMessage("shop.confirm.info-lore.line2"),
                        plugin.getMessagesManager().getMessage("shop.confirm.info-lore.line3"),
                        plugin.getMessagesManager().getMessage("shop.confirm.info-lore.line4"),
                        plugin.getMessagesManager().getMessage("shop.confirm.info-lore.line5")
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

        BoosterItemListener listener = getBoosterItemListener();
        if (listener != null) {
            listener.unregisterGUIs(player);
            listener.registerConfirmGUI(player, this);
        }
    }

    public void handleClick(int slot) {
        if (slot == 11) {
            purchaseBooster();
        } else if (slot == 15) {
            player.closeInventory();
            new BoosterShopGUI(plugin, player).open();
        }
    }

    private void purchaseBooster() {
        if (!plugin.getEconomy().has(player, price)) {
            player.sendMessage(plugin.getMessagesManager().getMessage("purchase.not-enough-money",
                    new HashMap<String, String>() {{ put("%price%", String.format("%.2f", price)); }}));
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
            player.sendMessage(plugin.getMessagesManager().getMessage("purchase.inventory-full"));
        } else {
            player.getInventory().addItem(boosterItem);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%booster%", boosterType.getDisplayName());
        placeholders.put("%price%", String.format("%.2f", price));

        player.sendMessage(plugin.getMessagesManager().getMessage("purchase.success", placeholders));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.closeInventory();
    }

    public Inventory getInventory() {
        return inventory;
    }

    private BoosterItemListener getBoosterItemListener() {
        return plugin.getBoosterItemListener();
    }
}