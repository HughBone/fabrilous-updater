package com.hughbone.fabrilousupdater.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hughbone.fabrilousupdater.platform.ModPlatform;
import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.world.GameRules;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class FabdateUtil {

    public static void sendActionBar(String message) throws CommandSyntaxException {
        // set command feedback to false
        boolean sendCommandFB = ModPlatform.commandSource.getMinecraftServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).get(); // original value
        ModPlatform.commandSource.getMinecraftServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, ModPlatform.commandSource.getMinecraftServer());
        // send message
        CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);
        message = message.replace("(fabric)", "");
        message = message.replace("(Fabric)", "");
        message = "title " + ModPlatform.commandSource.getPlayer().getEntityName() + " actionbar {\"text\":\"" + message + "\"}";
        cm.getDispatcher().execute(message, ModPlatform.commandSource.getMinecraftServer().getCommandSource());
        // reset command feedback
        ModPlatform.commandSource.getMinecraftServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(sendCommandFB, ModPlatform.commandSource.getMinecraftServer());
    }


    public static void sendUpdateMessage(String websiteUrl, String downloadUrl, String modName) throws CommandSyntaxException {
        CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);

        String commandString = "tellraw " + ModPlatform.commandSource.getName() + " [\"\",{\"text\":\"" + modName + "  \",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + websiteUrl + "\"}," +
                "\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Website\",\"italic\":true}]}},{" +
                "\"text\":\"has an \"},{\"text\":\"update.\",\"color\":\"dark_green\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + downloadUrl + "\"}," +
                "\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Direct Download\",\"italic\":true}]}}]";

        cm.getDispatcher().execute(commandString, ModPlatform.commandSource.getMinecraftServer().getCommandSource());
    }

    private static String getJsonString(String sURL) {
        try {
            URL obj = new URL(sURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e){
            return null;
        }
    }
    public static JsonArray getJsonArray(String sURL) {
        String jsonStr = getJsonString(sURL);
        JsonParser jp = new JsonParser();
        return jp.parse(jsonStr).getAsJsonArray();
    }
    public static JsonObject getJsonObject(String sURL) {
        String jsonStr = getJsonString(sURL);
        JsonParser jp = new JsonParser();
        return jp.parse(jsonStr).getAsJsonObject();
    }

    public static String getMinecraftVersion() {
        GameVersion minecraftVersion = MinecraftVersion.create();
        // remove last decimal in MC version (ex. 1.16.5 --> 1.16)
        String versionStr = minecraftVersion.getId();
        String[] versionStrSplit = versionStr.split("\\.");
        try {
            versionStrSplit = ArrayUtils.remove(versionStrSplit, 2);
        }
        catch (IndexOutOfBoundsException e) {}
        versionStr = versionStrSplit[0] + "." + versionStrSplit[1];
        return versionStr;
    }
}
