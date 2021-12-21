package com.jsainz.coordinate.buddy.utils;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public enum CBCommand {
    me("me", "Send you current coordinates in chat"),
    setHome("set-home", "set your home coordinates for other players to see"),
    getHome("get-home", "get your home coordinates"),
    help("help", "Show list of available commands");

    public String name;
    public String description;
    public LiteralArgumentBuilder<ServerCommandSource> builder;

    CBCommand(String name, String description) {
        this.name = name;
        this.description = description;
        this.builder = CommandManager.literal(name);
    }
}
