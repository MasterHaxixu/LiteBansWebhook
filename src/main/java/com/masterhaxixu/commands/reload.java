package com.masterhaxixu.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.masterhaxixu.Main;

public class reload implements CommandExecutor {
    private Main plugin;
    public reload(Main pl) {
        plugin = pl;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
        Bukkit.getServer().getPluginManager().enablePlugin(plugin);
        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Reloaded plugin successfully");
        return true;
    }
}
