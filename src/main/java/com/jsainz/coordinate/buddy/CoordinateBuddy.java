package com.jsainz.coordinate.buddy;

import com.jsainz.coordinate.buddy.utils.CBCommand;
import com.jsainz.coordinate.buddy.utils.PlayerEntityExt;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

import java.util.EnumSet;
import java.util.List;

public class CoordinateBuddy implements ModInitializer {

    public static final String MOD_ID = "cb";
    private static final String TOP_LEVEL_COMMAND = "cb";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralCommandNode<ServerCommandSource> cbCommand = CommandManager
                    .literal(TOP_LEVEL_COMMAND)
                    .executes(ctx -> showCommands(ctx.getSource()))
                    .build();

            LiteralCommandNode<ServerCommandSource> meCommand = CBCommand.me.builder
                    .executes(ctx -> broadcastCoordinates(ctx.getSource()))
                    .build();

            LiteralCommandNode<ServerCommandSource> helpCommand = CBCommand.help.builder
                    .executes(ctx -> showCommands(ctx.getSource()))
                    .build();

            LiteralCommandNode<ServerCommandSource> setHomeCommand = CBCommand.setHome.builder
                    .executes(ctx -> setHomeCoordinates(ctx.getSource()))
                    .build();

            LiteralCommandNode<ServerCommandSource> getHomeCommand = CBCommand.getHome.builder
                    .executes(ctx -> getPlayerHomeCoordinates(ctx.getSource()))
                    .build();

            dispatcher.getRoot().addChild(cbCommand);

            cbCommand.addChild(meCommand);
            cbCommand.addChild(helpCommand);
            cbCommand.addChild(setHomeCommand);
            cbCommand.addChild(getHomeCommand);
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
    public static int showCommands(ServerCommandSource source) throws CommandSyntaxException {
        String listStr = "";
        CBCommand[] commandList = CBCommand.values();
        for(int i = 0; i < commandList.length; i++) {
            CBCommand command = commandList[i];
            listStr += "/cb " + command.name + " - " + command.description;

            if(i != commandList.length - 1) {
                listStr += "\n";
            }
        }

        source.getPlayer().sendMessage(new LiteralText(listStr), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int setHomeCoordinates(ServerCommandSource source) throws CommandSyntaxException {
        try {
            final ServerPlayerEntity player = source.getPlayer();

            if (!(player instanceof PlayerEntity)) {
                throw new Exception("Player is not valid PlayerEntity");
            }

            final String coords = getPlayerCoords(source);
            ((PlayerEntityExt) player).setHomeCoordinates(coords);
            player.sendMessage(new LiteralText("Home coordinates set! - " + coords), false);

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            if(e instanceof CommandSyntaxException) {
                throw (CommandSyntaxException) e;
            }
            e.printStackTrace();
            return 0;
        }
    }

    public static int getPlayerHomeCoordinates(ServerCommandSource source) throws CommandSyntaxException {
//        final List<ServerPlayerEntity> playerList = source.getServer().getPlayerManager().getPlayerList();
//
//        for(ServerPlayerEntity player: playerList) {
//            System.out.println(player.getDisplayName());
//        }

        final ServerPlayerEntity player = source.getPlayer();
        final String coordinates = ((PlayerEntityExt) player).getHomeCoordinates();

        final LiteralText message = new LiteralText(player.getDisplayName().getString() + "'s home: " + coordinates);
        player.sendMessage(message, false);

        return Command.SINGLE_SUCCESS;
    }
}