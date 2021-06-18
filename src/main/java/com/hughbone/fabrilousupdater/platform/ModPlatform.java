package com.hughbone.fabrilousupdater.platform;

import com.google.gson.JsonArray;
import com.hughbone.fabrilousupdater.util.FabUtil;
import com.hughbone.fabrilousupdater.util.Hash;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.io.*;


public class ModPlatform {

    public static ServerCommandSource commandSource;

    public static void platformStart(ServerCommandSource cm) {
        commandSource = cm;

        // Search through all mods
        File directoryPath = new File("mods");
        File filesList[] = directoryPath.listFiles();
        outer:
        for (File modFile : filesList) {
            System.out.println(modFile.getName());
            // Skip mod if ignored
            try {
                String line = "";
                BufferedReader file = new BufferedReader(
                        new FileReader(System.getProperty("user.dir") + File.separator + "config" + File.separator + "fabrilous-updater-ignore.txt"));
                while ((line = file.readLine()) != null) {
                    if (modFile.getName().equals(line)) {
                        System.out.println("skipped: " + modFile.getName());
                        continue outer;
                    }
                }
            } catch (IOException e) {}

            // Check for updates
            if (modFile.getName().contains(".jar")) {
                try {
                    // Check if Modrinth mod
                    String sha1 = Hash.getSHA1(modFile);
                    System.out.println(sha1);
                    CurrentMod currentMod = new CurrentMod(sha1, "modrinth");

                    if (currentMod.modName != null) {
                        // Get entire json list of release info
                        System.out.println("Modrinth: " + sha1);
                        JsonArray json = FabUtil.getJsonArray("https://api.modrinth.com/api/v1/mod/" + currentMod.projectID + "/version");
                        FabUtil.getNewUpdate(json, currentMod, "modrinth");
                    }

                    // Check if CurseForge mod
                    else {
                        String murmurHash = Hash.getMurmurHash(modFile);
                        String postResult = FabUtil.sendPost(murmurHash);

                        if (postResult != null) {
                            // Get project ID
                            currentMod = new CurrentMod(postResult, "curseforge");
                            if (currentMod.modName != null) {
                                //System.out.println("Curseforge: " + murmurHash);
                                // Get entire json list of release info
                                JsonArray json = FabUtil.getJsonArray("https://addons-ecs.forgesvc.net/api/v2/addon/" + currentMod.projectID + "/files");
                                FabUtil.getNewUpdate(json, currentMod, "curseforge");
                            }
                        }
                    }
                    if (currentMod.modName == null) {
                        commandSource.sendFeedback(new LiteralText("[Error] '" + modFile.getName() + "' not found in Modrinth or CurseForge"), false);
                    }

                } catch (Exception e) {
                    commandSource.sendFeedback(new LiteralText("[Error] '" + modFile.getName() + "' not found in Modrinth or CurseForge"), false);
                }
            }
        }
        FabUtil.sendActionBar("Finished!");
    }
}
