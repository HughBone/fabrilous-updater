package com.hughbone.fabrilousupdater.platform;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hughbone.fabrilousupdater.util.FabUtil;

public class CurrentMod {
    public String projectID;
    public String fileDate;
    public String modName;
    public String websiteUrl;

    CurrentMod(String hashOrResult, String platform) throws Exception {
        try {
            if (platform.equals("curseforge")) {
                JsonParser jp = new JsonParser();
                JsonObject json = jp.parse(hashOrResult).getAsJsonObject();

                projectID = json.get("exactMatches").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                fileDate = json.get("exactMatches").getAsJsonArray().get(0).getAsJsonObject().get("file").getAsJsonObject().get("fileDate").getAsString();

                json = FabUtil.getJsonObject("https://addons-ecs.forgesvc.net/api/v2/addon/" + projectID);
                modName = json.get("name").getAsString();
                websiteUrl = json.get("websiteUrl").getAsString();
            }

            else if (platform.equals("modrinth")) {
                JsonObject json = FabUtil.getJsonObject("https://api.modrinth.com/api/v1/version_file/" + hashOrResult + "?algorithm=sha1");

                projectID = json.get("mod_id").getAsString();
                fileDate = json.get("date_published").getAsString();

                json = FabUtil.getJsonObject("https://api.modrinth.com/api/v1/mod/" + projectID);
                modName = json.get("title").getAsString();
                websiteUrl = "https://www.modrinth.com/mod/" + json.get("slug").getAsString();
            }

            FabUtil.sendActionBar("Checking " + modName + "..");

        } catch (Exception e) {}
    }

}
