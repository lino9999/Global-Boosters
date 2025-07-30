package com.Lino.globalBoosters.commands;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.gui.BoosterShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoostShopCommand implements CommandExecutor {

    private final GlobalBoosters plugin;

    public BoostShopCommand(GlobalBoosters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("globalboosters.shop")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        new BoosterShopGUI(plugin, player).open();
        return true;
    }
}