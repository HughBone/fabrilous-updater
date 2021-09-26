package com.hughbone.fabrilousupdater.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.entity.player.PlayerEntity;

public class ClientPlayerHack {
    public static PlayerEntity getPlayer(CommandContext<FabricClientCommandSource> ctx) {
        return ctx.getSource().getPlayer();
    }
}
