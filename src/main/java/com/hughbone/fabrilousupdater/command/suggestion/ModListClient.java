package com.hughbone.fabrilousupdater.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import java.io.File;
import java.util.concurrent.CompletableFuture;


public class ModListClient implements SuggestionProvider<FabricClientCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        // Search through all mods
        File directoryPath = new File("mods");
        File filesList[] = directoryPath.listFiles();
        for (File modFile : filesList) {
            if (modFile.getName().contains(".jar")) {
                builder.suggest(modFile.getName());
            }
        }

        return builder.buildFuture();
    }


}