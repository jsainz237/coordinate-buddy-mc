package com.jsainz.coordinate.buddy.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jsainz.coordinate.buddy.MyComponents;
import com.jsainz.coordinate.buddy.utils.WorldComponent;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public final class SyncedWorldComponent implements WorldComponent, AutoSyncedComponent {
    private static final String KEY = "CB-COORDS";

    private final World provider;
    private JsonObject coordinates;

    public SyncedWorldComponent(World provider) {
        this.provider = provider;
    }

    @Override
    public String getPlayerHomeCoordinates(String playerName) {
        final JsonElement coords = coordinates.get(playerName);
        if(coords == null) {
            return null;
        }

        return coords.getAsString();
    }

    @Override
    public void setHomeCoordinates(String playerName, String coords) {
        if(this.coordinates == null) {
            this.coordinates = new JsonObject();
        }
        coordinates.addProperty(playerName, coords);
        MyComponents.CBWORLD.sync(this.provider);
    }

    @Override
    public String[] getAllSavedPlayerNames() {
        if(this.coordinates == null) {
            return new String[0];
        }

        return this.coordinates.keySet().toArray(new String[0]);
    }

    @Override
    public void clearPlayerHomeCoordinates(String playerName) {
        this.coordinates.remove(playerName);
    }


    @Override
    public void readFromNbt(NbtCompound tag) {
        final String jsonStr = tag.getString(KEY);
        this.coordinates = (JsonObject) JsonParser.parseString(jsonStr);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if(this.coordinates == null) {
            return;
        }
        final String jsonStr = this.coordinates.toString();
        tag.putString(KEY, jsonStr);
    }


}
