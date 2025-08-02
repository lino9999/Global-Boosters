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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
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
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK &&
                event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
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

        if (plugin.getBoosterManager().isBoosterActive(type)) {
            player.sendMessage(plugin.getMessagesManager().getMessage("booster.already-active"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int maxActive = plugin.getConfigManager().getMaxActiveBoosters();
        if (maxActive != -1 && plugin.getBoosterManager().getActiveBoosterCount() >= maxActive) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%max%", String.valueOf(maxActive));
            player.sendMessage(plugin.getMessagesManager().getMessage("booster.max-active-reached", placeholders));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
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

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (clickedItem != null && BoosterItem.isBoosterItem(clickedItem)) {
            if (event.getSlotType() == InventoryType.SlotType.ARMOR ||
                    event.getSlotType() == InventoryType.SlotType.QUICKBAR && event.getSlot() == 40) {
                event.setCancelled(true);
                return;
            }
        }

        if (cursorItem != null && BoosterItem.isBoosterItem(cursorItem)) {
            if (event.getSlotType() == InventoryType.SlotType.ARMOR ||
                    event.getSlotType() == InventoryType.SlotType.QUICKBAR && event.getSlot() == 40) {
                event.setCancelled(true);
                return;
            }

            InventoryType invType = event.getInventory().getType();
            if (invType == InventoryType.CRAFTING || invType == InventoryType.WORKBENCH ||
                    invType == InventoryType.ANVIL || invType == InventoryType.ENCHANTING ||
                    invType == InventoryType.SMITHING || invType == InventoryType.GRINDSTONE ||
                    invType == InventoryType.STONECUTTER || invType == InventoryType.LOOM ||
                    invType == InventoryType.CARTOGRAPHY || invType == InventoryType.BREWING ||
                    invType == InventoryType.FURNACE || invType == InventoryType.BLAST_FURNACE ||
                    invType == InventoryType.SMOKER) {
                if (event.getRawSlot() < event.getInventory().getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (event.isShiftClick() && clickedItem != null && BoosterItem.isBoosterItem(clickedItem)) {
            InventoryType topType = player.getOpenInventory().getTopInventory().getType();
            if (topType != InventoryType.PLAYER && topType != InventoryType.CREATIVE &&
                    topType != InventoryType.CHEST && topType != InventoryType.ENDER_CHEST &&
                    topType != InventoryType.SHULKER_BOX && topType != InventoryType.BARREL) {
                event.setCancelled(true);
                return;
            }
        }

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
        plugin.getEffectBoosterListener().applyActiveEffects(event.getPlayer());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (!BoosterItem.isBoosterItem(event.getOldCursor())) {
            return;
        }

        InventoryType invType = event.getInventory().getType();
        if (invType == InventoryType.CRAFTING || invType == InventoryType.WORKBENCH ||
                invType == InventoryType.ANVIL || invType == InventoryType.ENCHANTING ||
                invType == InventoryType.SMITHING || invType == InventoryType.GRINDSTONE ||
                invType == InventoryType.STONECUTTER || invType == InventoryType.LOOM ||
                invType == InventoryType.CARTOGRAPHY || invType == InventoryType.BREWING ||
                invType == InventoryType.FURNACE || invType == InventoryType.BLAST_FURNACE ||
                invType == InventoryType.SMOKER) {

            for (int slot : event.getRawSlots()) {
                if (slot < event.getInventory().getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        for (int slot : event.getRawSlots()) {
            if (slot >= 5 && slot <= 8) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (BoosterItem.isBoosterItem(event.getMainHandItem()) ||
                BoosterItem.isBoosterItem(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (BoosterItem.isBoosterItem(event.getPlayerItem())) {
            event.setCancelled(true);
        }
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