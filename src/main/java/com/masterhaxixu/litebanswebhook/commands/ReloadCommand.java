package com.masterhaxixu.litebanswebhook.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.masterhaxixu.litebanswebhook.LiteBansWebhook;

public class ReloadCommand implements CommandExecutor {
    private LiteBansWebhook plugin;
    public ReloadCommand(LiteBansWebhook pl) {
        plugin = pl;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.plugin.reloadConfig();
        this.plugin.reloadEmbeds();
        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Reloaded config successfully");
        return true;
    }
}
