package com.Lino.globalBoosters.listeners;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import com.Lino.globalBoosters.gui.BoosterShopGUI;
import com.Lino.globalBoosters.gui.ConfirmPurchaseGUI;
import com.Lino.globalBoosters.items.BoosterItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BoosterItemListener implements Listener {

    private final GlobalBoosters plugin;
    private final Map<Player, BoosterShopGUI> shopGUIs;
    private final Map<Player, ConfirmPurchaseGUI> confirmGUIs;

    public BoosterItemListener(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.shopGUIs = new HashMap<>();
        this.confirmGUIs = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!BoosterItem.isBoosterItem(item)) {
            return;
        }

        event.setCancelled(true);

        BoosterType type = BoosterItem.getBoosterType(item);
        int duration = BoosterItem.getBoosterDuration(item);

        if (type == null) {
            return;
        }

        if (!player.hasPermission("globalboosters.use")) {
            player.sendMessage(plugin.getMessagesManager().getMessage("general.no-permission-use"));
            return;
        }

        if (plugin.getBoosterManager().activateBooster(type, player, duration)) {
            item.setAmount(item.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getHolder() != null) {
            return;
        }

        if (shopGUIs.containsKey(player)) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            if (event.getRawSlot() < event.getInventory().getSize()) {
                BoosterShopGUI gui = shopGUIs.get(player);
                gui.handleClick(event.getRawSlot());
            }
        } else if (confirmGUIs.containsKey(player)) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            if (event.getRawSlot() < event.getInventory().getSize()) {
                ConfirmPurchaseGUI gui = confirmGUIs.get(player);
                gui.handleClick(event.getRawSlot());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            shopGUIs.remove(player);
            confirmGUIs.remove(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getBossBarManager().addPlayerToBossBars(event.getPlayer());
    }

    public void registerShopGUI(Player player, BoosterShopGUI gui) {
        shopGUIs.put(player, gui);
    }

    public void registerConfirmGUI(Player player, ConfirmPurchaseGUI gui) {
        confirmGUIs.put(player, gui);
    }

    public void unregisterGUIs(Player player) {
        shopGUIs.remove(player);
        confirmGUIs.remove(player);
    }
}