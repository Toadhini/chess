package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTeam;
    private boolean gameOver = false;


    public boolean isGameOver(){
        return gameOver;
    }

    public void setGameOver(boolean gameOver){
        this.gameOver = gameOver;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTeam == chessGame.currentTeam;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTeam);
    }

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.currentTeam = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        //Return array of valid moves for chosen piece
        ChessPiece playerPiece = board.getPiece(startPosition);
        if (playerPiece == null) {
            return null;
        }
        
        //Get all possible moves for the piece (including moves that might leave king in check)
        Collection<ChessMove> possibleMoves = playerPiece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        
        //Filter out moves that would leave the king in check
        TeamColor pieceColor = playerPiece.getTeamColor();
        for (ChessMove move : possibleMoves) {
            if (!wouldLeaveInCheck(move, pieceColor)) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece piece = board.getPiece(startPosition);

        //Check for piece in position
        if (piece == null) {
            throw new InvalidMoveException("No piece at start position");
        }

        //Check for turn
        if (piece.getTeamColor() != currentTeam) {
            throw new InvalidMoveException("Not your turn");
        }
        Collection<ChessMove> valid = validMoves(startPosition);
        if (!valid.contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        //Execute the move
        ChessPiece pieceToPlace = piece;

        //Pawn promotion
        if (move.getPromotionPiece() != null) {
            pieceToPlace = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        board.addPiece(endPosition, pieceToPlace);
        board.addPiece(startPosition, null);

        //Change turn
        if (currentTeam == TeamColor.WHITE) {
            currentTeam = TeamColor.BLACK;
        } else {
            currentTeam = TeamColor.WHITE;
        }


    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        //Check if king is in danger from enemy pieces
        ChessPosition kingPosition = null;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                //Check for what team king is on
                if (piece != null &&
                        piece.getTeamColor() == teamColor &&
                        piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = pos;
                    break;
                }
            }
            if (kingPosition != null) {
                break;
            }
        }

        //Check if opponent can attack the king position
        TeamColor opponentColor = (teamColor == TeamColor.BLACK) ? TeamColor.WHITE : TeamColor.BLACK;
        return canOpponentAttackPosition(opponentColor, kingPosition);
    }

    /**
     * Helper method to check if opponent can attack a position
     */
    private boolean canOpponentAttackPosition(TeamColor opponentColor, ChessPosition targetPosition) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece == null || piece.getTeamColor() != opponentColor) {
                    continue;
                }

                Collection<ChessMove> moves = piece.pieceMoves(board, pos);
                for (ChessMove move : moves) {
                    if (move.getEndPosition().equals(targetPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Helper method to check if a team has any valid moves
     *
     * @param teamColor which team to check
     * @return True if the team has at least one valid move
     */
    private boolean hasValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (!moves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // Must be in check AND have no valid moves
        return isInCheck(teamColor) && !hasValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // Must NOT be in check AND have no valid moves
        return !isInCheck(teamColor) && !hasValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        //Return current board set up
        return board;
    }

    public boolean wouldLeaveInCheck(ChessMove move, TeamColor teamColor) {
        //Simulate the move and check if it leaves the king in check
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessPiece movingPiece = board.getPiece(startPos);
        ChessPiece capturePiece = board.getPiece(endPos);

        //Pawn promotion
        ChessPiece pieceToPlace = movingPiece;
        if (move.getPromotionPiece() != null) {
            pieceToPlace = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
        }

        //Check the move
        board.addPiece(endPos, pieceToPlace);
        board.addPiece(startPos, null);

        //Check if this leaves the king in check
        boolean checked = isInCheck(teamColor);

        //Undo the move
        board.addPiece(startPos, movingPiece);
        board.addPiece(endPos, capturePiece);

        return checked;
    }
}
