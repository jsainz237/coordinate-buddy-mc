package com.jsainz.coordinate.buddy;

import com.jsainz.coordinate.buddy.suggesters.PlayerSuggestionProvider;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.EnumSet;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

public class CoordinateBuddy implements ModInitializer {

    public static final String MOD_ID = "cb";
    private static final String TOP_LEVEL_COMMAND = "cb";
    private static final SuggestionProvider<ServerCommandSource> playerSuggestions = new PlayerSuggestionProvider();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralCommandNode<ServerCommandSource> cbCommand = CommandManager
                    .literal(TOP_LEVEL_COMMAND)
                    .build();

            LiteralCommandNode<ServerCommandSource> meCommand = CommandManager.literal("me")
                    .executes(ctx -> broadcastCoordinates(ctx.getSource()))
                    .build();

            LiteralCommandNode<ServerCommandSource> helpCommand = CommandManager.literal("help")
                    .executes(ctx -> showCommands(cbCommand, dispatcher, ctx.getSource()))
                    .build();

            LiteralCommandNode<ServerCommandSource> homeCommand = CommandManager.literal("home")
                    .build();

            LiteralCommandNode<ServerCommandSource> setHomeCommand = CommandManager.literal("set")
                    .executes(ctx -> setHomeCoordinates(ctx.getSource()))
                    .build();

            LiteralCommandNode<ServerCommandSource> getHomeCommand = CommandManager.literal("get")
                    .build();

            LiteralCommandNode<ServerCommandSource> getMyHomeCommand = CommandManager.literal("me")
                    .executes(ctx -> {
                        final String playerName = ctx.getSource().getPlayer().getDisplayName().getString();
                        final LiteralText message = getPlayerHomeCoordinates(playerName, ctx.getSource());
                        ctx.getSource().getPlayer().sendMessage(message, false);
                        return Command.SINGLE_SUCCESS;
                    })
                    .build();

            ArgumentCommandNode<ServerCommandSource, String> getPlayerHomeCommand = CommandManager.argument("player", word())
                    .suggests(playerSuggestions)
                    .executes(ctx -> {
                        final String playerName = ctx.getArgument("player", String.class);
                        final LiteralText message = getPlayerHomeCoordinates(playerName, ctx.getSource());
                        ctx.getSource().getPlayer().sendMessage(message, false);
                        return Command.SINGLE_SUCCESS;
                    })
                    .build();



            dispatcher.getRoot().addChild(cbCommand);

            cbCommand.addChild(meCommand);
            cbCommand.addChild(helpCommand);
            cbCommand.addChild(homeCommand);

            homeCommand.addChild(setHomeCommand);
            homeCommand.addChild(getHomeCommand);
            getHomeCommand.addChild(getMyHomeCommand);
            getHomeCommand.addChild(getPlayerHomeCommand);
        });
    }

    /**
     * Get requesting Player's current coordinates
     * @param source
     * @return player's current coordinates (floored)
     * @throws CommandSyntaxException
     */
    private static String getPlayerCoords(ServerCommandSource source) throws CommandSyntaxException {
        final EnumSet<Direction.Axis> axes = EnumSet.of(Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z);
        final String playerCoords = source.getPlayer().getPos().floorAlongAxes(axes).toString();
        return playerCoords;
    }

    /**
     * Broadcast requesting Player's current coordinates to the general chat
     * @param source
     * @return Success Integer
     * @throws CommandSyntaxException
     */
    public static int broadcastCoordinates(ServerCommandSource source) throws CommandSyntaxException {
        final String playerCoords = getPlayerCoords(source);
        final String playerName = source.getPlayer().getDisplayName().getString();

        final Text message = new LiteralText(playerName + ": " + playerCoords);

        source.getServer().getPlayerManager().broadcast(message, MessageType.CHAT, source.getPlayer().getUuid());
        return Command.SINGLE_SUCCESS; //success
    }

    /**
     * Send list of commands as message to requesting player
     * @param source
     * @return Success Integer
     * @throws CommandSyntaxException
     */
    public static int showCommands(
        LiteralCommandNode<ServerCommandSource> node,
        CommandDispatcher<ServerCommandSource> dispatcher,
        ServerCommandSource source
    ) throws CommandSyntaxException {
        final Collection<String> commandList = dispatcher.getSmartUsage(node, source).values();
        String text = "\nCoordinate Buddy :)\n";
        for (int i = 0; i < commandList.size(); i++) {
            text += "/cb " + commandList.toArray()[i];
            if(i != commandList.size() - 1) {
                text += "\n";
            }
        }

        final ServerPlayerEntity player = source.getPlayer();
        player.sendMessage(new LiteralText(text), false);

        return Command.SINGLE_SUCCESS;
    }

    public static int setHomeCoordinates(ServerCommandSource source) {
        try {
            World worldProvider = source.getWorld();
            String playerName = source.getPlayer().getDisplayName().getString();
            MyComponents.CBWORLD.get(worldProvider).setHomeCoordinates(playerName, getPlayerCoords(source));

            source.getPlayer().sendMessage(
                    new LiteralText("Home coordinates set: " + getPlayerCoords(source)),
                    false
            );

            return Command.SINGLE_SUCCESS;
        } catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static LiteralText getPlayerHomeCoordinates(String playerName, ServerCommandSource source) {
        World worldProvider = source.getWorld();
        final String playerHomeCoords = MyComponents.CBWORLD.get(worldProvider).getPlayerHomeCoordinates(playerName);

        if(playerHomeCoords == null) {
            return new LiteralText(playerName + " has no saved home coordinates :(\nUse the '/cb home set' command to set your home coordinates!");
        }

        return new LiteralText(playerName + "'s home: " + playerHomeCoords);
    }
}