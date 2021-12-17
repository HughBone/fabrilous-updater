package com.hughbone.fabrilousupdater.command;

import com.hughbone.fabrilousupdater.command.suggestion.*;
import com.hughbone.fabrilousupdater.util.FabUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IgnoreCommand {
    public void register(String env) {
        if (env.equals("CLIENT")) {
            registerClient();
        } else {
            registerServer();
        }

        removeDeletedMods(); // Remove from ignore list if not found in mods directory
    }

    private void registerClient() {
        ClientCommandManager.DISPATCHER.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("fabdate")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("ignore")
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("add").then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("mod", StringArgumentType.word()).suggests(ModList::getSuggestions).executes(ctx -> execute(1, StringArgumentType.getString(ctx, "mod"), ClientPlayerHack.getPlayer(ctx)))))
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("remove").then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("mod", StringArgumentType.word()).suggests(IgnoreList::getSuggestions).executes(ctx -> execute(2, StringArgumentType.getString(ctx, "mod"), ClientPlayerHack.getPlayer(ctx)))))
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("list").executes((ctx) -> execute(3, null, ClientPlayerHack.getPlayer(ctx))))
                )
        );
    }

    private void registerServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, isDedicated) -> dispatcher.register(CommandManager.literal("fabdateserver").requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("ignore")
                        .then(CommandManager.literal("add").then(CommandManager.argument("mod", StringArgumentType.word()).suggests(ModList::getSuggestions).executes((ctx) -> execute(1, StringArgumentType.getString(ctx, "mod"), ctx.getSource().getPlayer()))))
                        .then(CommandManager.literal("remove").then((CommandManager.argument("mod", StringArgumentType.word()).suggests(IgnoreList::getSuggestions).executes((ctx) -> execute(2, StringArgumentType.getString(ctx, "mod"), ctx.getSource().getPlayer())))))
                        .then(CommandManager.literal("list").executes((ctx) -> execute(3, null, ctx.getSource().getPlayer())))
                )
        ));
    }

    private void removeDeletedMods() {
        // Remove from ignore list if mod is deleted
        FabUtil.createConfigFiles();
        try {
            BufferedReader file = Files.newBufferedReader(FabUtil.updaterIgnorePath);

            List<String> goodLines = new ArrayList<>();
            String line;
            boolean modDeleted = false;
            while ((line = file.readLine()) != null) {
                boolean modExists = false;

                for (Path modFile : Files.list(FabUtil.modsDir).toList()) {
                    String fileName = modFile.getFileName().toString();
                    if (fileName.contains(".jar")) {
                        if (fileName.equals(line)) {
                            modExists = true;
                            break;
                        }
                    }
                }

                if (modExists) {
                    goodLines.add(line);
                }
                else {
                    modDeleted = true;
                }
            }
            file.close();

            if (modDeleted) {
                BufferedWriter writeFile = Files.newBufferedWriter(FabUtil.updaterIgnorePath);

                for (String writeLine : goodLines) {
                    writeFile.write(writeLine + "\n");
                }
                writeFile.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private int execute(int option, String modInput, PlayerEntity player) {  // Option: (1=add, 2=remove, 3=list)
        // get just the mod from input
        if (option == 3) {
            player.sendMessage(new LiteralText("[Fabrilous Updater] Ignore List:").setStyle(Style.EMPTY.withColor(Formatting.GRAY)), false);
        }

        FabUtil.createConfigFiles(); // Make sure ignore config file exists
        String line;
        StringBuilder newFile = new StringBuilder();
        boolean isRemoved = false;
        if (option == 1) {
            newFile.append(modInput).append("\n");
        }
        try {
            BufferedReader file = Files.newBufferedReader(FabUtil.updaterIgnorePath);

            while ((line = file.readLine()) != null) {
                if (option == 1) {
                    if (line.equals(modInput)) {
                        player.sendMessage(new LiteralText("[Error] " + modInput + " is already in ignore list.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                        file.close();
                        return 0;
                    } else {
                        newFile.append(line).append("\n");
                    }
                } else if (option == 2) {
                    if (!line.equals(modInput)) {
                        newFile.append(line).append("\n");
                    } else {
                        isRemoved = true;
                    }
                } else if (option == 3) {
                    player.sendMessage(new LiteralText(line), false);
                }
            }
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (option == 3) {
            player.sendMessage(new LiteralText(""), false);
            return 1;
        } else {
            try {
                BufferedWriter file = Files.newBufferedWriter(FabUtil.updaterIgnorePath);
                file.write(newFile.toString());
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Success messages
        if (option == 1) {
            player.sendMessage(new LiteralText("Added " + modInput + " to ignore list."), false);
        } else if (option == 2) {
            if (isRemoved) {
                player.sendMessage(new LiteralText("Removed " + modInput + " from ignore list."), false);
            } else {
                player.sendMessage(new LiteralText("[Error] " + modInput + " is not in ignore list.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            }
        }
        return 1;
    }
}