package com.hughbone.fabrilousupdater.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class ModListSuggestion implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        // Search through all mods
        File directoryPath = new File("mods");
        File filesList[] = directoryPath.listFiles();
        for (File modFile : filesList) {
            builder.suggest(modFile.getName());
        }

        return builder.buildFuture();
    }

}