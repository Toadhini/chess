package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import commands.MakeMoveCommand;
import commands.UserGameCommand;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.*;
import messages.ErrorMessage;
import messages.LoadGameMessage;
import messages.NotificationMessage;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.Collection;

/**
 * Handles WebSocket messages for gameplay
 */
public class WebSocketHandler {
    private final ConnectionManager connectionManager;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final Gson gson = new Gson();

    public WebSocketHandler(ConnectionManager connectionManager, AuthDAO authDAO, GameDAO gameDAO) {
        this.connectionManager = connectionManager;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void onConnect(WsConnectContext ctx) {
        System.out.println("WebSocket connected: " + ctx.session.getRemoteAddress());
    }

    public void onMessage(WsMessageContext ctx) {
        Session session = ctx.session;
        String message = ctx.message();
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMakeMove(session, message);
                case LEAVE -> handleLeave(session, command);
                case RESIGN -> handleResign(session, command);
            }
        } catch (Exception e) {
            sendError(session, "Error processing command: " + e.getMessage());
        }
    }

    public void onClose(WsCloseContext ctx) {
        connectionManager.removeSession(ctx.session);
        System.out.println("WebSocket closed: " + ctx.session.getRemoteAddress());
    }

    public void onError(WsErrorContext ctx) {
        System.err.println("WebSocket error: " + (ctx.error() != null ? ctx.error().getMessage() : "Unknown error"));
    }

    /**
     * Handle CONNECT command
     */
    private void handleConnect(Session session, UserGameCommand command) {
        try {
            // Validate auth token
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            // Validate game exists
            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            // Add session to game
            connectionManager.addSessionToGame(command.getGameID(), session);

            // Send LOAD_GAME to root client
            LoadGameMessage loadGame = new LoadGameMessage(gameData.game());
            connectionManager.sendMessage(session, gson.toJson(loadGame));

            // Determine if player or observer and send notification to others
            String username = authData.username();
            String notificationMsg;

            if (username.equals(gameData.whiteUsername())) {
                notificationMsg = username + " joined the game as WHITE";
            } else if (username.equals(gameData.blackUsername())) {
                notificationMsg = username + " joined the game as BLACK";
            } else {
                notificationMsg = username + " is now observing the game";
            }

            NotificationMessage notification = new NotificationMessage(notificationMsg);
            connectionManager.broadcastExcept(command.getGameID(), session, gson.toJson(notification));

        } catch (DataAccessException e) {
            sendError(session, "Error: Database error - " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Handle MAKE_MOVE command
     */
    private void handleMakeMove(Session session, String message) {
        try {
            MakeMoveCommand command = gson.fromJson(message, MakeMoveCommand.class);

            // Validate auth token
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            // Validate game exists
            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            ChessGame game = gameData.game();
            String username = authData.username();

            // Check if game is over
            if (game.isGameOver()) {
                sendError(session, "Error: Game is over");
                return;
            }

            // Validate it's the player's turn
            ChessGame.TeamColor currentTurn = game.getTeamTurn();
            boolean isWhitePlayer = username.equals(gameData.whiteUsername());
            boolean isBlackPlayer = username.equals(gameData.blackUsername());

            if ((currentTurn == ChessGame.TeamColor.WHITE && !isWhitePlayer) ||
                    (currentTurn == ChessGame.TeamColor.BLACK && !isBlackPlayer)) {
                sendError(session, "Error: It's not your turn");
                return;
            }

            // Validate and make move
            Collection<ChessMove> validMoves = game.validMoves(command.getMove().getStartPosition());
            if (validMoves == null || !validMoves.contains(command.getMove())) {
                sendError(session, "Error: Invalid move");
                return;
            }

            game.makeMove(command.getMove());

            // Update game in database
            gameDAO.updateGame(command.getGameID(), game);

            // Broadcast LOAD_GAME to all clients
            LoadGameMessage loadGame = new LoadGameMessage(game);
            connectionManager.broadcast(command.getGameID(), gson.toJson(loadGame));

            // Send move notification to other clients
            String moveDescription = command.getMove().getStartPosition().toString() +
                    " to " + command.getMove().getEndPosition().toString();
            NotificationMessage moveNotification = new NotificationMessage(
                    username + " made move " + moveDescription
            );
            connectionManager.broadcastExcept(command.getGameID(), session, gson.toJson(moveNotification));

            // Check for check, checkmate, or stalemate
            ChessGame.TeamColor opponentColor = (currentTurn == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

            if (game.isInCheckmate(opponentColor)) {
                String opponentUsername = (opponentColor == ChessGame.TeamColor.WHITE)
                        ? gameData.whiteUsername() : gameData.blackUsername();
                NotificationMessage checkmateNotification = new NotificationMessage(
                        opponentUsername + " is in checkmate. " + username + " wins!"
                );
                connectionManager.broadcast(command.getGameID(), gson.toJson(checkmateNotification));
                game.setGameOver(true);
                gameDAO.updateGame(command.getGameID(), game);
            } else if (game.isInStalemate(opponentColor)) {
                NotificationMessage stalemateNotification = new NotificationMessage(
                        "Game ended in stalemate"
                );
                connectionManager.broadcast(command.getGameID(), gson.toJson(stalemateNotification));
                game.setGameOver(true);
                gameDAO.updateGame(command.getGameID(), game);
            } else if (game.isInCheck(opponentColor)) {
                String opponentUsername = (opponentColor == ChessGame.TeamColor.WHITE)
                        ? gameData.whiteUsername() : gameData.blackUsername();
                NotificationMessage checkNotification = new NotificationMessage(
                        opponentUsername + " is in check"
                );
                connectionManager.broadcast(command.getGameID(), gson.toJson(checkNotification));
            }

        } catch (DataAccessException e) {
            sendError(session, "Error: Database error - " + e.getMessage());
        } catch (InvalidMoveException e) {
            sendError(session, "Error: Invalid move - " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Handle LEAVE command
     */
    private void handleLeave(Session session, UserGameCommand command) {
        try {
            // Validate auth token
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            // Validate game exists
            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            String username = authData.username();

            // If player is leaving, remove them from the game
            if (username.equals(gameData.whiteUsername())) {
                GameData updatedGame = new GameData(gameData.gameID(), null, gameData.blackUsername(),
                        gameData.gameName(), gameData.game());
                gameDAO.updateGame(updatedGame);
            } else if (username.equals(gameData.blackUsername())) {
                GameData updatedGame = new GameData(gameData.gameID(), gameData.whiteUsername(), null,
                        gameData.gameName(), gameData.game());
                gameDAO.updateGame(updatedGame);
            }

            // Remove session from connection manager
            connectionManager.removeSession(session);

            // Notify other clients
            NotificationMessage notification = new NotificationMessage(username + " left the game");
            connectionManager.broadcast(command.getGameID(), gson.toJson(notification));

        } catch (DataAccessException e) {
            sendError(session, "Error: Database error - " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Handle RESIGN command
     */
    private void handleResign(Session session, UserGameCommand command) {
        try {
            // Validate auth token
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            // Validate game exists
            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            String username = authData.username();
            ChessGame game = gameData.game();

            // Check if game is already over
            if (game.isGameOver()) {
                sendError(session, "Error: Game is already over");
                return;
            }

            // Only players can resign (not observers)
            if (!username.equals(gameData.whiteUsername()) &&
                    !username.equals(gameData.blackUsername())) {
                sendError(session, "Error: Observers cannot resign");
                return;
            }

            // Mark game as over
            game.setGameOver(true);
            gameDAO.updateGame(command.getGameID(), game);

            // Notify all clients
            NotificationMessage notification = new NotificationMessage(username + " resigned");
            connectionManager.broadcast(command.getGameID(), gson.toJson(notification));

        } catch (DataAccessException e) {
            sendError(session, "Error: Database error - " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Send error message to a client
     */
    private void sendError(Session session, String errorMessage) {
        try {
            ErrorMessage error = new ErrorMessage(errorMessage);
            connectionManager.sendMessage(session, gson.toJson(error));
        } catch (IOException e) {
            System.err.println("Error sending error message: " + e.getMessage());
        }
    }
}