package com.hughbone.fabrilousupdater.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hughbone.fabrilousupdater.platform.CurrentMod;
import com.hughbone.fabrilousupdater.platform.ModPlatform;
import com.hughbone.fabrilousupdater.platform.ReleaseFile;
import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.world.GameRules;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class FabUtil {

    public static void sendActionBar(String message) {
        try {
            // set command feedback to false
            boolean sendCommandFB = ModPlatform.commandSource.getMinecraftServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).get(); // original value
            ModPlatform.commandSource.getMinecraftServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, ModPlatform.commandSource.getMinecraftServer());
            // send message
            CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);
            message = message.replace("(fabric)", "");
            message = message.replace("(Fabric)", "");
            message = "title " + ModPlatform.commandSource.getPlayer().getEntityName() + " actionbar {\"text\":\"" + message + "\"}";
            cm.getDispatcher().execute(message, ModPlatform.commandSource.getMinecraftServer().getCommandSource());
            // reset command feedback
            ModPlatform.commandSource.getMinecraftServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(sendCommandFB, ModPlatform.commandSource.getMinecraftServer());
        } catch (CommandSyntaxException e) {
        }
    }

    public static void sendUpdateMessage(String websiteUrl, String downloadUrl, String modName) {
        try {
            CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);

            String commandString = "tellraw " + ModPlatform.commandSource.getName() + " [\"\",{\"text\":\"" + modName + "  \",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + websiteUrl + "\"}," +
                    "\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Website\",\"italic\":true}]}},{" +
                    "\"text\":\"has an \"},{\"text\":\"update.\",\"color\":\"dark_green\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + downloadUrl + "\"}," +
                    "\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Direct Download\",\"italic\":true}]}}]";

            cm.getDispatcher().execute(commandString, ModPlatform.commandSource.getMinecraftServer().getCommandSource());

        } catch (CommandSyntaxException e) {
        }
    }

    public static String sendPost(String murmurHash) throws Exception {
        String body = "[" + murmurHash + "]";

        HttpURLConnection urlConn;
        URL mUrl = new URL("https://addons-ecs.forgesvc.net/api/v2/fingerprint");
        urlConn = (HttpURLConnection) mUrl.openConnection();
        urlConn.setDoOutput(true);

        urlConn.addRequestProperty("Accept", "application/json");
        urlConn.addRequestProperty("Content-Type", "application/json");
        urlConn.addRequestProperty("Content-Type", "application/json");
        urlConn.getOutputStream().write(body.getBytes("UTF8"));

        StringBuilder content;
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

        String line;
        content = new StringBuilder();
        while ((line = br.readLine()) != null) {
            content.append(line);
        }
        urlConn.disconnect();

        if (content.toString().contains("\"exactMatches\":[]")) {
            return null;
        }
        return content.toString();
    }

    public static void getNewUpdate(JsonArray json, CurrentMod currentMod, String platform) {
        // Find newest release for MC version
        ReleaseFile newestFile = null;
        FileTime newestDate = FileTime.from(Instant.parse(currentMod.fileDate));

        for (JsonElement jsonElement : json) {
            ReleaseFile modRelease = new ReleaseFile(jsonElement.getAsJsonObject(), platform);

            if (modRelease.isFabric) {
                if (modRelease.isCompatible(FabUtil.getMinecraftVersion())) {
                    // Compare release dates to get most recent mod version
                    FileTime fileDate = FileTime.from(Instant.parse(modRelease.fileDate));
                    if (newestDate.compareTo(fileDate) < 0) {
                        newestDate = fileDate;
                        newestFile = modRelease;
                    }
                }
            }
        }

        // Send update messages
        if (newestFile != null) {
            if (platform.equals("curseforge")) {
                FabUtil.sendUpdateMessage(currentMod.websiteUrl + "/files", newestFile.downloadUrl, currentMod.modName);
            } else if (platform.equals("modrinth")) {
                FabUtil.sendUpdateMessage(currentMod.websiteUrl + "/versions", newestFile.downloadUrl, currentMod.modName);
            }
        }
    }

    private static String getJsonString(String sURL) {
        try {
            URL obj = new URL(sURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonArray getJsonArray(String sURL) {
        String jsonStr = getJsonString(sURL);
        JsonParser jp = new JsonParser();
        assert jsonStr != null;
        return jp.parse(jsonStr).getAsJsonArray();
    }

    public static JsonObject getJsonObject(String sURL) {
        String jsonStr = getJsonString(sURL);
        JsonParser jp = new JsonParser();
        assert jsonStr != null;
        return jp.parse(jsonStr).getAsJsonObject();
    }

    public static String getMinecraftVersion() {
        GameVersion minecraftVersion = MinecraftVersion.create();
        // remove last decimal in MC version (ex. 1.16.5 --> 1.16)
        String versionStr = minecraftVersion.getId();
        String[] versionStrSplit = versionStr.split("\\.");
        try {
            versionStrSplit = ArrayUtils.remove(versionStrSplit, 2);
        } catch (IndexOutOfBoundsException e) {
        }
        versionStr = versionStrSplit[0] + "." + versionStrSplit[1];
        return versionStr;
    }

    public static void createConfigFiles() {
        try {
            File file = new File(System.getProperty("user.dir") + File.separator + "config" + File.separator + "fabrilous-updater-ignore.txt");
            file.createNewFile();
        } catch (IOException ioe) {}
    }

}
