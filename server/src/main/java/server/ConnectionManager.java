package server;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Session, Integer> sessionToGame = new ConcurrentHashMap<>();

    public void addSessionToGame(Integer gameID, Session session){
        gameSessions.computeIfAbsent(gameID, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionToGame.put(session, gameID);
    }
}
