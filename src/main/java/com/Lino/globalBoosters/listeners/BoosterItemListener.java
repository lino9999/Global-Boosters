package com.globalboosters.listeners;

import com.globalboosters.GlobalBoosters;
import com.globalboosters.boosters.BoosterType;
import com.globalboosters.gui.BoosterShopGUI;
import com.globalboosters.gui.ConfirmPurchaseGUI;
import com.globalboosters.items.BoosterItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class BoosterItemListener implements Listener {

    private final GlobalBoosters plugin;

    public BoosterItemListener(GlobalBoosters plugin) {
        this.plugin = plugin;
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
            player.sendMessage("§cYou don't have permission to use boosters!");
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

        if (event.getView().getTitle().equals("§6§lBooster Shop")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            if (event.getRawSlot() < event.getInventory().getSize()) {
                BoosterShopGUI gui = new BoosterShopGUI(plugin, player);
                gui.handleClick(event.getRawSlot());
            }
        } else if (event.getView().getTitle().equals("§6Confirm Purchase")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            if (event.getRawSlot() < event.getInventory().getSize()) {
                String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
                if (itemName.contains("CONFIRM") || itemName.contains("CANCEL")) {
                    ConfirmPurchaseGUI gui = new ConfirmPurchaseGUI(plugin, player, null, 0);
                    gui.handleClick(event.getRawSlot());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getBossBarManager().addPlayerToBossBars(event.getPlayer());
    }
}