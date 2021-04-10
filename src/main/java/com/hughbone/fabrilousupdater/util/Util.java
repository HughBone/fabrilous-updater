package com.hughbone.fabrilousupdater.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.bridge.game.GameVersion;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.MinecraftVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class Util {

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
        return JsonParser.parseString(jsonStr).getAsJsonArray();
    }
    public static JsonObject getJsonObject(String sURL) {
        String jsonStr = getJsonString(sURL);
        return JsonParser.parseString(jsonStr).getAsJsonObject();
    }

    public static String urlToString(String urlStr) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        URL url = new URL(urlStr);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
        }

        return stringBuilder.toString();
    }

    private static String getMinecraftSemanticVersion() {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer("minecraft");
        if (mod.isPresent()) {
            return mod.get().getMetadata().getVersion().getFriendlyString();
        } else {
            return "";
        }
    }

    private static String minecraftVersionSemantic = null;
    private static GameVersion minecraftVersion = null;

    private static void updateMinecraftVersion() {
        if (minecraftVersionSemantic == null) {
            minecraftVersionSemantic = getMinecraftSemanticVersion();
        }
        if (minecraftVersion == null) {
            minecraftVersion = MinecraftVersion.create();
        }
    }

    public static GameVersion getMinecraftVersion() {
        updateMinecraftVersion();
        return minecraftVersion;
    }






}
