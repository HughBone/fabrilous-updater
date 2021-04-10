package com.hughbone.fabrilousupdater.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hughbone.fabrilousupdater.FabrilousUpdater;
import com.hughbone.fabrilousupdater.util.Util;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.commons.lang3.ArrayUtils;

import java.net.HttpURLConnection;
import java.net.URL;

import java.io.*;
import java.util.ArrayList;

public class CurseForgeUpdater {

    private static final String sURL = "https://addons-ecs.forgesvc.net/api/v2/addon/";

    private static class ReleaseFile {
        private String fileName;
        private String fileDate;
        private String downloadUrl;
        private ArrayList<String> gameVersions = new ArrayList<>();

        ReleaseFile(JsonObject json) {
            this.fileName = json.get("fileName").toString().replace("\"", "");
            this.fileDate = json.get("fileDate").toString().replace("\"", "");
            this.downloadUrl = json.get("downloadUrl").toString();
            downloadUrl = downloadUrl.substring(1, downloadUrl.length() - 1);
            for (JsonElement j : json.getAsJsonArray("gameVersion")) {
                gameVersions.add(j.toString().replace("\"", ""));
            }
        }
    }

    private static class ModPage {
        private String name;
        private String websiteUrl;

        ModPage(JsonObject json) {
            this.name = json.get("name").toString().replace("\"", "");
            this.websiteUrl = json.get("websiteUrl").toString().replace("\"", "");
        }

    }

    public static void start(ServerCommandSource source) {

        try {
            writeModNames(); // Format config file to show mod names next to the ID

            // config file
            BufferedReader reader = new BufferedReader(new FileReader(FabrilousUpdater.path));

            // mods directory
            File dir = new File(System.getProperty("user.dir") + File.separator + "mods");
            File[] listDir = dir.listFiles();
            if (listDir != null) {
                // remove last decimal in version (ex. 1.16.5 --> 1.16)
                String versionStr = Util.getMinecraftVersion().getId();
                String[] versionStrSplit = versionStr.split("\\.");
                versionStrSplit = ArrayUtils.remove(versionStrSplit, 2);
                versionStr = versionStrSplit[0] + "." + versionStrSplit[1];
                String configLn; // projectID

                // loop through mod IDs
                while ((configLn = reader.readLine()) != null) {
                    configLn = configLn.substring(0, 6);
                    JsonArray json1 = Util.getJsonArray(sURL + configLn + "/files"); // Get entire json list of release info

                    // Find newest release for MC version
                    ReleaseFile newestFile = null;
                    int date = 0;
                    for (JsonElement jsonElement : json1) {
                        ReleaseFile currentFile = new ReleaseFile(jsonElement.getAsJsonObject());

                        String gameVersionsString = String.join(" ", currentFile.gameVersions); // states mc version, fabric, forge
                        // Skip if it contains forge and not fabric
                        if (gameVersionsString.toLowerCase().contains("forge") && !gameVersionsString.toLowerCase().contains("fabric")) {
                            continue;
                        }
                        // Allow if universal release, or if same MC version
                        if (gameVersionsString.contains(versionStr) || currentFile.fileName.toLowerCase().contains("universal")) {
                            // Format the date into an integer
                            String[] fileDateSplit = currentFile.fileDate.split("-");
                            fileDateSplit[2] = fileDateSplit[2].substring(0, 2);

                            // Compare release dates to get most recent mod version
                            if (date == 0) {
                                date = Integer.parseInt(String.join("", fileDateSplit));
                                newestFile = currentFile;
                            }
                            else if (date < Integer.parseInt(String.join("", fileDateSplit))) {
                                date = Integer.parseInt(String.join("", fileDateSplit));
                                newestFile = currentFile;
                            }
                        }
                    }
                    // Check if an update is needed
                    boolean upToDate = false;
                    for (File child : listDir) {
                        if (child.getName().equals(newestFile.fileName)) {
                            upToDate = true;
                            break;
                        }
                    }
                    if (!upToDate) {
                        JsonObject json2 = Util.getJsonObject(sURL + configLn);
                        ModPage modPage = new ModPage(json2);

                        sendMessage(source, newestFile, modPage); // Sends update message to player
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private static void writeModNames() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(FabrilousUpdater.path));
        String entireConfig = "";
        String line;
        // Add mod name next to ID if not already there
        while ((line = reader.readLine()) != null) {
            if (line.length() < 8) {
                JsonObject json2 = Util.getJsonObject(sURL + line);
                ModPage modPage = new ModPage(json2);

                line = line.replace(line, line + " (" + modPage.name + ")");
            }
            entireConfig += line + "\n";
        }
        BufferedWriter file = new BufferedWriter(new FileWriter(FabrilousUpdater.path));
        file.write(entireConfig);
        file.close();
    }

    private static void sendMessage(ServerCommandSource source, ReleaseFile newestFile, ModPage modPage) throws CommandSyntaxException {
        CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);

        String commandString = "tellraw @p [\"\",{\"text\":\"[Click Me] \",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"$url1\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Website\",\"italic\":true}]}},{\"text\":\"Update found: \"},{\"text\":\"$modname\",\"color\":\"dark_green\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"$url2\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Direct Download\",\"italic\":true}]}}]";
        commandString = commandString.replace("$url1", modPage.websiteUrl + "/files");
        commandString = commandString.replace("$url2", newestFile.downloadUrl);
        commandString = commandString.replace("$modname", newestFile.fileName);

        cm.getDispatcher().execute(commandString, source);
    }

}
