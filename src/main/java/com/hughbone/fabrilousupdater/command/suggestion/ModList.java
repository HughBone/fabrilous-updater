package com.hughbone.fabrilousupdater.command.suggestion;

import com.hughbone.fabrilousupdater.util.FabUtil;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ModList {
    public static CompletableFuture<Suggestions> getSuggestions(Object o, SuggestionsBuilder builder) {
        FabUtil.createConfigFiles();
        // Search through all mods
        try {
            outer:
            for (Path modFile : Files.list(FabUtil.modsDir).toList()) {
                String modFileName = modFile.getFileName().toString();

                if (modFileName.contains(".jar")) {
                    try {
                        String line;
                        BufferedReader file = Files.newBufferedReader(FabUtil.updaterIgnorePath);
                        while ((line = file.readLine()) != null) {
                            if (line.equals(modFileName)) {
                                continue outer;
                            }
                        }
                    } catch (IOException ignored) {}

                    builder.suggest(modFileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.buildFuture();
    }
}
