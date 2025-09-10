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
        var piece = board.getPiece(myPosition);
        switch (piece.getPieceType()) {
            case BISHOP -> {
                return bishopPieceMoves(board, myPosition);
            }
            case KING -> {
                return kingPieceMoves(board, myPosition);
            }
            case KNIGHT -> {
                return knightPieceMoves(board, myPosition);
            }
            case PAWN -> {
                return pawnPieceMoves(board, myPosition);
            }
            case QUEEN -> {
                return queenPieceMoves(board, myPosition);
            }
            case ROOK -> {
                return rookPieceMoves(board, myPosition);
            }
        }
        return List.of();
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

    private boolean validPosition(int row, int col) {
        return row > 0 && row < 9 && col > 0 && col < 9;
    }

    private boolean validPosition(ChessPosition position) {
        return position.getRow() > 0 && position.getRow() < 9 && position.getColumn() > 0 && position.getColumn() < 9;
    }

    private void validateAndAdd(ChessPosition pos, ChessBoard board, ChessPiece piece, ChessPosition myPosition, HashSet<ChessMove> moves) {
        if (validPosition(pos) && (board.getPiece(pos) == null || (board.getPiece(pos).getTeamColor() != piece.getTeamColor()))) {
            moves.add(new ChessMove(myPosition, pos, null));
        }
    }

    private void validateAndAdd(ChessPosition pos, ChessBoard board, ChessPiece piece, ChessPosition myPosition, HashSet<ChessMove> moves, ChessPiece.PieceType promotionPiece) {
        if (validPosition(pos) && (board.getPiece(pos) == null || (board.getPiece(pos).getTeamColor() != piece.getTeamColor()))) {
            moves.add(new ChessMove(myPosition, pos, promotionPiece));
        }
    }

    private void addDiagonalMoves(HashSet<ChessMove> moves, int rowOffset, int colOffset, int rowLimit, ChessPosition myPosition, ChessPiece piece, ChessBoard board) {
        int col = myPosition.getColumn() + colOffset;
        for (int row = myPosition.getRow() + rowOffset; row != rowLimit; row += rowOffset) {
            ChessPosition pos = new ChessPosition(row, col);
            if (validPosition(pos)) { // Continues until it gets to an invalid position
                if (board.getPiece(pos) == null) { // If there's nothing there, we continue
                    moves.add(new ChessMove(myPosition, pos, null));
                } else { // If there's a piece in the way, we check if we can take it, and then break
                    if (board.getPiece(pos).getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, pos, null));
                    }
                    break;
                }
            } else { // If the position is invalid, we break
                break;
            }
            col += colOffset;
        }
    }

    private Collection<ChessMove> bishopPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        var moves = new HashSet<ChessMove>();

        addDiagonalMoves(moves, 1, 1, 9, myPosition, piece, board);
        addDiagonalMoves(moves, 1, -1, 9, myPosition, piece, board);
        addDiagonalMoves(moves, -1, 1, 0, myPosition, piece, board);
        addDiagonalMoves(moves, -1, -1, 0, myPosition, piece, board);

        return moves;
    }

    private Collection<ChessMove> kingPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        var moves = new HashSet<ChessMove>();

//        Validate every surrounding position
        for (int new_row = row - 1; new_row < row + 2; new_row++) {
            for (int new_col = col - 1; new_col < col + 2; new_col++) {
                if (new_row == 0 && new_col == 0) {
                    continue;
                }
                ChessPosition pos = new ChessPosition(new_row, new_col);
                validateAndAdd(pos, board, piece, myPosition, moves);
            }
        }

        return moves;
    }

    private Collection<ChessMove> knightPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        var moves = new HashSet<ChessMove>();

//        Iterate over possible positions by row
//        i is a counter to help with iteration logic
        int i = -2;
        for (int new_row = row - 2; new_row < row + 3; new_row++) {
            if (i == 0) {
                i++;
                continue;
            }
            ChessPosition pos;
            ChessPosition pos2;
//            Case that new_row == row - 2 || new_row == row + 2
            if (i * i == 4) {
                pos = new ChessPosition(new_row, col - 1);
                pos2 = new ChessPosition(new_row, col + 1);
            } else {  // Case that new_row == row - 1 || new_row == row + 1
                pos = new ChessPosition(new_row, col - 2);
                pos2 = new ChessPosition(new_row, col + 2);
            }
//            Validate both positions
            validateAndAdd(pos, board, piece, myPosition, moves);
            validateAndAdd(pos2, board, piece, myPosition, moves);
            i++;
        }

        return moves;
    }

    private Collection<ChessMove> pawnPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        var moves = new HashSet<ChessMove>();

        int rowOffset;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            rowOffset = 1;
        } else {
            rowOffset = -1;
        }

//        Validate normal positions
        for (int new_col = col - 1; new_col < col + 2; new_col++) {
            var pos = new ChessPosition(row + rowOffset, new_col);
//            validateAndAdd(pos, board, piece, myPosition, moves);
            if ((validPosition(pos) && new_col == col && board.getPiece(pos) == null) || (validPosition(pos) && new_col != col && board.getPiece(pos) != null && board.getPiece(pos).getTeamColor() != piece.getTeamColor())) {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && row + rowOffset == 8 || piece.getTeamColor() == ChessGame.TeamColor.BLACK && row + rowOffset == 1) {
                    moves.add(new ChessMove(myPosition, pos, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, pos, PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, pos, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, pos, PieceType.KNIGHT));
                } else {
                    moves.add(new ChessMove(myPosition, pos, null));
                }
            }
        }
//        Validate beginning positions
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2 || piece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7) {
            var pos = new ChessPosition(row + (2 * rowOffset), col);
            if (validPosition(pos) && board.getPiece(pos) == null && board.getPiece(new ChessPosition(row + rowOffset, col)) == null) {
                moves.add(new ChessMove(myPosition, pos, null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> queenPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        var moves = new HashSet<ChessMove>();


        return moves;
    }

    private Collection<ChessMove> rookPieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        var moves = new HashSet<ChessMove>();


        return moves;
    }
}
