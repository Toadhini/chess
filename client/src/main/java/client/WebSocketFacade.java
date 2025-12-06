package client;

import chess.ChessGame;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;
import chess.ChessMove;

import javax.websocket.*;
import org.glassfish.tyrus.client.ClientManager;
import java.net.URI;

@ClientEndpoint
public class WebSocketFacade {
    private Session session;
    private final NotificationHandler notificationHandler;
    private final Gson gson = new Gson();

    public interface NotificationHandler{
        void onLoadGame(ChessGame game);
        void onNotification(String message);
        void onError(String errorMessage);
    }

    public WebSocketFacade(String url, NotificationHandler handler)throws Exception{
        this.notificationHandler = handler;
        URI uri = new URI(url);
        WebSocketContainer container = ClientManager.createClient();
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

    @OnClose
    public void onClose(Session session) {
        System.out.println("WebSocket closed");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

    public void sendConnect(String authToken, int gameID) throws Exception {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        sendMessage(gson.toJson(command));
    }

    public void sendMakeMove(String authToken, int gameID, ChessMove move) throws Exception {
        MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
        sendMessage(gson.toJson(command));
    }

    public void sendLeave(String authToken, int gameID) throws Exception {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        sendMessage(gson.toJson(command));
    }

    public void sendResign(String authToken, int gameID) throws Exception {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        sendMessage(gson.toJson(command));
    }

    private void sendMessage(String message) throws Exception {
        if (session.isOpen()) {
            session.getBasicRemote().sendText(message);
        } else {
            throw new Exception("WebSocket session is not open");
        }
    }

    public void close() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}