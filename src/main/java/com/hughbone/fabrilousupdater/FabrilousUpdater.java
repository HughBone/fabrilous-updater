package com.hughbone.fabrilousupdater;

import com.hughbone.fabrilousupdater.command.AutoUpdateCommand;
import com.hughbone.fabrilousupdater.command.IgnoreCommand;
import com.hughbone.fabrilousupdater.command.ModUpdaterCommand;
import com.hughbone.fabrilousupdater.platform.ModPlatform;
import com.hughbone.fabrilousupdater.util.FabUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.mixin.resource.loader.client.GameOptionsMixin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FabrilousUpdater implements ModInitializer {

    @Override
    public void onInitialize() {

        // Register Commands
        String env = FabricLoader.getInstance().getEnvironmentType().name(); // Returns client or server
        new ModUpdaterCommand().register(env);
        new IgnoreCommand().register(env);
        new AutoUpdateCommand().register(env);

        // Check if the client sees this mod on a server
        Identifier identifier = new Identifier("fabrilous_updater");
        ServerPlayNetworking.registerGlobalReceiver(identifier, (server, player, handler, buf, responseSender) -> { });

        try {
            ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
                FabUtil.modPresentOnServer = false;
                if (!client.isInSingleplayer()) {
                    FabUtil.modPresentOnServer = ClientPlayNetworking.canSend(identifier);
                }

            });
        } catch (Exception e) {}

    new DeleteThread().run();
    }

    private class DeleteThread extends Thread {

        public void run() {
            ServerLifecycleEvents.SERVER_STOPPING.register( e -> {
                System.out.println("STOPPING.");
                Paths.get(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "fabrilous-updater-2.4.jar").toFile().deleteOnExit();
                File directoryPath = new File("mods");
                File filesList[] = directoryPath.listFiles();

                for (File modFile : filesList) {

                    while (true) {
                        try {
                            Thread.sleep(1000);
                            System.out.println("Trying to delete " + modFile.getName() + "...");
                            if (!modFile.delete()) {
                                continue;
                            }

                        } catch (Exception ex) {
                            continue;
                        }
                        break;
                    }

                }
            });

        }
    }


}
