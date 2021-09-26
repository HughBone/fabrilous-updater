package com.hughbone.fabrilousupdater.command.suggestion;

import com.hughbone.fabrilousupdater.util.FabUtil;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class IgnoreList {
    public static CompletableFuture<Suggestions> getSuggestions(Object o, SuggestionsBuilder builder) {
        FabUtil.createConfigFiles();
        try {
            String line;
            BufferedReader file = Files.newBufferedReader(FabUtil.updaterIgnorePath);
            while ((line = file.readLine()) != null) {
                builder.suggest(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.buildFuture();
    }
}
