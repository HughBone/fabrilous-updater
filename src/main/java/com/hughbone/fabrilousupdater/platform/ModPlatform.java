package com.hughbone.fabrilousupdater.platform;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hughbone.fabrilousupdater.hash.Hash;
import net.minecraft.server.command.ServerCommandSource;


import java.io.*;

public class ModPlatform {

    public static ServerCommandSource commandSource;
    public static String modName;

    public static void platformStart(ServerCommandSource cm) throws Exception {
        commandSource = cm;

        // Search through all mods
        File directoryPath = new File("modtest"); // change to mods
        File filesList[] = directoryPath.listFiles();
        for (File modFile : filesList) {
            // Check if Modrinth mod
            String sh1 = Hash.getSH1(modFile);
            String projectID = ModrinthUpdater.getProjectID(sh1);

            if (projectID != null) {
                //ModrinthUpdater.start(projectID);
            }
            // Check if CurseForge mod
            else {
                String murmurHash = Hash.getMurmurHash(modFile);
                String postResult = CurseForgeUpdater.sendPost(murmurHash);

                if (postResult != null) {
                    // Get project ID
                    try {
                        JsonParser jp = new JsonParser();
                        JsonObject jsonObject = jp.parse(postResult).getAsJsonObject();
                        projectID = jsonObject.get("exactMatches").getAsJsonArray().get(0).getAsJsonObject().get("id").toString();

                        //CurseForgeUpdater.start(projectID);

                    } catch (Exception e){}

                }

            }

        }

    }
}
