package com.hughbone.fabrilousupdater.command;

import com.hughbone.fabrilousupdater.command.suggestion.IgnoreListSuggestion;
import com.hughbone.fabrilousupdater.command.suggestion.ModListSuggestion;
import com.hughbone.fabrilousupdater.util.FabUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.io.*;

public class IgnoreCommand {

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, isDedicated) -> dispatcher.register(CommandManager.literal("fabdate")
                .then(CommandManager.literal("ignore")
                        .then(CommandManager.literal("add").then((CommandManager.argument("mod", StringArgumentType.word()).suggests(new ModListSuggestion()).executes((ctx) -> {
                            return execute(1, ctx, isDedicated);
                        }))))
                        .then(CommandManager.literal("remove").then((CommandManager.argument("ignore_list", StringArgumentType.word()).suggests(new IgnoreListSuggestion()).executes((ctx) -> {
                            return execute(2, ctx, isDedicated);
                        }))))
                        .then(CommandManager.literal("list").executes((ctx) -> {
                            return execute(3, ctx, isDedicated);
                        }))

        )));

    }

    private int execute(int option, CommandContext<ServerCommandSource> ctx, boolean isDedicated) {
        // Option: (1=add, 2=remove, 3=list)

        if (!isDedicated || ctx.getSource().hasPermissionLevel(4)) {
            // get just the mod from input
            String modInput = "";
            if (!(option == 3)) {
                try {
                    if (option == 1) {
                        modInput = ctx.getInput().substring(19);
                    }
                    else if (option == 2) {
                        modInput = ctx.getInput().substring(22);
                    }
                    // Remove spaces at the start of the string
                    while (Character.toString(modInput.charAt(0)).equals(" ")) {
                        modInput = modInput.substring(1);
                    }
                    // Remove spaces at the end of the string
                    while (Character.toString(modInput.charAt(modInput.length()-1)).equals(" ")) {
                        modInput = modInput.substring(0, modInput.length() - 1);
                    }
                } catch (IndexOutOfBoundsException e) {}
            }

            FabUtil.createConfigFiles(); // Make sure ignore config file exists
            String line;
            StringBuilder newFile = new StringBuilder();
            boolean isRemoved = false;
            if (option == 1) {
                newFile.append(modInput + "\n");
            }
            try {
                BufferedReader file = new BufferedReader(
                        new FileReader(System.getProperty("user.dir") + File.separator + "config" + File.separator + "fabrilous-updater-ignore.txt"));

                while ((line = file.readLine()) != null) {
                    if (option == 1) {
                        if (line.equals(modInput)) {
                            ctx.getSource().sendError(new LiteralText("[Error] " + modInput + " is already in ignore list."));
                            file.close();
                            return 0;
                        }
                        else {
                            newFile.append(line + "\n");
                        }
                    }
                    else if (option == 2) {
                        if (!line.equals(modInput)) {
                            newFile.append(line + "\n");
                        }
                        else {
                            isRemoved = true;
                        }
                    }
                    else if (option == 3) {
                        ctx.getSource().sendFeedback(new LiteralText(line), false);
                    }
                }
                file.close();
            } catch (IOException e) {}

            if (option == 3) {
                return 1;
            }
            else {
                try {
                    BufferedWriter file = new BufferedWriter(
                            new FileWriter(System.getProperty("user.dir") + File.separator + "config" + File.separator + "fabrilous-updater-ignore.txt"));
                    file.write(newFile.toString());
                    file.close();
                } catch (IOException e) {}
            }
            // Success messages
            if (option == 1) {
                ctx.getSource().sendFeedback(new LiteralText("Successfully added " + modInput + " to ignore list."), false);
            }
            else if (option == 2) {
                if (isRemoved) {
                    ctx.getSource().sendFeedback(new LiteralText("Successfully removed " + modInput + " from ignore list.") , false);
                }
                else {
                    ctx.getSource().sendError(new LiteralText("[Error] " + modInput + " is not in ignore list."));
                }
            }
            return 1;
        }
        else {
            ctx.getSource().sendFeedback(new LiteralText("[FabrilousUpdater] You need OP to use this command on servers."), false);
            return 0;
        }

    }
}