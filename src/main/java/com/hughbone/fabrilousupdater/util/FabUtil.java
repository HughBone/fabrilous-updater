package com.hughbone.fabrilousupdater.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hughbone.fabrilousupdater.platform.CurrentMod;
import com.hughbone.fabrilousupdater.platform.ReleaseFile;
import com.mojang.bridge.game.GameVersion;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class FabUtil {
    public static boolean modPresentOnServer = false;
    public static Path updaterIgnorePath = FabricLoader.getInstance().getConfigDir().resolve("fabrilous-updater-ignore.txt");
    public static Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");

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

    public static ReleaseFile getNewUpdate(JsonArray json, CurrentMod currentMod, String platform) {
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

        return newestFile;
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
        } catch (IndexOutOfBoundsException ignored) {}
        versionStr = versionStrSplit[0] + "." + versionStrSplit[1];
        return versionStr;
    }

    public static void createConfigFiles() {
        try {
            if (!Files.exists(updaterIgnorePath))
                Files.createFile(updaterIgnorePath);
        } catch (IOException ignored) {}
    }
}
