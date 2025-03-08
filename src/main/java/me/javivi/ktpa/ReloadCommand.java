package me.javivi.ktpa;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final KindlyTPA plugin;

    public ReloadCommand(KindlyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("kindlytpa.reload")) {
                    sender.sendMessage(plugin.colorize(plugin.getPrefix() + "&cYou don't have permission to use this command."));
                    return true;
                }

                // Reload the configuration
                plugin.reloadPluginConfig();
                
                // Send success message
                sender.sendMessage(plugin.colorize(plugin.getPrefix() + "&aConfiguration reloaded successfully!"));
                break;

            case "help":
                showHelp(sender);
                break;

            default:
                sender.sendMessage(plugin.colorize(plugin.getPrefix() + "&cUnknown subcommand. Use &e/ktpa help &cfor a list of commands."));
                break;
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.colorize("&b----- &eKindlyTPA Admin Commands &b-----"));
        if (sender.hasPermission("kindlytpa.reload")) {
            sender.sendMessage(plugin.colorize("&e/ktpa reload &7- Reload the plugin configuration"));
        }
        sender.sendMessage(plugin.colorize("&e/ktpa help &7- Show this help message"));
    }
}