package com.masterhaxixu.litebanswebhook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    @Override
    public void onEnable() {
        saveDefaultConfig();
        JsonChecker.checkFiles(getDataFolder().getAbsolutePath());
        if (getConfig().getString("webhookurl").equals("WEBHOOK_URL")
                || getConfig().getString("webhookurl").isEmpty()) {
            this.getLogger().warning("No webhook provided in config.yml. Disabling...");
            this.getServer().getPluginManager().disablePlugin(this);
        }
        getLogger().info("Plugin Enabled!");
        registerEvents();
        this.getCommand("litebanswebhook").setExecutor(new ReloadCommand(this));
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
            String p = getPlayerName(entry.getUuid());
            String json;
            StringEntity params;
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost(getConfig().getString("webhookurl"));
            request.setHeader("Content-type", "application/json");
            switch (entry.getType()) {
                case "ban":
                    json = new String(Files.readAllBytes(Paths.get(getDataFolder().getAbsolutePath()+"/embeds/ban-remove.json")));
                    json = json.replace("PLAYER", p).replace("EXECUTOR", entry.getExecutorName()).replace("SERVER", entry.getServerOrigin());
                    if (entry.getReason() == null)
                        json = json.replace("REASON", "No Reason Provided");
                    else
                        json = json.replace("REASON", entry.getReason());
                    params = new StringEntity(json);
                    request.setEntity(params);
                    break;
                case "mute":
                    json = new String(Files.readAllBytes(Paths.get(getDataFolder().getAbsolutePath()+"/embeds/mute-remove.json")));
                    json = json.replace("PLAYER", p).replace("EXECUTOR", entry.getExecutorName()).replace("SERVER", entry.getServerOrigin());
                    if (entry.getReason() == null)
                        json = json.replace("REASON", "No Reason Provided");
                    else
                        json = json.replace("REASON", entry.getReason());
                    params = new StringEntity(json);
                    request.setEntity(params);
                    break;
                case "warn":
                    json = new String(Files.readAllBytes(Paths.get(getDataFolder().getAbsolutePath()+"/embeds/warn-remove.json")));
                    json = json.replace("PLAYER", p).replace("EXECUTOR", entry.getExecutorName()).replace("SERVER", entry.getServerOrigin());
                    if (entry.getReason() == null)
                        json = json.replace("REASON", "No Reason Provided");
                    else
                        json = json.replace("REASON", entry.getReason());
                    params = new StringEntity(json);
                    request.setEntity(params);
                    break;
            }

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
            String p = getPlayerName(entry.getUuid());
            String json;
            StringEntity params;
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost(getConfig().getString("webhookurl"));
            request.setHeader("Content-type", "application/json");
            switch (entry.getType()) {
                case "ban":
                    if (entry.isIpban()) {
                        json = new String(Files.readAllBytes(Paths.get(getDataFolder().getAbsolutePath()+"/embeds/ipban-added.json")));
                        json = json.replace("PLAYER", p).replace("EXECUTOR", entry.getExecutorName()).replace("SERVER", entry.getServerOrigin());
                        if (entry.getReason() == null)
                            json = json.replace("REASON", "No Reason Provided");
                        else
                            json = json.replace("REASON", entry.getReason());
                        if (!entry.isPermanent())
                            json = json.replace("DURATION", getDurationString(entry.getDuration()));
                        else
                            json = json.replace("DURATION", "");
                        params = new StringEntity(json);
                        request.setEntity(params);
                    } else {
                        json = new String(Files.readAllBytes(Paths.get(getDataFolder().getAbsolutePath()+"/embeds/ban-added.json")));
                        json = json.replace("PLAYER", p).replace("EXECUTOR", entry.getExecutorName()).replace("SERVER", entry.getServerOrigin());
                        if (entry.getReason() == null)
                            json = json.replace("REASON", "No Reason Provided");
                        else
                            json = json.replace("REASON", entry.getReason());
                        if (!entry.isPermanent())
                            json = json.replace("DURATION", getDurationString(entry.getDuration()));
                        else
                            json = json.replace("DURATION", "");
                        params = new StringEntity(json);
                        request.setEntity(params);

                    }
                    break;
                case "kick":
                    json = new String(Files.readAllBytes(Paths.get(getDataFolder().getAbsolutePath()+"/embeds/kick.json")));
                    json = json.replace("PLAYER", p).replace("EXECUTOR", entry.getExecutorName()).replace("REASON",
                            entry.getReason()).replace("SERVER", entry.getServerOrigin());
                    if (!entry.isPermanent())
                        json = json.replace("DURATION", getDurationString(entry.getDuration()));
                    else
                        json = json.replace("DURATION", "");
                    params = new StringEntity(json);
                    request.setEntity(params);
                    break;
                case "mute":
                    if (entry.isIpban()) {
                        json = new String(Files.readAllBytes(Paths.get(getDataFolder().getAbsolutePath()+"/embeds/ipmute-added.json")));
                        json = json.replace("PLAYER", p).replace("EXECUTOR", entry.getExecutorName()).replace("SERVER", entry.getServerOrigin());
                        if (entry.getReason() == null)
                            json = json.replace("REASON", "No Reason Provided");
                        else
                            json = json.replace("REASON", entry.getReason());
                        if (!entry.isPermanent())
                            json = json.replace("DURATION", getDurationString(entry.getDuration()));
                        else
                            json = json.replace("DURATION", "");
                        params = new StringEntity(json);
                        request.setEntity(params);
                    } else {
                        json = new String(Files.readAllBytes(Paths.get(getDataFolder().getAbsolutePath()+"/embeds/mute-added.json")));
                        json = json.replace("PLAYER", p).replace("EXECUTOR", entry.getExecutorName()).replace("SERVER", entry.getServerOrigin());
                        if (entry.getReason() == null)
                            json = json.replace("REASON", "No Reason Provided");
                        else
                            json = json.replace("REASON", entry.getReason());
                        if (!entry.isPermanent())
                            json = json.replace("DURATION", getDurationString(entry.getDuration()));
                        else
                            json = json.replace("DURATION", "");
                        params = new StringEntity(json);
                        request.setEntity(params);
                    }
                    break;
                case "warn":
                    json = new String(Files.readAllBytes(Paths.get(getDataFolder().getAbsolutePath()+"/embeds/warn-added.json")));
                    json = json.replace("PLAYER", p).replace("EXECUTOR", entry.getExecutorName()).replace("SERVER", entry.getServerOrigin());
                    if (entry.getReason() == null)
                        json = json.replace("REASON", "No Reason Provided");
                    else
                        json = json.replace("REASON", entry.getReason());
                    params = new StringEntity(json);
                    request.setEntity(params);
            }

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
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        duration -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        duration -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        duration -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);

        StringBuilder sb = new StringBuilder();
        sb.append(",duration `");
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
        sb.append("`");
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
