package com.jsainz.coordinate.buddy.utils;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;

public interface WorldComponent extends ComponentV3 {
    String getPlayerHomeCoordinates(String playerName);
    void setHomeCoordinates(String playerName, String coords);
    String[] getAllSavedPlayerNames() throws Exception;
}
