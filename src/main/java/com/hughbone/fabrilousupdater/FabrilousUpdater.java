package com.hughbone.fabrilousupdater;

import com.hughbone.fabrilousupdater.command.AutoUpdateCommand;
import com.hughbone.fabrilousupdater.command.IgnoreCommand;
import com.hughbone.fabrilousupdater.command.ModUpdaterCommand;
import com.hughbone.fabrilousupdater.util.FabUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;


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
        } catch (Exception ignored) {}
    }
}
