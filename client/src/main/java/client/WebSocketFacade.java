package client;

import chess.ChessGame;
import com.google.gson.Gson;
import com.sun.nio.sctp.NotificationHandler;
import commands.UserGameCommand;
import commands.MakeMoveCommand;
import jakarta.websocket.ClientEndpoint;
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


}
