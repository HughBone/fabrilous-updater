package com.hughbone.fabrilousupdater.command.suggestion;

import com.hughbone.fabrilousupdater.util.FabUtil;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class IgnoreListSuggestion implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        FabUtil.createConfigFiles();
        try {
            String line = "";
            BufferedReader file = new BufferedReader(
                    new FileReader(System.getProperty("user.dir") + File.separator + "config" + File.separator + "fabrilous-updater-ignore.txt"));
            while ((line = file.readLine()) != null) {
                builder.suggest(line);
            }
        } catch (IOException e) {}

        return builder.buildFuture();
    }

}