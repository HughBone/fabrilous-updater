package com.hughbone.fabrilousupdater.platform;

import com.google.gson.JsonArray;
import com.hughbone.fabrilousupdater.FabrilousUpdater;
import com.hughbone.fabrilousupdater.util.FabUtil;
import com.hughbone.fabrilousupdater.util.Hash;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class ModPlatform {

    public static boolean isRunning = false;

    public void start(PlayerEntity player, String command) {
        if (isRunning) {
            player.sendMessage(new LiteralText("[Error] Already checking for updates!").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
        }
        isRunning = true;

        // Search through all mods
        File directoryPath = new File("mods");
        File filesList[] = directoryPath.listFiles();
        outer:
        for (File modFile : filesList) {

            // Skip mod if in ignore list
            try {
                String line = "";
                BufferedReader file = new BufferedReader(
                        new FileReader(System.getProperty("user.dir") + File.separator + "config" + File.separator + "fabrilous-updater-ignore.txt"));
                while ((line = file.readLine()) != null) {
                    if (modFile.getName().equals(line)) {
                        continue outer;
                    }
                }
            } catch (IOException e) {}

            player.sendMessage(new LiteralText("Checking " + modFile.getName() + ".."), true);

            // Check for updates
            if (modFile.getName().contains(".jar")) {
                ReleaseFile newestFile = null;
                try {
                    // Check if Modrinth mod
                    String sha1 = Hash.getSHA1(modFile);
                    CurrentMod currentMod = new CurrentMod(sha1, "modrinth");

                    if (currentMod.modName != null) {
                        // Get entire json list of release info
                        JsonArray json = FabUtil.getJsonArray("https://api.modrinth.com/api/v1/mod/" + currentMod.projectID + "/version");
                        newestFile = FabUtil.getNewUpdate(json, currentMod, "modrinth");
                    }

                    // Check if CurseForge mod
                    else {
                        String murmurHash = Hash.getMurmurHash(modFile);
                        String postResult = FabUtil.sendPost(murmurHash);

                        if (postResult != null) {
                            // Get project ID
                            currentMod = new CurrentMod(postResult, "curseforge");
                            if (currentMod.modName != null) {
                                // Get entire json list of release info
                                JsonArray json = FabUtil.getJsonArray("https://addons-ecs.forgesvc.net/api/v2/addon/" + currentMod.projectID + "/files");
                                newestFile = FabUtil.getNewUpdate(json, currentMod, "curseforge");
                            }
                        }
                    }

                    if (currentMod.modName == null) {
                        player.sendMessage(new LiteralText("[Error] '" + modFile.getName() + "' not found in Modrinth or CurseForge").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                    }
                    // Send update message0
                    else if (newestFile != null) {
                        if (command.equals("update")) {
                            Text updateMessage = Text.Serializer.fromJson(" [\"\",{\"text\":\"" +
                                    currentMod.modName + "  \",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" +
                                    currentMod.websiteUrl + "\"}," + "\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Website\",\"italic\":true}]}},{" + "\"text\":\"has an \"},{\"text\":\"update.\",\"color\":\"dark_green\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" +
                                    newestFile.downloadUrl + "\"}," + "\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Direct Download\",\"italic\":true}]}}]");

                            player.sendMessage(updateMessage, false);

                        }
                        else if (command.equals("autoupdate")) {
                            try {
                                FabUtil.createConfigFiles();
                                //modFile.deleteOnExit();
                                //Files.move(modFile.toPath(), Paths.get( System.getProperty("user.dir") + File.separator + "mods" + File.separator + "Outdated_Mods" + File.separator + modFile.getName()), StandardCopyOption.REPLACE_EXISTING);
                                //downloadFromURL(newestFile.downloadUrl, System.getProperty("user.dir") + "/mods/" + newestFile.fileName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Text updateMessage = Text.Serializer.fromJson("[\"\",{\"text\":\"" +
                                    currentMod.modName + ": \",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" +
                                    currentMod.websiteUrl + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"Website\"]}},{\"text\":\"[" +
                                    Array.get(currentMod.fileDate.split("T"), 0) + "] \",\"color\":\"white\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"" +
                                    currentMod.fileName + "\"]}},\"--> \",{\"text\":\"[" +
                                    Array.get(newestFile.fileDate.split("T"), 0) + "]\",\"color\":\"dark_green\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"" +
                                    newestFile.fileName + "\"]}}]");

                            player.sendMessage(updateMessage, false);
                        }

                    }

                } catch (Exception e) {
                    player.sendMessage(new LiteralText("[Error] '" + modFile.getName() + "' not found in Modrinth or CurseForge").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                }
            }
        }

        player.sendMessage(new LiteralText("Finished!"), true);
        isRunning = false;
    }

    private void downloadFromURL(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

}
