package com.hughbone.fabrilousupdater.platform;

import com.google.gson.JsonArray;
import com.hughbone.fabrilousupdater.util.FabUtil;
import com.hughbone.fabrilousupdater.util.Hash;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModPlatform {
    public static boolean isRunning = false;

    public void start(PlayerEntity player, String command) {
        if (isRunning) {
            player.sendMessage(new LiteralText("[Error] Already checking for updates!").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
        }
        isRunning = true;

        // Search through all mods
        try {
            outer:
            for (Path modFile : Files.list(FabUtil.modsDir).toList()) {
                // Skip mod if in ignore list
                String fileName = modFile.getFileName().toString();
                try {
                    String line;
                    BufferedReader file = Files.newBufferedReader(FabUtil.updaterIgnorePath);
                    while ((line = file.readLine()) != null) {
                        if (fileName.equals(line)) {
                            continue outer;
                        }
                    }
                } catch (IOException ignored) {}

                player.sendMessage(new LiteralText("Checking " + fileName + ".."), true);

                // Check for updates
                if (fileName.contains(".jar")) {
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
                            player.sendMessage(new LiteralText("[Error] '" + fileName + "' not found in Modrinth or CurseForge").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                        }
                        // Send update message
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
                                    Files.delete(modFile);
                                    String newFileName = newestFile.fileName;
                                    int li = fileName.lastIndexOf(".jar");
                                    if (li < fileName.length() - 4)
                                        newFileName += fileName.substring(li + 4);
                                    downloadFromURL(newestFile.downloadUrl, FabUtil.modsDir.resolve(newFileName));
                                } catch (IOException e) {
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
                        player.sendMessage(new LiteralText("[Error] '" + fileName + "' not found in Modrinth or CurseForge").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendMessage(new LiteralText("Finished!"), true);
        isRunning = false;
    }

    private void downloadFromURL(String urlStr, Path target) throws IOException {
        try (InputStream is = new URL(urlStr).openStream()) {
            Files.copy(is, target);
        }
    }
}
