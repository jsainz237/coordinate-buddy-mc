package com.jsainz.coordinate.buddy.suggesters;

import com.jsainz.coordinate.buddy.MyComponents;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;

public class PlayerSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        World worldProvider = context.getSource().getWorld();
        final String[] playerNames = MyComponents.CBWORLD.get(worldProvider).getAllSavedPlayerNames();

        for(String playerName : playerNames) {
            builder.suggest(playerName);
        }

        return builder.buildFuture();
    }

}
