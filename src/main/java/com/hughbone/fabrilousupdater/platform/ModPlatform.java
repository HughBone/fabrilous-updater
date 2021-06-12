package com.hughbone.fabrilousupdater.platform;

import com.hughbone.fabrilousupdater.CurrentMod;
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
            try {
                // Check if Modrinth mod
                String sh1 = Hash.getSHA1(modFile);
                CurrentMod currentMod = ModrinthUpdater.getCurrentMod(sh1);

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
                if (currentMod == null) {
                    commandSource.sendFeedback(new LiteralText("[Error] '" + modFile.getName() + "' not found in Modrinth or CurseForge"), false);
                }
            } catch (Exception e) {
                commandSource.sendFeedback(new LiteralText("[Error] '" + modFile.getName() + "' not found in Modrinth or CurseForge"), false);
            }

        }
        FabUtil.sendActionBar("Finished!");
    }
}
