package ui;

import chess.*;
import static ui.EscapeSequences.*;

/**
 * Draws a chess board in the terminal with proper colors and piece placement
 */
public class BoardDrawer {

    private static final String LIGHT_SQUARE = SET_BG_COLOR_WHITE;
    private static final String DARK_SQUARE = SET_BG_COLOR_DARK_GREEN;
    private static final String BORDER_COLOR = SET_BG_COLOR_LIGHT_GREY;

    /**
     * Draws the chess board from the specified perspective
     * @param game The chess game to draw
     * @param perspective The color perspective (WHITE shows a1 at bottom-left, BLACK shows a1 at top-right)
     */
    public static void drawBoard(ChessGame game, ChessGame.TeamColor perspective) {
        ChessBoard board = game.getBoard();

        if (perspective == ChessGame.TeamColor.BLACK) {
            drawBoardBlackPerspective(board);
        } else {
            drawBoardWhitePerspective(board);
        }
    }

    /**
     * Draws board from white's perspective (a1 at bottom-left)
     */
    private static void drawBoardWhitePerspective(ChessBoard board) {
        // Top border with column labels
        drawColumnLabels(false);

        // Draw rows 8 down to 1 (top to bottom)
        for (int row = 8; row >= 1; row--) {
            drawRow(board, row, false);
        }

        // Bottom border with column labels
        drawColumnLabels(false);

        System.out.println(); // Extra line for spacing
    }

    /**
     * Draws board from black's perspective (a1 at top-right)
     */
    private static void drawBoardBlackPerspective(ChessBoard board) {
        // Top border with column labels (reversed)
        drawColumnLabels(true);

        // Draw rows 1 up to 8 (top to bottom from black's view)
        for (int row = 1; row <= 8; row++) {
            drawRow(board, row, true);
        }

        // Bottom border with column labels (reversed)
        drawColumnLabels(true);

        System.out.println(); // Extra line for spacing
    }

    /**
     * Draws the column labels (a-h or h-a)
     */
    private static void drawColumnLabels(boolean reversed) {
        System.out.print(BORDER_COLOR + SET_TEXT_COLOR_BLACK);
        System.out.print("   "); // Space for row number

        if (reversed) {
            for (char col = 'h'; col >= 'a'; col--) {
                System.out.print(" " + col + " ");
            }
        } else {
            for (char col = 'a'; col <= 'h'; col++) {
                System.out.print(" " + col + " ");
            }
        }

        System.out.print("   "); // Space for row number
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
        System.out.println();
    }

    /**
     * Draws a single row of the chess board
     */
    private static void drawRow(ChessBoard board, int row, boolean reversed) {
        // Left row number
        System.out.print(BORDER_COLOR + SET_TEXT_COLOR_BLACK);
        System.out.print(" " + row + " ");
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);

        // Draw squares
        if (reversed) {
            for (int col = 8; col >= 1; col--) {
                drawSquare(board, row, col);
            }
        } else {
            for (int col = 1; col <= 8; col++) {
                drawSquare(board, row, col);
            }
        }

        // Right row number
        System.out.print(BORDER_COLOR + SET_TEXT_COLOR_BLACK);
        System.out.print(" " + row + " ");
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
        System.out.println();
    }

    /**
     * Draws a single square with its piece (if any)
     */
    private static void drawSquare(ChessBoard board, int row, int col) {
        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(position);

        // Determine square color (h1 and a8 must be light)
        boolean isLightSquare = (row + col) % 2 == 0;
        String squareColor = isLightSquare ? LIGHT_SQUARE : DARK_SQUARE;

        System.out.print(squareColor);

        if (piece == null) {
            System.out.print(EMPTY);
        } else {
            System.out.print(getPieceSymbol(piece));
        }

        System.out.print(RESET_BG_COLOR);
    }

    /**
     * Gets the Unicode symbol for a chess piece
     */
    private static String getPieceSymbol(ChessPiece piece) {
        ChessGame.TeamColor color = piece.getTeamColor();
        ChessPiece.PieceType type = piece.getPieceType();

        // Set text color based on team
        String textColor = (color == ChessGame.TeamColor.WHITE) ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;

        String symbol = switch (type) {
            case KING -> (color == ChessGame.TeamColor.WHITE) ? WHITE_KING : BLACK_KING;
            case QUEEN -> (color == ChessGame.TeamColor.WHITE) ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> (color == ChessGame.TeamColor.WHITE) ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> (color == ChessGame.TeamColor.WHITE) ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> (color == ChessGame.TeamColor.WHITE) ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> (color == ChessGame.TeamColor.WHITE) ? WHITE_PAWN : BLACK_PAWN;
        };

        return textColor + symbol + RESET_TEXT_COLOR;
    }
}