package com.hughbone.fabrilousupdater.command;

import com.hughbone.fabrilousupdater.FabrilousUpdater;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;

public class ModListCommand {

    public static void register() {

        CommandRegistrationCallback.EVENT.register((dispatcher, isDedicated) -> dispatcher.register(CommandManager.literal("fabdate")
                .then(CommandManager.literal("list").executes(context -> {
                    if (!isDedicated) {
                        new UpdateThread(context.getSource()).start();
                    }
                    else if (context.getSource().hasPermissionLevel(4)) {
                        new UpdateThread(context.getSource()).start();
                    }
                    else {
                        context.getSource().sendFeedback(new LiteralText("[FabrilousUpdater] You need OP to use this command on servers."), false);
                    }
                    return 1;
                }))
        ));
    }

    public static class UpdateThread extends Thread{

        private ServerCommandSource source;
        public UpdateThread(ServerCommandSource source) {
            this.source = source;
        }
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(FabrilousUpdater.path));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Get mod name
                    String[] lineArray = line.split(" ");
                    String pID = lineArray[0];
                    lineArray = ArrayUtils.remove(lineArray, 0);
                    String modName = String.join(" ", lineArray);

                    if (modName.length() < 2) {
                        source.sendFeedback(new LiteralText("[NAME NOT FOUND] Use '/fabdate update' and run this command again."), false);
                    }
                    else {
                        source.sendFeedback(new LiteralText(modName), false);
                    }
                }
                source.sendFeedback(new LiteralText("↑ MODS CURRENTLY IN CONFIG ↑"), false);
            } catch (Exception e) {}
        }
    }

}
