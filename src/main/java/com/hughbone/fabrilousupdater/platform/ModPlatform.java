package com.hughbone.fabrilousupdater.platform;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hughbone.fabrilousupdater.CurrentMod;
import com.hughbone.fabrilousupdater.hash.Hash;
import net.minecraft.server.command.ServerCommandSource;


import java.io.*;

public class ModPlatform {

    public static ServerCommandSource commandSource;
    public static String modName;
    private static CurrentMod currentMod;

    public static void platformStart(ServerCommandSource cm) throws Exception {
        commandSource = cm;

        // Search through all mods
        File directoryPath = new File("modtest"); // change to mods
        File filesList[] = directoryPath.listFiles();
        for (File modFile : filesList) {
            // Check if Modrinth mod
            String sh1 = Hash.getSH1(modFile);
            currentMod = ModrinthUpdater.getCurrentMod(sh1);

            if (currentMod != null) {
                ModrinthUpdater.start(currentMod);
            }
            // Check if CurseForge mod
            else {
                String murmurHash = Hash.getMurmurHash(modFile);
                String postResult = CurseForgeUpdater.sendPost(murmurHash);

                if (postResult != null) {
                    // Get project ID
                    currentMod = CurseForgeUpdater.getCurrentMod(postResult);
                    CurseForgeUpdater.start(currentMod);

                }

            }

        }

    }
}
