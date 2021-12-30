package com.jsainz.coordinate.buddy.utils;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;

public interface WorldComponent extends ComponentV3 {
    // Home coordinates
    String getPlayerHomeCoordinates(String playerName);
    void setHomeCoordinates(String playerName, String coords);
    String[] getAllSavedPlayerNames();
    void clearPlayerHomeCoordinates(String playerName);

    // Community Coordinates
    String getCommunityCoords(String name);
    void setCommunityCoords(String name, String coords);
    String[] getAllCommunityKeys();
    void clearCommunityCoords(String name);
}
