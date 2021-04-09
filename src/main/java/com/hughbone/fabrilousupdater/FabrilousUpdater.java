package com.hughbone.fabrilousupdater;

import com.hughbone.fabrilousupdater.command.ModListCommand;
import com.hughbone.fabrilousupdater.command.ModUpdaterCommand;
import net.fabricmc.api.ModInitializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FabrilousUpdater implements ModInitializer {

    public static final String path = System.getProperty("user.dir") + File.separator + "config" + File.separator + "fabrilousupdater.txt";

    @Override
    public void onInitialize() {
        // config file
        if (!new File(path).isFile()) {
            try {
                new BufferedWriter(new FileWriter(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ModListCommand.register();
        ModUpdaterCommand.register();
    }
}
