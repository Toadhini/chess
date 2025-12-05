package server;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Session, Integer> sessionToGame = new ConcurrentHashMap<>();

    //Add session to a game
    public void addSessionToGame(Integer gameID, Session session){
        gameSessions.computeIfAbsent(gameID, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionToGame.put(session, gameID);
    }

    //Remove a session from its game
    public void removeSession(Session session){
        Integer gameID = sessionToGame.remove(session);
        if (gameID != null){
            Set<Session> sessions = gameSessions.get(gameID);
            if(sessions != null){
                sessions.remove(session);
                if(sessions.isEmpty()){
                    gameSessions.remove(gameID);
                }
            }
        }
    }

    //Send message to specific sesssion
    public void sendMessage(Session session, String message)throws IOException{
        if (session.isOpen()){
            session.getRemote().sendString(message);
        }
    }
}
