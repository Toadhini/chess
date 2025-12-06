package chess;

import java.util.*;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
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

    /**
     * Helper method for sliding pieces (bishop, rook, queen)
     * Adds moves in given directions until blocked
     */
    private void addSlidingMoves(ChessBoard board, ChessPosition myPosition,
                                  Collection<ChessMove> moves, int[][] directions) {
        for (int[] direction : directions) {
            int rowDirection = direction[0];
            int colDirection = direction[1];
            int currentRow = myPosition.getRow() + rowDirection;
            int currentCol = myPosition.getColumn() + colDirection;

            while (isValidPosition(currentRow, currentCol)) {
                ChessPosition targetPosition = new ChessPosition(currentRow, currentCol);
                ChessPiece targetPiece = board.getPiece(targetPosition);

                if (targetPiece == null) {
                    moves.add(new ChessMove(myPosition, targetPosition, null));
                } else {
                    if (targetPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, targetPosition, null));
                    }
                    break;
                }
                currentRow += rowDirection;
                currentCol += colDirection;
            }
        }
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] bishopDirections = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        addSlidingMoves(board, myPosition, moves, bishopDirections);
        return moves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] rookDirections = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        addSlidingMoves(board, myPosition, moves, rookDirections);
        return moves;
    }
    /**
     * Helper method for non-sliding pieces (knight, king)
     * Adds single-step moves in given directions
     */
    private void addSingleStepMoves(ChessBoard board, ChessPosition myPosition,
                                     Collection<ChessMove> moves, int[][] directions) {
        for (int[] direction : directions) {
            int targetRow = myPosition.getRow() + direction[0];
            int targetCol = myPosition.getColumn() + direction[1];

            if (isValidPosition(targetRow, targetCol)) {
                ChessPosition targetPosition = new ChessPosition(targetRow, targetCol);
                ChessPiece targetPiece = board.getPiece(targetPosition);

                if (targetPiece == null || targetPiece.getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, targetPosition, null));
                }
            }
        }
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] knightDirections = {
                {2, 1}, {1, 2}, {-1, 2}, {-2, 1},
                {1, -2}, {2, -1}, {-1, -2}, {-2, -1}
        };
        addSingleStepMoves(board, myPosition, moves, knightDirections);
        return moves;
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] kingDirections = {
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1},
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        addSingleStepMoves(board, myPosition, moves, kingDirections);
        return moves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] queenDirections = {
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1},
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        addSlidingMoves(board, myPosition, moves, queenDirections);
        return moves;
    }
    private void addPromotionMoves(ChessPosition start, ChessPosition end, Collection<ChessMove> moves) {
        moves.add(new ChessMove(start, end, PieceType.QUEEN));
        moves.add(new ChessMove(start, end, PieceType.BISHOP));
        moves.add(new ChessMove(start, end, PieceType.ROOK));
        moves.add(new ChessMove(start, end, PieceType.KNIGHT));
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves = new ArrayList<>();
        int direction = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        
        //Forward move
        int forwardRow = myPosition.getRow() + direction;
        int forwardCol = myPosition.getColumn();
        
        if (isValidPosition(forwardRow, forwardCol)) {
            ChessPosition forwardPosition = new ChessPosition(forwardRow, forwardCol);
            if (board.getPiece(forwardPosition) == null) {
                if (forwardRow == 8 || forwardRow == 1) {
                    addPromotionMoves(myPosition, forwardPosition, moves);
                } else {
                    moves.add(new ChessMove(myPosition, forwardPosition, null));
                }
                
                //Double move from starting position
                boolean isStartingPosition = (this.getTeamColor() == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2)
                        || (this.getTeamColor() == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7);
                if (isStartingPosition) {
                    int doubleRow = myPosition.getRow() + (2 * direction);
                    ChessPosition doublePosition = new ChessPosition(doubleRow, forwardCol);
                    if (board.getPiece(doublePosition) == null) {
                        moves.add(new ChessMove(myPosition, doublePosition, null));
                    }
                }
            }
        }
        
        //Capture moves
        int[] captureDirections = {-1, 1};
        for (int colDirection : captureDirections) {
            int captureRow = myPosition.getRow() + direction;
            int captureCol = myPosition.getColumn() + colDirection;
            
            if (!isValidPosition(captureRow, captureCol)) {
                continue;
            }
            
            ChessPosition capturePosition = new ChessPosition(captureRow, captureCol);
            ChessPiece capturePiece = board.getPiece(capturePosition);
            
            if (capturePiece != null && capturePiece.getTeamColor() != this.getTeamColor()) {
                if (captureRow == 8 || captureRow == 1) {
                    addPromotionMoves(myPosition, capturePosition, moves);
                } else {
                    moves.add(new ChessMove(myPosition, capturePosition, null));
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
