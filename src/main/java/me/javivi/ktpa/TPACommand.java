package me.javivi.ktpa;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPACommand implements CommandExecutor {
    private final KindlyTPA plugin;

    public TPACommand(KindlyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.colorize(plugin.getPrefix() + "&cThis command can only be used by players."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Send help message
            for (String line : plugin.getConfig().getStringList("messages.help")) {
                player.sendMessage(plugin.colorize(line));
            }
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.colorize(plugin.getPrefix() + plugin.getMessage("request.not-found")));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.colorize(plugin.getPrefix() + plugin.getMessage("request.self")));
            return true;
        }

        plugin.sendTpaRequest(player, target);
        return true;
    }
}