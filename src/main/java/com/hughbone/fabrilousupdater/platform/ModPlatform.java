package com.hughbone.fabrilousupdater.platform;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;


import java.io.*;

public class ModPlatform {

    public static ServerCommandSource commandSource;
    public static String modName;

    public static void platformStart(ServerCommandSource cm) throws IOException, CommandSyntaxException {
        commandSource = cm;

        /*
        // CurseForge
        if (pID.length() == 6) {
            CurseForgeUpdater.start(pID);
        }
        // Modrinth
        else if (pID.length() == 8) {
            ModrinthUpdater.start(pID);
        }
         */
    }

}
