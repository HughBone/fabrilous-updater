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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutoUpdateCommand {
    public void register(String env) {
        if (env.equals("CLIENT")) {
            registerClient();
        }
        else {
            registerServer();
        }
    }

    private void registerClient() {
        ClientCommandManager.DISPATCHER.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("fabdate")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("autoupdate").executes(ctx -> {
                    PlayerEntity player = ClientPlayerHack.getPlayer(ctx);

                    if (FabUtil.modPresentOnServer && player.hasPermissionLevel(4)) {
                        player.sendMessage(new LiteralText("Note: Use '/fabdateserver update' for server mods.").setStyle(Style.EMPTY.withColor(Formatting.BLUE)), false);
                    }
                    Text warningMessage = Text.Serializer.fromJson("[\"\",{\"text\":\"[Warning] \",\"color\":\"red\"},\"This command automatically deletes old mods and downloads new versions. \",{\"text\":\"Click here to continue.\",\"color\":\"dark_green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/ඞmogusClient\"}}]");
                    player.sendMessage(warningMessage, false);
                    return 1;
                }))
        );

        ClientCommandManager.DISPATCHER.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("ඞmogusClient")
                .executes(ctx -> {
                    new StartThread(ClientPlayerHack.getPlayer(ctx)).start();
                    return 1;
                })
        );
    }

    private void registerServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, isDedicated) -> dispatcher.register(CommandManager.literal("fabdateserver").requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("autoupdate").executes(ctx -> {
                    Text warningMessage = Text.Serializer.fromJson("[\"\",{\"text\":\"[Warning] \",\"color\":\"red\"},\"This command automatically deletes old mods and downloads new versions. \",{\"text\":\"Click here to continue.\",\"color\":\"dark_green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/ඞmogusServer\"}}]");
                    ctx.getSource().getPlayer().sendMessage(warningMessage, false);
                    return 1;
                }))
        ));

        CommandRegistrationCallback.EVENT.register((dispatcher, isDedicated) -> dispatcher.register(CommandManager.literal("ඞmogusServer").requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
                    new StartThread(ctx.getSource().getPlayer()).start();
                    return 1;
                })
        ));
    }

    private static class StartThread extends Thread {
        PlayerEntity player;

        public StartThread(PlayerEntity player) {
            this.player = player;
        }

        public void run() {
            if (ModPlatform.isRunning) {
                player.sendMessage(new LiteralText("[Error] Already checking for updates!").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            }
            else {
                player.sendMessage(Text.of("[Fabrilous Updater] Automatically updating all mods..."), false);
                new ModPlatform().start(player, "autoupdate");
                player.sendMessage(new LiteralText("[FabrilousUpdater] Finished! Restart Minecraft to apply updates."), false);
            }
        }
    }
}
