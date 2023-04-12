package com.masterhaxixu.litebanswebhook;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.masterhaxixu.litebanswebhook.commands.ReloadCommand;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import litebans.api.Database;
import litebans.api.Entry;
import litebans.api.Events;

public class LiteBansWebhook extends JavaPlugin {
    private List<String> punishmentTypes = Arrays.asList("ban-added", "ban-removed", "ban-ip-added", "mute-ip-added", "kick-added", "mute-added", "mute-removed", "warn-added", "warn-removed");
    private Map<String, String> embeds = new HashMap<>();
    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadEmbeds();
        if (getConfig().getString("webhookurl").equals("WEBHOOK_URL")
                || getConfig().getString("webhookurl").isEmpty()) {
            this.getLogger().warning("No webhook provided in config.yml. Disabling...");
            this.getServer().getPluginManager().disablePlugin(this);
        }
        registerEvents();
        this.getCommand("litebanswebhook").setExecutor(new ReloadCommand(this));
    }

    public void reloadEmbeds() {
        embeds.clear();
        for (String fileName : punishmentTypes) {
            File file = new File(this.getDataFolder(), "embeds/" + fileName + ".json");
            if (!file.exists()) {
                this.saveResource("embeds/" + fileName + ".json", false);
            }
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                embeds.put(fileName, content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private String parseString(Entry entry, String message) {
        return message.replaceAll("\\$\\{entry\\.id\\}", String.valueOf(entry.getId()))
                .replaceAll("\\$\\{entry\\.type\\}", entry.getType())
                .replaceAll("\\$\\{entry\\.name\\}", getPlayerName(entry.getUuid()))
                .replaceAll("\\$\\{entry\\.uuid\\}", entry.getUuid())
                .replaceAll("\\$\\{entry\\.ip\\}", entry.getIp())
                .replaceAll("\\$\\{entry\\.reason\\}", entry.getReason())
                .replaceAll("\\$\\{entry\\.executorUUID\\}", entry.getExecutorUUID())
                .replaceAll("\\$\\{entry\\.executorName\\}", entry.getExecutorName())
                .replaceAll("\\$\\{entry\\.removedByUUID\\}", entry.getRemovedByUUID())
                .replaceAll("\\$\\{entry\\.removedByName\\}", entry.getRemovedByName())
                .replaceAll("\\$\\{entry\\.removalReason\\}", entry.getRemovalReason())
                .replaceAll("\\$\\{entry\\.dateStart\\}", String.valueOf(entry.getDateStart()))
                .replaceAll("\\$\\{entry\\.dateEnd\\}", String.valueOf(entry.getDateEnd()))
                .replaceAll("\\$\\{entry\\.duration\\}", getDurationString(entry.getDuration()))
                .replaceAll("\\$\\{entry\\.serverScope\\}", entry.getServerScope())
                .replaceAll("\\$\\{entry\\.serverOrigin\\}", entry.getServerOrigin())
                .replaceAll("\\$\\{entry\\.silent\\}", String.valueOf(entry.isSilent()))
                .replaceAll("\\$\\{entry\\.ipban\\}", String.valueOf(entry.isIpban()))
                .replaceAll("\\$\\{entry\\.active\\}", String.valueOf(entry.isActive()));
    }

    public void registerEvents() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryRemoved(Entry entry) {
                LiteBansWebhook.this.entryRemoved(entry);
            }

            @Override
            public void entryAdded(Entry entry) {
                LiteBansWebhook.this.entryAdded(entry);
            }
        });
    }

    private void entryRemoved(Entry entry) {
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost(getConfig().getString("webhookurl"));
            request.setHeader("Content-type", "application/json");
            request.setEntity(new StringEntity(parseString(entry, embeds.get(entry.getType().toLowerCase() + "-removed"))));
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    httpClient.execute(request);
                } catch (IOException e) {
                    this.getLogger().severe("Failed to send notification. Is the webhook valid?");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void entryAdded(Entry entry) {
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost(getConfig().getString("webhookurl"));
            request.setHeader("Content-type", "application/json");
            request.setEntity(new StringEntity(parseString(entry, embeds.get(entry.getType().toLowerCase() + (entry.isIpban() ? "-ip" : "") + "-added"))));
            
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    httpClient.execute(request);
                } catch (IOException e) {
                    this.getLogger().severe("Failed to send notification. Is the webhook valid?");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDurationString(long duration) {
        if (duration == -1) {
            return "Permanent";
        }
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
        }
        if (hours > 0) {
            sb.append(hours);
            sb.append(" hour");
            if (hours != 1) {
                sb.append("s");
            }
        }
        if (minutes > 0) {
            sb.append(minutes);
            sb.append(" minute");
            if (minutes != 1) {
                sb.append("s");
            }
        }
        if (seconds > 0) {
            sb.append(seconds);
            sb.append(" second");
            if (seconds != 1) {
                sb.append("s");
            }
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
