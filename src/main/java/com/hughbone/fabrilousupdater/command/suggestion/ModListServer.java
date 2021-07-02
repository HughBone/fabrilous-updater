package com.hughbone.fabrilousupdater.command.suggestion;

import com.hughbone.fabrilousupdater.util.FabUtil;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;


public class ModListServer implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        FabUtil.createConfigFiles();
        // Search through all mods
        File directoryPath = new File("mods");
        File filesList[] = directoryPath.listFiles();

        outer:
        for (File modFile : filesList) {
            String modFileName = modFile.getName();

            if (modFileName.contains(".jar")) {
                try {
                    String line = "";
                    BufferedReader file = new BufferedReader(
                            new FileReader(System.getProperty("user.dir") + File.separator + "config" + File.separator + "fabrilous-updater-ignore.txt"));
                    while ((line = file.readLine()) != null) {
                        if (line.equals(modFileName)) {
                            continue outer;
                        }
                    }
                } catch (IOException e) {}

                builder.suggest(modFile.getName());
            }
        }

        return builder.buildFuture();
    }

}