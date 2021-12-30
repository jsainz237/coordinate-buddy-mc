package com.jsainz.coordinate.buddy.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jsainz.coordinate.buddy.MyComponents;
import com.jsainz.coordinate.buddy.utils.WorldComponent;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public final class SyncedWorldComponent implements WorldComponent, AutoSyncedComponent {
    private static final String HOME_KEY = "CB-COORDS-HOME";
    private static final String COMMUNITY_KEY = "CB-COORDS-COMMUNITY";

    private final World provider;
    private JsonObject homeCoordinates;
    private JsonObject communityCoordinates;

    public SyncedWorldComponent(World provider) {
        this.provider = provider;
    }

    @Override
    public String getPlayerHomeCoordinates(String playerName) {
        final JsonElement coords = homeCoordinates.get(playerName);
        if(coords == null) {
            return null;
        }

        return coords.getAsString();
    }

    @Override
    public void setHomeCoordinates(String playerName, String coords) {
        if(this.homeCoordinates == null) {
            this.homeCoordinates = new JsonObject();
        }
        homeCoordinates.addProperty(playerName, coords);
        MyComponents.CBWORLD.sync(this.provider);
    }

    @Override
    public String[] getAllSavedPlayerNames() {
        if(this.homeCoordinates == null) {
            return new String[0];
        }

        return this.homeCoordinates.keySet().toArray(new String[0]);
    }

    @Override
    public void clearPlayerHomeCoordinates(String playerName) {
        this.homeCoordinates.remove(playerName);
    }

    @Override
    public String getCommunityCoords(String name) {
        final JsonElement coords = communityCoordinates.get(name);
        if(coords == null) {
            return null;
        }

        return coords.getAsString();
    }

    @Override
    public void setCommunityCoords(String name, String coords) {
        if(this.communityCoordinates == null) {
            this.communityCoordinates = new JsonObject();
        }
        communityCoordinates.addProperty(name, coords);
        MyComponents.CBWORLD.sync(this.provider);
    }

    @Override
    public String[] getAllCommunityKeys() {
        if(this.communityCoordinates == null) {
            return new String[0];
        }

        return this.communityCoordinates.keySet().toArray(new String[0]);
    }

    @Override
    public void clearCommunityCoords(String name) {
        this.communityCoordinates.remove(name);
    }


    @Override
    public void readFromNbt(NbtCompound tag) {
        final String homeJsonStr = tag.getString(HOME_KEY);
        this.homeCoordinates = (JsonObject) JsonParser.parseString(homeJsonStr);

        final String communityJsonStr = tag.getString(COMMUNITY_KEY);
        this.communityCoordinates = (JsonObject) JsonParser.parseString(communityJsonStr);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if(this.homeCoordinates != null) {
            final String homeJsonStr = this.homeCoordinates.toString();
            tag.putString(HOME_KEY, homeJsonStr);
        }
        if(this.communityCoordinates != null) {
            final String communityJsonStr = this.communityCoordinates.toString();
            tag.putString(COMMUNITY_KEY, communityJsonStr);
        }
    }


}
