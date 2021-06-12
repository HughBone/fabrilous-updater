package com.hughbone.fabrilousupdater.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hughbone.fabrilousupdater.CurrentMod;
import com.hughbone.fabrilousupdater.util.FabdateUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;


public class ModrinthUpdater {

    private static class MrReleaseFile {
        private String fileName;
        private String fileDate;
        private String downloadUrl;
        private ArrayList<String> gameVersions = new ArrayList<>();

        MrReleaseFile(JsonObject json) {
            this.fileDate = json.get("date_published").toString().replace("\"", "");

            final JsonArray filesArray =  json.getAsJsonArray("files");
            for (JsonElement j : filesArray) {
                this.fileName = j.getAsJsonObject().get("filename").toString().replace("\"", "");
                ;
                this.downloadUrl = j.getAsJsonObject().get("url").toString();
                downloadUrl = downloadUrl.substring(1, downloadUrl.length() - 1);
            }
            final JsonArray gameVerArray = json.getAsJsonArray("game_versions");
            for (JsonElement j : gameVerArray) {
                gameVersions.add(j.toString().replace("\"", ""));
            }
        }
    }

    private static class ModPage {
        private String name;
        private String websiteUrl;

        ModPage(JsonObject json) {
            this.name = json.get("title").toString().replace("\"", "");
            this.websiteUrl = "https://www.modrinth.com/mod/" + json.get("slug").toString().replace("\"", "");
        }
    }

    public static CurrentMod getCurrentMod(String sh1Hash) {
        try {
            JsonObject json = FabdateUtil.getJsonObject("https://api.modrinth.com/api/v1/version_file/" + sh1Hash + "?algorithm=sha1");
            final String fileName = json.get("files").getAsJsonArray().get(0).getAsJsonObject().get("filename").toString().replace("\"", "");
            final String projectID = json.get("mod_id").toString().replace("\"", "");
            CurrentMod currentMod = new CurrentMod(fileName, projectID);
            return currentMod;
        } catch (Exception e) {}

        return null;
    }

    public static void start(CurrentMod currentMod) throws Exception {
        final String sURL = "https://api.modrinth.com/api/v1/mod/";

        // Get mod name + webpage
        JsonObject json2 = FabdateUtil.getJsonObject(sURL + currentMod.projectID);
        ModPage modPage = new ModPage(json2);
        // send actionbar message
        FabdateUtil.sendActionBar("Checking " + modPage.name + "..");

        // Get Minecraft Version
        String versionStr = FabdateUtil.getMinecraftVersion();

        // Get entire json list of release info
        JsonArray json1 = FabdateUtil.getJsonArray(sURL + currentMod.projectID + "/version");
        // Find newest release for MC version
        MrReleaseFile newestFile = null;
        FileTime newestDate = null;
        for (JsonElement jsonElement : json1) {
            MrReleaseFile modFile = new MrReleaseFile(jsonElement.getAsJsonObject());

            String gameVersionsString = String.join(" ", modFile.gameVersions); // states mc version, fabric, forge
            // Skip if it contains forge and not fabric
            if (gameVersionsString.toLowerCase().contains("forge") && !gameVersionsString.toLowerCase().contains("fabric")) {
                continue;
            }
            // Allow if same MC version or if universal release
            if (gameVersionsString.contains(versionStr) || modFile.fileName.toLowerCase().contains("universal")) {
                // Compare release dates to get most recent mod version
                FileTime currentDate = FileTime.from(Instant.parse(modFile.fileDate));
                if (newestDate == null) {
                    newestDate = currentDate;
                    newestFile = modFile;
                }
                else if (currentDate.compareTo(newestDate) > 0) {
                    newestDate = currentDate;
                    newestFile = modFile;
                }
            }
        }
        // Send update message if an update is found
        if (!currentMod.fileName.equals(newestFile.fileName)) {
            FabdateUtil.sendUpdateMessage(modPage.websiteUrl + "/versions", newestFile.downloadUrl, modPage.name); // Sends update message to player
        }
    }

}
