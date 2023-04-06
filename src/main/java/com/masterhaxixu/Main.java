package com.masterhaxixu;

import java.awt.Color;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.masterhaxixu.commands.reload;

import litebans.api.Database;
import litebans.api.Entry;
import litebans.api.Events;

public class Main extends JavaPlugin implements Listener {

    FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        Bukkit.getLogger().info("LiteBans Webhook Relay loaded! (by Master Haxixu#1602)");
        registerEvents();
        this.getCommand("litebanswebhook").setExecutor(new reload(this));
        getServer().getPluginManager().registerEvents(this, this);

    }

    public void registerEvents() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryRemoved(Entry entry) {
                String p = getPlayerName(entry.getUuid());
                DiscordWebhook webhook = new DiscordWebhook(config.getString("webhookurl"));
                String desc;
                switch (entry.getType()) {
                    case "ban":
                        desc = "The player `" + p + "` was unbanned by `" + entry.getExecutorName()
                                + "` for `" + entry.getReason() + "`";
                        break;
                    case "mute":
                        desc = "The player `" + p + "` was unmuted by `" + entry.getExecutorName()
                                + "` for `" + entry.getReason() + "`";
                        break;
                    default:
                        desc = "";
                }
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle("Punishment Revoked ✅")
                        .setDescription(desc)
                        .setColor(new Color(50,205,50))
                        .setFooter("Powered by LiteBans Webhook", null));
                Thread thread = new Thread(() -> {
                    try {
                        webhook.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Bukkit.getLogger().warning("Please check that the server has access to an internet connection and the webhook url is valid");
                    }
                });

                thread.start();

            }

            @Override
            public void entryAdded(Entry entry) {
                String p = getPlayerName(entry.getUuid());
                String desc;
                java.awt.Color color;
                DiscordWebhook webhook = new DiscordWebhook(config.getString("webhookurl"));
                switch (entry.getType()) {
                    case "ban":
                    color = java.awt.Color.RED;
                        if (entry.isIpban()) {
                            if (entry.isPermanent()) {
                                desc = "The player `" + p + "` was ip-banned by `" + entry.getExecutorName()
                                        + "` for `" + entry.getReason() + "`";
                            } else {
                                desc = "The player `" + p + "` was temporarily ip-banned by `"
                                        + entry.getExecutorName() + "` for `" + entry.getReason() + "`,duration `"
                                        + getDurationString(entry.getDuration()) + "`";
                            }
                        } else {
                            if (entry.isPermanent()) {
                                desc = "The player `" + p + "` was banned by `" + entry.getExecutorName()
                                        + "` for `" + entry.getReason() + "`";
                            } else {
                                desc = "The player `" + p + "` was temporarily banned by `"
                                        + entry.getExecutorName() + "` for `" + entry.getReason() + "`,duration `"
                                        + getDurationString(entry.getDuration()) + "`";
                            }
                        }
                        break;
                    case "mute":
                    color = new Color(135,206,250);
                        if (entry.isIpban()) {
                            if (entry.isPermanent()) {
                                desc = "The player `" + p + "` was ip-muted by `" + entry.getExecutorName()
                                        + "` for `" + entry.getReason() + "`";
                            } else {
                                desc = "The player `" + p + "` was temporarily ip-muted by `"
                                        + entry.getExecutorName() + "` for `" + entry.getReason() + "`,duration `"
                                        + getDurationString(entry.getDuration()) + "`";
                            }
                        } else {
                            if (entry.isPermanent()) {
                                desc = "The player `" + p + "` was muted by `" + entry.getExecutorName()
                                        + "` for `" + entry.getReason() + "`";
                            } else {
                                desc = "The player `" + p + "` was temporarily muted by `"
                                        + entry.getExecutorName() + "` for `" + entry.getReason() + "`,duration `"
                                        + getDurationString(entry.getDuration()) + "`";
                            }
                        }
                        break;
                    default:
                        desc = "";
                        color = java.awt.Color.RED;
                }
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle("New Punishment ⏲️")
                        .setDescription(desc)
                        .setColor(color)
                        .setFooter("Powered by LiteBans Webhook", null));

                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        webhook.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Bukkit.getLogger().warning("Please check that the server has access to an internet connection and the webhook url is valid");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                thread.start();
            }

        });

    }

    public static String getDurationString(long duration) {
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        duration -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        duration -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        duration -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days);
            sb.append(" day");
            if (days != 1) {
                sb.append("s");
            }
            // sb.append(" ");
        }
        if (hours > 0) {
            sb.append(hours);
            sb.append(" hour");
            if (hours != 1) {
                sb.append("s");
            }
            // sb.append(" ");
        }
        if (minutes > 0) {
            sb.append(minutes);
            sb.append(" minute");
            if (minutes != 1) {
                sb.append("s");
            }
            // sb.append(" ");
        }
        if (seconds > 0) {
            sb.append(seconds);
            sb.append(" second");
            if (seconds != 1) {
                sb.append("s");
            }
            // sb.append(" ");
        }

        if (sb.length() == 0) {
            sb.append("0 seconds");
        }

        return sb.toString();
    }

    private String getPlayerName(String uuid) {
        try {
            PreparedStatement stmt = Database.get()
                    .prepareStatement("SELECT name FROM {history} WHERE uuid = ? ORDER BY id DESC LIMIT 1");
            try {
                stmt.setString(1, uuid);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String str = rs.getString(1);
                    if (stmt != null)
                        stmt.close();
                    return str;
                }
                if (stmt != null)
                    stmt.close();
            } catch (Throwable throwable) {
                if (stmt != null)
                    try {
                        stmt.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
