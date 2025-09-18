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
        else if(piece.getPieceType() == PieceType.QUEEN){
            return queenMoves(board, myPosition);
        }
        else if(piece.getPieceType() == PieceType.PAWN){
            return pawnMoves(board, myPosition);
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

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();

        // Queen moves like both rook and bishop combined
        int[][] queenDirections = {
                {1, 1},   // diagonal
                {-1, -1}, // diagonal
                {1, -1},  // diagonal
                {-1, 1},  // diagonal
                {1, 0},   // vertical
                {-1, 0},  // vertical
                {0, 1},   // horizontal
                {0, -1}   // horizontal
        };

        for(int[] direction : queenDirections){
            int rowDirection = direction[0];
            int colDirection = direction[1];

            // Keep moving in this direction until we hit something or go off the board
            int currentRow = myPosition.getRow() + rowDirection;
            int currentCol = myPosition.getColumn() + colDirection;

            while(isValidPosition(currentRow, currentCol)){
                ChessPosition targetPosition = new ChessPosition(currentRow, currentCol);
                ChessPiece targetPiece = board.getPiece(targetPosition);

                if (targetPiece == null){
                    // Empty square
                    moves.add(new ChessMove(myPosition, targetPosition, null));
                } else {
                    // There's a piece here
                    if(targetPiece.getTeamColor() != this.getTeamColor()){
                        // Enemy piece - can capture
                        moves.add(new ChessMove(myPosition, targetPosition, null));
                    }
                    // Stop sliding in this direction
                    break;
                }

                // Move to the next position in this direction
                currentRow += rowDirection;
                currentCol += colDirection;
            }
        }
        return moves;
    }
    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        
        // White pawns move up (+1), black pawns move down (-1)
        int direction = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        
        // Check forward move (one square)
        int forwardRow = myPosition.getRow() + direction;
        int forwardCol = myPosition.getColumn();
        
        if (isValidPosition(forwardRow, forwardCol)) {
            ChessPosition forwardPosition = new ChessPosition(forwardRow, forwardCol);
            ChessPiece forwardPiece = board.getPiece(forwardPosition);
            
            if (forwardPiece == null) {
                // Empty square - can move forward
                if (forwardRow == 8 || forwardRow == 1) {
                    // Promotion - pawn becomes a queen
                    moves.add(new ChessMove(myPosition, forwardPosition, PieceType.QUEEN));
                } else {
                    // Normal forward move
                    moves.add(new ChessMove(myPosition, forwardPosition, null));
                }
                
                // Check double move from starting position
                if ((this.getTeamColor() == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2) ||
                    (this.getTeamColor() == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7)) {
                    
                    int doubleRow = myPosition.getRow() + (2 * direction);
                    if (isValidPosition(doubleRow, forwardCol)) {
                        ChessPosition doublePosition = new ChessPosition(doubleRow, forwardCol);
                        if (board.getPiece(doublePosition) == null) {
                            moves.add(new ChessMove(myPosition, doublePosition, null));
                        }
                    }
                }
            }
        }
        
        // Check diagonal captures (left and right)
        int[] captureDirections = {-1, 1}; // left and right
        for (int colDirection : captureDirections) {
            int captureRow = myPosition.getRow() + direction;
            int captureCol = myPosition.getColumn() + colDirection;
            
            if (isValidPosition(captureRow, captureCol)) {
                ChessPosition capturePosition = new ChessPosition(captureRow, captureCol);
                ChessPiece capturePiece = board.getPiece(capturePosition);
                
                if (capturePiece != null && capturePiece.getTeamColor() != this.getTeamColor()) {
                    // Enemy piece - can capture
                    if (captureRow == 8 || captureRow == 1) {
                        // Promotion on capture - pawn becomes a queen
                        moves.add(new ChessMove(myPosition, capturePosition, PieceType.QUEEN));
                    } else {
                        // Normal capture
                        moves.add(new ChessMove(myPosition, capturePosition, null));
                    }
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
