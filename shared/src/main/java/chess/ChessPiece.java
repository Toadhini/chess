package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP){
            return bishopMoves(board, myPosition);
        }
        else if (piece.getPieceType() == PieceType.ROOK){
            return rookMoves(board, myPosition);
        }
        else if(piece.getPieceType() == PieceType.KNIGHT){
            return knightMoves(board, myPosition);
        }
        else if(piece.getPieceType() == PieceType.KING){
            return kingMoves(board, myPosition);
        }
        return List.of();

    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition){
        //Array to store possible moves
        Collection<ChessMove> moves = new ArrayList<>();

        //Possible directions bishop can move
        int[][] bishopDirections = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };

        for (int[] direction : bishopDirections) {
            int rowDirection = direction[0];
            int colDirection = direction[1];

            //Keep moving in this direction until we hit something or go off the board
            int currentRow = myPosition.getRow() + rowDirection;
            int currentCol = myPosition.getColumn() + colDirection;

            while (isValidPosition(currentRow, currentCol)) {
                ChessPosition targetPosition = new ChessPosition(currentRow, currentCol);
                ChessPiece targetPiece = board.getPiece(targetPosition);

                if (targetPiece == null) {
                    //Empty square
                    moves.add(new ChessMove(myPosition, targetPosition, null));
                } else {
                    //There's a piece here
                    if (targetPiece.getTeamColor() != this.getTeamColor()) {
                        //Enemy piece
                        moves.add(new ChessMove(myPosition, targetPosition, null));
                    }
                    //Finished moving
                    break;
                }

                //Move to the next position in this direction
                currentRow += rowDirection;
                currentCol += colDirection;
            }
        }

        return moves;
    }
    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();

        //Possible directions knight can move
        int[][] rookDirections = {
                {1, 0},
                {0, 1},
                {-1, 0},
                {0, -1}
        };

        for (int[] direction : rookDirections){
            int rowDirection = direction[0];
            int colDirection = direction[1];

            int currentRow = myPosition.getRow() + rowDirection;
            int currentCol = myPosition.getColumn() + colDirection;

            while (isValidPosition(currentRow, currentCol)){
                ChessPosition targetPosition = new ChessPosition(currentRow, currentCol);
                ChessPiece targetPiece = board.getPiece(targetPosition);

                if (targetPiece == null){
                    moves.add(new ChessMove(myPosition, targetPosition, null));
                } else {
                    if (targetPiece.getTeamColor() != this.getTeamColor()){
                        moves.add(new ChessMove(myPosition, targetPosition, null));
                    }
                    break;
                }
                //Move to the next position in this direction
                currentRow += rowDirection;
                currentCol += colDirection;
            }
        }
        return moves;
    }
    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] knightDirections = {
                {2, 1},
                {1, 2},
                {-1, 2},
                {-2, 1},
                {1, -2},
                {2, -1},
                {-1, -2},
                {-2, -1}
        };

        for (int[] direction : knightDirections){
            int rowDirection = direction[0];
            int colDirection = direction[1];

            int targetRow = myPosition.getRow() + rowDirection;
            int targetCol = myPosition.getColumn() + colDirection;

            //Not sliding piece so just a jump
            if (isValidPosition(targetRow, targetCol)){
                ChessPosition targetPosition = new ChessPosition(targetRow, targetCol);
                ChessPiece targetPiece = board.getPiece(targetPosition);

                if (targetPiece == null){
                    //Empty square
                    moves.add(new ChessMove(myPosition, targetPosition, null));
                }
                else if (targetPiece.getTeamColor() != this.getTeamColor()){
                    //Enemy piece
                    moves.add(new ChessMove(myPosition, targetPosition, null));
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();

        int[][] kingDirections = {
                {1, 1},
                {-1, -1},
                {1, -1},
                {-1, 1},
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        for(int[] direction : kingDirections){
            int rowDirection = direction[0];
            int colDirection = direction[1];

            int targetRow = myPosition.getRow() + rowDirection;
            int targetCol = myPosition.getColumn() + colDirection;

            if (isValidPosition(targetRow, targetCol)){
                ChessPosition targetPosition = new ChessPosition(targetRow, targetCol);
                ChessPiece targetPiece = board.getPiece(targetPosition);

                if (targetPiece == null){
                    moves.add(new ChessMove(myPosition, targetPosition, null));
                }
                else if (targetPiece.getTeamColor() != this.getTeamColor()){
                    moves.add(new ChessMove(myPosition, targetPosition, null));
                }
            }
        }
        return moves;
    }
    //Checks for valid position
    private boolean isValidPosition(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
