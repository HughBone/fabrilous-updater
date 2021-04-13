package com.hughbone.fabrilousupdater.platform;

import com.hughbone.fabrilousupdater.FabrilousUpdater;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;

public class ModPlatform {

    public static ServerCommandSource commandSource;
    public static String modName;

    private static void platformStart(String pID) throws IOException, CommandSyntaxException {
        // CurseForge
        if (pID.length() == 6) {
            CurseForgeUpdater.start(pID);
        }
        // Modrinth
        else if (pID.length() == 8) {
            ModrinthUpdater.start(pID);
        }
    }

    public static void readConfig(ServerCommandSource source) throws IOException, CommandSyntaxException {
        commandSource = source;

        BufferedReader reader = new BufferedReader(new FileReader(FabrilousUpdater.path));
        String entireConfig = "";
        String line;
        // Add mod name next to ID if not already there
        while ((line = reader.readLine()) != null) {
            String[] lineArray = line.split(" ");
            String pID = lineArray[0]; // Get project ID
            lineArray = ArrayUtils.remove(lineArray, 0);
            String modNameCheck = String.join(" ", lineArray); // Get mod name

            platformStart(pID); // Gets mod update based on platform

            // Add modname if not already in config file
            if (modNameCheck.length() < 3) {
                line = pID + " (" + modName + ")";
            }
            entireConfig += line + "\n";
        }
        BufferedWriter file = new BufferedWriter(new FileWriter(FabrilousUpdater.path));
        file.write(entireConfig);
        file.close();
    }

}
