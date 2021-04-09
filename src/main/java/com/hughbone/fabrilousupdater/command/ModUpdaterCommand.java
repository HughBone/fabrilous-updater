package com.hughbone.fabrilousupdater.command;

import com.hughbone.fabrilousupdater.CheckForUpdate;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class ModUpdaterCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, isDedicated) -> dispatcher.register(CommandManager.literal("fabdate")
                .then(CommandManager.literal("update").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
                    new UpdateThread(context.getSource()).start();
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
            source.sendFeedback(new LiteralText("[FabrilousUpdater] Searching for updates. This may take a while..."), false);
            CheckForUpdate.start(source);
            source.sendFeedback(new LiteralText("[FabrilousUpdater] Finished searching!"), false);
        }
    }

}
