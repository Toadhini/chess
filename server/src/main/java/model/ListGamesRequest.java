package model;

import java.util.Collection;

public record ListGamesRequest(Collection<GameData> games, String message) {
    public ListGamesRequest(Collection<GameData> games){
        this(games, null);
    }
    public ListGamesRequest(String message){
        this(null, message);
    }
}
