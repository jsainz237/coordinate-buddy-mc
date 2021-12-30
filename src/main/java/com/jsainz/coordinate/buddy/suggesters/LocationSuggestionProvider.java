package com.jsainz.coordinate.buddy.suggesters;

import com.jsainz.coordinate.buddy.MyComponents;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;

public class LocationSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        World worldProvider = context.getSource().getWorld();
        final String[] locations = MyComponents.CBWORLD.get(worldProvider).getAllCommunityKeys();

        for(String location : locations) {
            builder.suggest(location);
        }

        return builder.buildFuture();
    }
}
