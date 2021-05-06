package com.hughbone.fabrilousupdater.command;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;


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

            } catch (Exception e) {}
        }
    }

}
