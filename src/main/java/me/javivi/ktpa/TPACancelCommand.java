package me.javivi.ktpa;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPACancelCommand implements CommandExecutor {
    private final KindlyTPA plugin;

    public TPACancelCommand(KindlyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.colorize(plugin.getPrefix() + "&cThis command can only be used by players."));
            return true;
        }

        Player player = (Player) sender;
        plugin.cancelTpaRequest(player);
        return true;
    }
}