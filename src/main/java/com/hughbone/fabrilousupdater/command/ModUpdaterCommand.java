package com.hughbone.fabrilousupdater.command;

import com.hughbone.fabrilousupdater.platform.ModPlatform;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import java.io.File;


public class ModUpdaterCommand {

    public static void register() {

        CommandRegistrationCallback.EVENT.register((dispatcher, isDedicated) -> dispatcher.register(CommandManager.literal("fabdate")
                .then(CommandManager.literal("update").executes(context -> {
                    if (!isDedicated) {
                        new ListThread(context.getSource()).start();
                    }
                    else if (context.getSource().hasPermissionLevel(4)) {
                        new ListThread(context.getSource()).start();
                    }
                    else {
                        context.getSource().sendFeedback(new LiteralText("[FabrilousUpdater] You need OP to use this command on servers."), false);
                    }
                    return 1;
                }))
        ));
    }

    private static class ListThread extends Thread{

        private ServerCommandSource source;

        public ListThread(ServerCommandSource source) {
            this.source = source;
        }

        public void run() {
            try {

                String path = System.getProperty("user.dir") + File.separator + "config" + File.separator + "fabrilousupdater.txt";
                if (new File(path).isFile()) {
                    source.sendFeedback(new LiteralText("[FabrilousUpdater] 'fabrilousupdater.txt' is no longer needed! You may delete it."), false);
                }

                source.sendFeedback(new LiteralText("[FabrilousUpdater] Searching for updates. This may take a while..."), false);
                ModPlatform.platformStart(source);
                source.sendFeedback(new LiteralText("[FabrilousUpdater] Finished searching!"), false);
            } catch (Exception e) {}
        }
    }

}
