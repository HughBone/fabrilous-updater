package com.hughbone.fabrilousupdater;

import com.hughbone.fabrilousupdater.command.ModUpdaterCommand;
import net.fabricmc.api.ModInitializer;


public class FabrilousUpdater implements ModInitializer {

    @Override
    public void onInitialize() {
        ModUpdaterCommand.register();
    }
}
