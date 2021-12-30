package com.jsainz.coordinate.buddy;

import com.jsainz.coordinate.buddy.suggesters.LocationSuggestionProvider;
import com.jsainz.coordinate.buddy.suggesters.PlayerSuggestionProvider;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
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
    private static final SuggestionProvider<ServerCommandSource> locationSuggestions = new LocationSuggestionProvider();

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

            LiteralCommandNode<ServerCommandSource> communityCommand = CommandManager.literal("community")
                    .build();

            LiteralCommandNode<ServerCommandSource> setCommunityCommand = CommandManager.literal("set")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(CommandManager.argument("location name", word())
                        .executes(ctx -> setCommunityCoordinates(ctx))
                    ).build();

            LiteralCommandNode<ServerCommandSource> getCommunityCommand = CommandManager.literal("get")
                    .then(CommandManager.argument("location", word())
                        .suggests(locationSuggestions)
                        .executes(ctx -> getCommunityCoordinates(ctx))
                    ).build();

            LiteralCommandNode<ServerCommandSource> clearCommunityCommand = CommandManager.literal("clear")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(ctx -> clearCommunityCoordinates(ctx))
                    .build();

            LiteralCommandNode<ServerCommandSource> setHomeCommand = CommandManager.literal("set")
                    .executes(ctx -> setHomeCoordinates(ctx.getSource()))
                    .build();

            LiteralCommandNode<ServerCommandSource> getHomeCommand = CommandManager.literal("get")
                    .then(CommandManager.literal("me")
                        .executes(ctx -> {
                            final String playerName = ctx.getSource().getPlayer().getDisplayName().getString();
                            final LiteralText message = getPlayerHomeCoordinates(playerName, ctx.getSource());
                            ctx.getSource().getPlayer().sendMessage(message, false);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                    .then(CommandManager.argument("player", word())
                        .suggests(playerSuggestions)
                        .executes(ctx -> {
                            final String playerName = ctx.getArgument("player", String.class);
                            final LiteralText message = getPlayerHomeCoordinates(playerName, ctx.getSource());
                            ctx.getSource().getPlayer().sendMessage(message, false);
                            return Command.SINGLE_SUCCESS;
                        }))
                    .build();

            LiteralCommandNode<ServerCommandSource> clearHomeCommand = CommandManager.literal("clear")
                    .executes(ctx -> {
                        final String playerName = ctx.getSource().getPlayer().getDisplayName().getString();
                        clearPlayerHomeCoordinates(playerName, ctx.getSource());
                        return Command.SINGLE_SUCCESS;
                    })
                    .build();

            dispatcher.getRoot().addChild(cbCommand);

            cbCommand.addChild(meCommand);
            cbCommand.addChild(helpCommand);
            cbCommand.addChild(homeCommand);
            cbCommand.addChild(communityCommand);

            homeCommand.addChild(setHomeCommand);
            homeCommand.addChild(getHomeCommand);
            homeCommand.addChild(clearHomeCommand);

            communityCommand.addChild(setCommunityCommand);
            communityCommand.addChild(getCommunityCommand);
            communityCommand.addChild(clearCommunityCommand);
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
        String text = "";
        for (int i = 0; i < commandList.size(); i++) {
            text += "/cb " + commandList.toArray()[i];
            if(i != commandList.size() - 1) {
                text += "\n";
            }
        }

        final ServerPlayerEntity player = source.getPlayer();
        final Text title = new LiteralText("Coordinate Buddy :)").formatted(Formatting.AQUA);

        player.sendMessage(title, false);
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

    public static LiteralText getPlayerHomeCoordinates(String playerName, ServerCommandSource source) throws CommandSyntaxException {
        World worldProvider = source.getWorld();
        final String playerHomeCoords = MyComponents.CBWORLD.get(worldProvider).getPlayerHomeCoordinates(playerName);

        final String currPlayerName = source.getPlayer().getDisplayName().getString();

        if(playerHomeCoords == null) {
            final LiteralText message = (LiteralText) new LiteralText(
                playerName.equals(currPlayerName) ?
                        "You have no saved home coordinates!" :
                        playerName + " has no saved home coordinates!"
            ).formatted(Formatting.RED);

            if(playerName.equals(currPlayerName)) {
                final LiteralText helpMessage = (LiteralText) new LiteralText("\nUse the '/cb home set' command to set your home coordinates")
                        .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
                final LiteralText commandMessage = (LiteralText) new LiteralText("\n[Set home]")
                    .setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cb home set"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Set your home coordinates?")))
                            .withColor(Formatting.GREEN)
                    );

                message.append(helpMessage);
                message.append(commandMessage);
            }

            return message;
        }

        return new LiteralText(playerName + "'s home: " + playerHomeCoords);
    }

    public static void clearPlayerHomeCoordinates(String playerName, ServerCommandSource source) throws CommandSyntaxException {
        final World worldProvider = source.getWorld();
        MyComponents.CBWORLD.get(worldProvider).clearPlayerHomeCoordinates(playerName);

        final String currPlayerName = source.getPlayer().getDisplayName().getString();
        LiteralText message;

        message = playerName.equals(currPlayerName) ?
                new LiteralText("your home coordinates have been removed!") :
                new LiteralText(playerName + "'s home coordinates have been removed!");

        source.getPlayer().sendMessage(message, false);
    }

    public static int setCommunityCoordinates(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final String locationName = ctx.getArgument("location name", String.class);
        final String coords = getPlayerCoords(ctx.getSource());

        MyComponents.CBWORLD.get(ctx.getSource().getWorld()).setCommunityCoords(locationName, coords);

        final LiteralText message = new LiteralText(locationName + " set for community: " + coords);
        ctx.getSource().getPlayer().sendMessage(message, false);

        return Command.SINGLE_SUCCESS;
    }

    public static int getCommunityCoordinates(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final String location = ctx.getArgument("location", String.class);
        final String coords = MyComponents.CBWORLD.get(ctx.getSource().getWorld()).getCommunityCoords(location);

        final LiteralText message = new LiteralText(location + ": " + coords);
        ctx.getSource().getPlayer().sendMessage(message, false);

        return Command.SINGLE_SUCCESS;
    }

    public static int clearCommunityCoordinates(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final String location = ctx.getArgument("location", String.class);
        MyComponents.CBWORLD.get(ctx.getSource().getWorld()).clearCommunityCoords(location);

        final LiteralText message = new LiteralText("Coordinates for " + location + " has been removed!");
        ctx.getSource().getPlayer().sendMessage(message, false);

        return Command.SINGLE_SUCCESS;
    }
}