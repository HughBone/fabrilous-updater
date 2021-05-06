package com.hughbone.fabrilousupdater.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hughbone.fabrilousupdater.CurrentMod;
import com.hughbone.fabrilousupdater.util.FabdateUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class CurseForgeUpdater {

    private static final String sURL = "https://addons-ecs.forgesvc.net/api/v2/addon/";

    private static class CurseReleaseFile {
        private String fileName;
        private String fileDate;
        private String downloadUrl;
        private ArrayList<String> gameVersions = new ArrayList<>();

        CurseReleaseFile(JsonObject json) {
            this.fileName = json.get("fileName").toString().replace("\"", "");
            this.fileDate = json.get("fileDate").toString().replace("\"", "");
            this.downloadUrl = json.get("downloadUrl").toString();
            downloadUrl = downloadUrl.substring(1, downloadUrl.length() - 1);
            for (JsonElement j : json.getAsJsonArray("gameVersion")) {
                gameVersions.add(j.toString().replace("\"", ""));
            }
        }
    }

    private static class CurseModPage {
        private String name;
        private String websiteUrl;

        CurseModPage(JsonObject json) {
            this.name = json.get("name").toString().replace("\"", "");
            this.websiteUrl = json.get("websiteUrl").toString().replace("\"", "");
        }
    }

    public static CurrentMod getCurrentMod(String postResult) {
        try {
            JsonParser jp = new JsonParser();
            JsonObject jsonObject = jp.parse(postResult).getAsJsonObject();
            String fileName = jsonObject.get("exactMatches").getAsJsonArray().get(0).getAsJsonObject().get("file").getAsJsonObject().get("fileName").toString().replace("\"", "");
            String projectID = jsonObject.get("exactMatches").getAsJsonArray().get(0).getAsJsonObject().get("id").toString();
            CurrentMod currentMod = new CurrentMod(fileName, projectID);
            return currentMod;
        } catch (Exception e) {}

        return null;
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

    public static void start(CurrentMod currentMod) throws Exception {
        // remove last decimal in MC version (ex. 1.16.5 --> 1.16)
        String versionStr = FabdateUtil.getMinecraftVersion().getId();
        String[] versionStrSplit = versionStr.split("\\.");
        versionStrSplit = ArrayUtils.remove(versionStrSplit, 2);
        versionStr = versionStrSplit[0] + "." + versionStrSplit[1];

        // Get entire json list of release info
        JsonArray json1 = FabdateUtil.getJsonArray(sURL + currentMod.projectID + "/files");
        // Find newest release for MC version
        CurseReleaseFile newestFile = null;
        int date = 0;
        for (JsonElement jsonElement : json1) {
            CurseReleaseFile modFile = new CurseReleaseFile(jsonElement.getAsJsonObject());

            String gameVersionsString = String.join(" ", modFile.gameVersions); // states mc version, fabric, forge
            // Skip if it contains forge and not fabric
            if (gameVersionsString.toLowerCase().contains("forge") && !gameVersionsString.toLowerCase().contains("fabric")) {
                continue;
            }
            // Allow if same MC version or if universal release
            if (gameVersionsString.contains(versionStr) || modFile.fileName.toLowerCase().contains("universal")) {
                // Format the date into an integer
                String[] fileDateSplit = modFile.fileDate.split("-");
                fileDateSplit[2] = fileDateSplit[2].substring(0, 2);

                // Compare release dates to get most recent mod version
                if (date == 0) {
                    date = Integer.parseInt(String.join("", fileDateSplit));
                    newestFile = modFile;
                } else if (date < Integer.parseInt(String.join("", fileDateSplit))) {
                    date = Integer.parseInt(String.join("", fileDateSplit));
                    newestFile = modFile;
                }
            }
        }

        if (!currentMod.fileName.equals(newestFile.fileName)) {
            // Get mod name
            JsonObject json2 = FabdateUtil.getJsonObject(sURL + currentMod.projectID);
            CurseModPage modPage = new CurseModPage(json2);
            ModPlatform.modName = modPage.name;

            FabdateUtil.sendMessage(modPage.websiteUrl + "/files", newestFile.downloadUrl, newestFile.fileName); // Sends update message to player
        }
    }

}
