package model;

import java.util.Collection;

public record ListGamesResult(Collection<GameData> games, String message) {
    public ListGamesResult(Collection<GameData> games){
        this(games, null);
    }
    public ListGamesResult(String message){
        this(null, message);
    }
}
