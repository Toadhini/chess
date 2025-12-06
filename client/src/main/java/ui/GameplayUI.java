package ui;

import chess.*;
import client.WebSocketFacade;

import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static ui.EscapeSequences.*;

/**
 * Handles the gameplay UI and commands
 */
public class GameplayUI implements WebSocketFacade.NotificationHandler {
    private final WebSocketFacade webSocketFacade;
    private final BoardDrawer boardDrawer;
    private ChessGame currentGame;
    private final String authToken;
    private final int gameID;
    private final ChessGame.TeamColor playerColor; // null if observer
    private final Scanner scanner;
    private boolean inGame = true;

    public GameplayUI(String serverUrl, String authToken, int gameID, ChessGame.TeamColor playerColor) throws Exception {
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;
        this.boardDrawer = new BoardDrawer();
        this.scanner = new Scanner(System.in);
        
        // Connect to WebSocket
        String wsUrl = serverUrl.replace("http://", "ws://") + "/ws";
        this.webSocketFacade = new WebSocketFacade(wsUrl, this);
        
        // Send CONNECT command
        webSocketFacade.sendConnect(authToken, gameID);
    }

    public void run() {
        System.out.println(SET_TEXT_COLOR_GREEN + "\nEntered gameplay. Type 'help' for commands." + RESET_TEXT_COLOR);
        
        while (inGame) {
            try {
                System.out.print("\n" + SET_TEXT_COLOR_BLUE + "[GAMEPLAY] >>> " + RESET_TEXT_COLOR);
                String line = scanner.nextLine().trim();
                
                if (line.isEmpty()) {
                    continue;
                }
                
                String[] tokens = line.split("\\s+");
                String command = tokens[0].toLowerCase();
                
                switch (command) {
                    case "help" -> printHelp();
                    case "redraw" -> redrawBoard();
                    case "leave" -> handleLeave();
                    case "move" -> handleMove(tokens);
                    case "resign" -> handleResign();
                    case "highlight" -> handleHighlight(tokens);
                    default -> System.out.println("Unknown command. Type 'help' for available commands.");
                }
            } catch (Exception e) {
                System.out.println(SET_TEXT_COLOR_RED + "Error: " + e.getMessage() + RESET_TEXT_COLOR);
            }
        }
    }

    private void printHelp() {
        System.out.println(SET_TEXT_BOLD + "\nGameplay Commands:" + RESET_TEXT_BOLD_FAINT);
        System.out.println("  help - Display available commands");
        System.out.println("  redraw - Redraw the chess board");
        System.out.println("  leave - Leave the game");
        System.out.println("  move <FROM> <TO> - Make a move (e.g., 'move e2 e4')");
        System.out.println("  resign - Forfeit the game");
        System.out.println("  highlight <POSITION> - Highlight legal moves for a piece (e.g., 'highlight e2')");
    }

    private void redrawBoard() {
        if (currentGame != null) {
            boardDrawer.drawBoard(currentGame.getBoard(), playerColor, null);
        } else {
            System.out.println("No game loaded yet.");
        }
    }

    private void handleLeave() {
        try {
            webSocketFacade.sendLeave(authToken, gameID);
            webSocketFacade.close();
            inGame = false;
            System.out.println(SET_TEXT_COLOR_GREEN + "Left the game." + RESET_TEXT_COLOR);
        } catch (Exception e) {
            System.out.println(SET_TEXT_COLOR_RED + "Error leaving game: " + e.getMessage() + RESET_TEXT_COLOR);
        }
    }

    private void handleMove(String[] tokens) {
        if (playerColor == null) {
            System.out.println(SET_TEXT_COLOR_RED + "Observers cannot make moves." + RESET_TEXT_COLOR);
            return;
        }
        
        if (tokens.length != 3) {
            System.out.println("Usage: move <FROM> <TO> (e.g., 'move e2 e4')");
            return;
        }
        
        try {
            ChessPosition start = parsePosition(tokens[1]);
            ChessPosition end = parsePosition(tokens[2]);
            
            if (start == null || end == null) {
                System.out.println(SET_TEXT_COLOR_RED + "Invalid position format. Use format like 'e2' or 'a1'" + RESET_TEXT_COLOR);
                return;
            }
            
            // Check if this is a pawn promotion move
            ChessMove move = createMove(start, end);
            
            
            webSocketFacade.sendMakeMove(authToken, gameID, move);
        } catch (Exception e) {
            System.out.println(SET_TEXT_COLOR_RED + "Error making move: " + e.getMessage() + RESET_TEXT_COLOR);
        }
    }

    private void handleResign() {
        if (playerColor == null) {
            System.out.println(SET_TEXT_COLOR_RED + "Observers cannot resign." + RESET_TEXT_COLOR);
            return;
        }
        
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (confirmation.equals("yes") || confirmation.equals("y")) {
            try {
                webSocketFacade.sendResign(authToken, gameID);
                System.out.println(SET_TEXT_COLOR_YELLOW + "You have resigned from the game." + RESET_TEXT_COLOR);
            } catch (Exception e) {
                System.out.println(SET_TEXT_COLOR_RED + "Error resigning: " + e.getMessage() + RESET_TEXT_COLOR);
            }
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void handleHighlight(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Usage: highlight <POSITION> (e.g., 'highlight e2')");
            return;
        }
        
        try {
            ChessPosition position = parsePosition(tokens[1]);
            if (position == null) {
                System.out.println(SET_TEXT_COLOR_RED + "Invalid position format. Use format like 'e2' or 'a1'" + RESET_TEXT_COLOR);
                return;
            }
            
            Collection<ChessMove> validMoves = currentGame.validMoves(position);
            if (validMoves == null || validMoves.isEmpty()) {
                System.out.println("No valid moves for piece at " + tokens[1]);
                return;
            }
            
            // Create set of positions to highlight
            Set<ChessPosition> highlightPositions = new HashSet<>();
            highlightPositions.add(position); // Highlight the piece's current position
            for (ChessMove move : validMoves) {
                highlightPositions.add(move.getEndPosition());
            }
            
            boardDrawer.drawBoard(currentGame.getBoard(), playerColor, highlightPositions);
        } catch (Exception e) {
            System.out.println(SET_TEXT_COLOR_RED + "Error highlighting moves: " + e.getMessage() + RESET_TEXT_COLOR);
        }
    }

    private ChessMove createMove(ChessPosition start, ChessPosition end) {
        ChessPiece piece = currentGame.getBoard().getPiece(start);
        
        // Check if this is a pawn promotion
        if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            boolean isPromotion = (piece.getTeamColor() == ChessGame.TeamColor.WHITE && end.getRow() == 8)
                    || (piece.getTeamColor() == ChessGame.TeamColor.BLACK && end.getRow() == 1);
            
            if (isPromotion) {
                return createPromotionMove(start, end);
            }
        }
        
        return new ChessMove(start, end, null);
    }
    
    private ChessMove createPromotionMove(ChessPosition start, ChessPosition end) {
        System.out.print("Promote to (Q/R/B/N): ");
        String promotionInput = scanner.nextLine().trim().toLowerCase();
        ChessPiece.PieceType promotionPiece = switch (promotionInput) {
            case "q" -> ChessPiece.PieceType.QUEEN;
            case "r" -> ChessPiece.PieceType.ROOK;
            case "b" -> ChessPiece.PieceType.BISHOP;
            case "n" -> ChessPiece.PieceType.KNIGHT;
            default -> ChessPiece.PieceType.QUEEN;
        };
        if (!promotionInput.matches("[qrbn]")) {
            System.out.println("Invalid promotion piece. Defaulting to Queen.");
        }
        return new ChessMove(start, end, promotionPiece);
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            return null;
        }
        
        char col = pos.charAt(0);
        char row = pos.charAt(1);
        
        if (col < 'a' || col > 'h' || row < '1' || row > '8') {
            return null;
        }
        
        int colNum = col - 'a' + 1; // a=1, b=2, ..., h=8
        int rowNum = row - '0';     // 1-8
        
        return new ChessPosition(rowNum, colNum);
    }

    @Override
    public void onLoadGame(ChessGame game) {
        this.currentGame = game;
        System.out.println("\n" + SET_TEXT_COLOR_GREEN + "Game board updated:" + RESET_TEXT_COLOR);
        boardDrawer.drawBoard(game.getBoard(), playerColor, null);
    }

    @Override
    public void onNotification(String message) {
        System.out.println("\n" + SET_TEXT_COLOR_YELLOW + "Notification: " + message + RESET_TEXT_COLOR);
    }

    @Override
    public void onError(String errorMessage) {
        System.out.println("\n" + SET_TEXT_COLOR_RED + errorMessage + RESET_TEXT_COLOR);
    }
}
