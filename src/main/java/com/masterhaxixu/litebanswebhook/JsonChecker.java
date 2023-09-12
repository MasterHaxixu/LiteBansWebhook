package com.masterhaxixu.litebanswebhook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Bukkit;

public class JsonChecker {

    public static void checkFiles(String dataFolder) {
        String[] fileNames = {
            "ban-added", "ban-remove", "ipban-added", "ipmute-added",
            "kick", "mute-added", "mute-remove", "warn-added", "warn-remove"
        };

        String[] jsonValues = {
            "{\n\"embeds\":[{\n\"title\":\"New Punishment (SERVER) :timer:\",\n\"description\":\"The player `PLAYER` was banned by `EXECUTOR` for `REASON` DURATION\",\n\"color\":\"16711680\",\n\"footer\":{\"text\":\"Powered by LiteBans Webhook\"}\n}]}",
            "{\n\"embeds\":[{\n\"title\":\"Punishment Revoked (SERVER) :white_check_mark:\",\n\"description\":\"The player `PLAYER` was unbanned by `EXECUTOR` for `REASON`\",\n\"color\":\"3329330\",\n\"footer\":{\"text\":\"Powered by LiteBans Webhook\"}\n}]}",
            "{\n\"embeds\":[{\n\"title\":\"New Punishment (SERVER) :timer:\",\n\"description\":\"The player `PLAYER` was ip-banned by `EXECUTOR` for `REASON` DURATION\",\n\"color\":\"16711680\",\n\"footer\":{\"text\":\"Powered by LiteBans Webhook\"}\n}]}",
            "{\n\"embeds\":[{\n\"title\":\"New Punishment (SERVER) :timer:\",\n\"description\":\"The player `PLAYER` was ip-muted by `EXECUTOR` for `REASON` DURATION\",\n\"color\":\"8900346\",\n\"footer\":{\"text\":\"Powered by LiteBans Webhook\"}\n}]}",
            "{\n\"embeds\":[{\n\"title\":\"New Punishment (SERVER) :boot:\",\n\"description\":\"The player `PLAYER` was kicked by `EXECUTOR` for `REASON`\",\n\"color\":\"16734208\",\n\"footer\":{\"text\":\"Powered by LiteBans Webhook\"}\n}]}",
            "{\n\"embeds\":[{\n\"title\":\"New Punishment (SERVER) :timer:\",\n\"description\":\"The player `PLAYER` was muted by `EXECUTOR` for `REASON` DURATION\",\n\"color\":\"8900346\",\n\"footer\":{\"text\":\"Powered by LiteBans Webhook\"}\n}]}",
            "{\n\"embeds\":[{\n\"title\":\"Punishment Revoked (SERVER) :white_check_mark:\",\n\"description\":\"The player `PLAYER` was unmuted by `EXECUTOR` for `REASON`\",\n\"color\":\"3329330\",\n\"footer\":{\"text\":\"Powered by LiteBans Webhook\"}\n}]}",
            "{\n\"embeds\":[{\n\"title\":\"New Warning (SERVER) :warning:\",\n\"description\":\"The player `PLAYER` was warned by `EXECUTOR` for `REASON`\",\n\"color\":\"16777062\",\n\"footer\":{\"text\":\"Powered by LiteBans Webhook\"}\n}]}",
            "{\n\"embeds\":[{\n\"title\":\"Warning Revoked (SERVER) :white_check_mark:\",\n\"description\":\"The player `PLAYER` was unwarned by `EXECUTOR` for `REASON`\",\n\"color\":\"3329330\",\n\"footer\":{\"text\":\"Powered by LiteBans Webhook\"}\n}]}"
        };
        String directory = dataFolder + File.separator + "embeds";
        File embedsDir = new File(directory);
        if (!embedsDir.exists()) {
            embedsDir.mkdirs();
        }
        try {
            for (int i = 0; i < fileNames.length; i++) {
                File file = new File(embedsDir, fileNames[i] + ".json");
                if (!file.exists()) {
                    Bukkit.getLogger().info("Created File "+fileNames[i]+".json");
                    FileWriter writer = new FileWriter(file);
                    writer.write(jsonValues[i]);
                    writer.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}