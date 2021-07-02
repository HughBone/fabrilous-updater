package com.hughbone.fabrilousupdater.command;

import com.hughbone.fabrilousupdater.platform.ModPlatform;

import com.hughbone.fabrilousupdater.util.FabUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;


public class ModUpdaterCommand {

    public void register(String env) {
        if (env.equals("CLIENT")) {
            registerClient();
        } else {
            registerServer();
        }
    }

    private void registerClient() {
        ClientCommandManager.DISPATCHER.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("fabdate")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("update").executes(ctx -> {
                            PlayerEntity player = ClientPlayerHack.getPlayer(ctx);
                            if (FabUtil.modPresentOnServer && player.hasPermissionLevel(4)) {
                                player.sendMessage(new LiteralText("Note: Use '/fabdateserver update' for server mods.").setStyle(Style.EMPTY.withColor(Formatting.BLUE)), false);
                            }
                            new StartThread(player).start();
                            return 1;
                        })
                ));
    }

    private void registerServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, isDedicated) -> dispatcher.register(CommandManager.literal("fabdateserver").requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("update").executes(ctx -> {
                    new StartThread(ctx.getSource().getPlayer()).start();
                    return 1;
                }))
        ));

    }

    private class StartThread extends Thread {

        PlayerEntity player;

        public StartThread(PlayerEntity player) {
            this.player = player;
        }

        public void run() {
            if (ModPlatform.isRunning) {
                player.sendMessage(new LiteralText("[Error] Already checking for updates!").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            }
            else {
                player.sendMessage(new LiteralText("[FabrilousUpdater] Searching for updates. This may take a while..."), false);
                new ModPlatform().start(player, "update");
                player.sendMessage(new LiteralText("[FabrilousUpdater] Finished!"), false);
            }

        }
    }

}
