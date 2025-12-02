package ui;

import client.ServerFacade;
import model.*;
import chess.*;
import java.util.*;

import static ui.EscapeSequences.*;

/**
 * Handles all postlogin UI operations (after user is authenticated)
 */
public class PostloginUI {
    private final ServerFacade serverFacade;
    private final String authToken;
    private Map<Integer, Integer> gameNumberToId = new HashMap<>();

    public PostloginUI(ServerFacade serverFacade, String authToken) {
        this.serverFacade = serverFacade;
        this.authToken = authToken;
    }

    /**
     * Displays help text for postlogin commands
     */
    public String help() {
        StringBuilder sb = new StringBuilder();
        sb.append(SET_TEXT_BOLD).append("\nAvailable Commands:").append(RESET_TEXT_BOLD_FAINT).append("\n");
        sb.append(SET_TEXT_COLOR_BLUE).append("  create <gameName>").append(RESET_TEXT_COLOR)
                .append(" - Create a new game\n");
        sb.append(SET_TEXT_COLOR_BLUE).append("  list").append(RESET_TEXT_COLOR)
                .append(" - List all games\n");
        sb.append(SET_TEXT_COLOR_BLUE).append("  play <gameNumber> <WHITE|BLACK>").append(RESET_TEXT_COLOR)
                .append(" - Join a game as a player\n");
        sb.append(SET_TEXT_COLOR_BLUE).append("  observe <gameNumber>").append(RESET_TEXT_COLOR)
                .append(" - Watch a game as an observer\n");
        sb.append(SET_TEXT_COLOR_BLUE).append("  logout").append(RESET_TEXT_COLOR)
                .append(" - Logout of your account\n");
        sb.append(SET_TEXT_COLOR_BLUE).append("  help").append(RESET_TEXT_COLOR)
                .append(" - Display this help message\n");
        return sb.toString();
    }

    /**
     * Logs out the current user
     */
    public void logout() throws Exception {
        serverFacade.logout(authToken);
    }

    /**
     * Creates a new game
     */
    public String createGame(String gameName) throws Exception {
        CreateGameResult result = serverFacade.createGame(authToken, gameName);
        return SET_TEXT_COLOR_GREEN + "Game '" + gameName + "' created successfully" + RESET_TEXT_COLOR;
    }

    /**
     * Lists all games on the server
     */
    public String listGames() throws Exception {
        ListGamesResult result = serverFacade.listGames(authToken);

        if (result.games() == null || result.games().isEmpty()) {
            return "No games available. Create one with 'create <gameName>'";
        }

        // Clear and rebuild the game number mapping
        gameNumberToId.clear();

        StringBuilder sb = new StringBuilder();
        sb.append(SET_TEXT_BOLD).append("\nGames:").append(RESET_TEXT_BOLD_FAINT).append("\n");

        int gameNumber = 1;
        for (GameData game : result.games()) {
            gameNumberToId.put(gameNumber, game.gameID());

            sb.append(SET_TEXT_COLOR_YELLOW).append(gameNumber).append(". ").append(RESET_TEXT_COLOR);
            sb.append(SET_TEXT_BOLD).append(game.gameName()).append(RESET_TEXT_BOLD_FAINT);
            sb.append(" - White: ");
            sb.append(game.whiteUsername() != null ? game.whiteUsername() : "(empty)");
            sb.append(", Black: ");
            sb.append(game.blackUsername() != null ? game.blackUsername() : "(empty)");
            sb.append("\n");

            gameNumber++;
        }

        return sb.toString();
    }

    /**
     * Joins a game as a player
     */
    public String playGame(int gameNumber, String color) throws Exception {
        Integer gameID = gameNumberToId.get(gameNumber);
        if (gameID == null) {
            return "Invalid game number. Use 'list' to see available games.";
        }

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return "Color must be WHITE or BLACK";
        }

        serverFacade.joinGame(authToken, gameID, color);

        // Get the game data to draw the board
        ListGamesResult result = serverFacade.listGames(authToken);
        GameData gameData = null;
        for (GameData game : result.games()) {
            if (game.gameID() == gameID) {
                gameData = game;
                break;
            }
        }

        if (gameData != null && gameData.game() != null) {
            System.out.println(SET_TEXT_COLOR_GREEN + "Joined game as " + color + RESET_TEXT_COLOR);
            ChessGame.TeamColor perspective = color.equals("WHITE") ?
                    ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            BoardDrawer.drawBoard(gameData.game(), perspective);
            return "";
        } else {
            return SET_TEXT_COLOR_GREEN + "Joined game as " + color + RESET_TEXT_COLOR;
        }
    }

    /**
     * Joins a game as an observer
     */
    public String observeGame(int gameNumber) throws Exception {
        Integer gameID = gameNumberToId.get(gameNumber);
        if (gameID == null) {
            return "Invalid game number. Use 'list' to see available games.";
        }

        try {
            serverFacade.joinGame(authToken, gameID, null);

            // Get the game data to draw the board
            ListGamesResult result = serverFacade.listGames(authToken);
            GameData gameData = null;
            for (GameData game : result.games()) {
                if (game.gameID() == gameID) {
                    gameData = game;
                    break;
                }
            }

            if (gameData != null && gameData.game() != null) {
                System.out.println(SET_TEXT_COLOR_GREEN + "Observing game" + RESET_TEXT_COLOR);
                // Observers see from white's perspective
                BoardDrawer.drawBoard(gameData.game(), ChessGame.TeamColor.WHITE);
                return "";
            } else {
                return SET_TEXT_COLOR_GREEN + "Observing game" + RESET_TEXT_COLOR;
            }
        } catch (Exception e) {
            throw e; // Re-throw exceptions
        }
    }
}