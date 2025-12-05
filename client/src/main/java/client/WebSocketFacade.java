package client;

import chess.ChessGame;
import com.google.gson.Gson;
import com.sun.nio.sctp.NotificationHandler;
import commands.UserGameCommand;
import commands.MakeMoveCommand;
import jakarta.websocket.*;
import messages.ServerMessage;
import messages.LoadGameMessage;
import messages.ErrorMessage;
import messages.NotificationMessage;
import chess.ChessMove;

import javax.websocket.*;
import java.net.URI;

@ClientEndpoint
public class WebSocketFacade {
    private Session session;
    private final NotificationHandler notificationHandler;
    private final Gson gson = new Gson();

    public interface NotificationHandler{
        void onLoadGame(ChessGame game);
        void onNotifications(String message);
        void onError(String errorMessage);
    }

    public WebSocketFacade(String url, NotificationHandler handler)throws Exception{
        this.notificationHandler = handler;
        URI uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
    }

    @OnOpen
    public void onOpen(Session session){
        System.out.println("WebSocket connected");
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME -> {
                    LoadGameMessage loadGame = gson.fromJson(message, LoadGameMessage.class);
                    notificationHandler.onLoadGame(loadGame.getGame());
                }
                case NOTIFICATION -> {
                    NotificationMessage notification = gson.fromJson(message, NotificationMessage.class);
                    notificationHandler.onNotification(notification.getMessage());
                }
                case ERROR -> {
                    ErrorMessage error = gson.fromJson(message, ErrorMessage.class);
                    notificationHandler.onError(error.getErrorMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

}
