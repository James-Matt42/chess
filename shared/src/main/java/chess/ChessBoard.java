package chess;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private final ChessPiece[][] board;

    public ChessBoard() {
        this.board = new ChessPiece[8][8];
    }

    public ChessBoard(ChessPiece[][] board) {
        this.board = board;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
//        Takes in position as 1-based index and converts it to 0-based index on the squares board
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    public void removePiece(ChessPosition position) {
        board[position.getRow() - 1][position.getColumn() - 1] = null;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        setBackRow(ChessGame.TeamColor.WHITE);
        setBackRow(ChessGame.TeamColor.BLACK);
        setPawns(ChessGame.TeamColor.WHITE);
        setPawns(ChessGame.TeamColor.BLACK);
        clearMiddleSpace();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();
        for (int rowIndex = 8; rowIndex > 0; rowIndex--) {
            boardString.append("|");
            for (int colIndex = 1; colIndex < 9; colIndex++) {
                var piece = getPiece(new ChessPosition(rowIndex, colIndex));
                if (piece == null) {
                    boardString.append(" |");
                } else {
                    String pieceString;
                    switch (piece.getPieceType()) {
                        case KING -> pieceString = "k";
                        case QUEEN -> pieceString = "q";
                        case BISHOP -> pieceString = "b";
                        case KNIGHT -> pieceString = "n";
                        case ROOK -> pieceString = "r";
                        case PAWN -> pieceString = "p";
                        default -> pieceString = " ";
                    }
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        pieceString = pieceString.toUpperCase();
                    }
                    boardString.append(pieceString).append("|");
                }
            }
            boardString.append("\n");
        }
        boardString.append("\n");
        return boardString.toString();
//        System.out.print(boardString);
    }


    public ChessPiece[][] getBoardCopy() {
        ChessPiece[][] newBoard = new ChessPiece[8][8];

        for (int rowIndex = 1; rowIndex < 9; rowIndex++) {
            for (int colIndex = 1; colIndex < 9; colIndex++) {
                var piece = getPiece(new ChessPosition(rowIndex, colIndex));
                if (piece != null) {
                    newBoard[rowIndex - 1][colIndex - 1] = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                }
            }
        }

        return newBoard;
    }

    public Collection<ChessPosition> getStartPositions(ChessGame.TeamColor color) {
        var moves = getColorMoves(color);
        var positions = new HashSet<ChessPosition>();
        for (var move : moves) {
            positions.add(move.getStartPosition());
        }
        return positions;
    }

    public Collection<ChessPosition> getEndPositions(ChessGame.TeamColor color) {
        var moves = getColorMoves(color);
        var positions = new HashSet<ChessPosition>();
        for (var move : moves) {
            positions.add(move.getEndPosition());
        }
        return positions;
    }

    public Collection<ChessMove> getColorMoves(ChessGame.TeamColor color) {
        var moves = new HashSet<ChessMove>();
        int rowIndex = 1;
        for (var row : board) {
            int colIndex = 1;
            for (var piece : row) {
                if (piece != null && piece.getTeamColor() == color) {
                    moves.addAll(piece.pieceMoves(this, new ChessPosition(rowIndex, colIndex)));
                }
                colIndex++;
            }
            rowIndex++;
        }

        return moves;
    }

    public ChessPosition findKing(ChessGame.TeamColor color) {
//        System.out.print(toString());
        int rowIndex = 1;
        for (var row : board) {
            int colIndex = 1;
            for (var piece : row) {
                if (piece != null && piece.getTeamColor() == color && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return new ChessPosition(rowIndex, colIndex);
                }
                colIndex++;
            }
            rowIndex++;
        }
        throw new RuntimeException(String.format("%s king was not found", color.toString()));
    }

    public void makeMove(ChessMove move) {
        var start = move.getStartPosition();
        var end = move.getEndPosition();
        var promotionPiece = move.getPromotionPiece();
        ChessPiece piece = promotionPiece == null ? getPiece(start) : new ChessPiece(getPiece(start).getTeamColor(), promotionPiece);

//        Each function moves the proper rook or kills the proper pawn respectively if necessary
        performCastle(start, end, piece);
        performEnPassant(start, end, piece);

        removePiece(start);
        addPiece(end, piece);
    }

    private void performCastle(ChessPosition start, ChessPosition end, ChessPiece piece) {
        if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING) {
            ChessPosition rookStart;
            ChessPosition rookEnd;
//            Left castle
            if (start.getColumn() == 5 && end.getColumn() == 3) {
                rookStart = new ChessPosition(start.getRow(), 1);
                rookEnd = new ChessPosition(start.getRow(), 4);
                removePiece(rookStart);
                addPiece(rookEnd, new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.ROOK));
            } else if (start.getColumn() == 5 && end.getColumn() == 7) { // Right castle
                rookStart = new ChessPosition(start.getRow(), 8);
                rookEnd = new ChessPosition(start.getRow(), 6);
                removePiece(rookStart);
                addPiece(rookEnd, new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.ROOK));
            }
        }
    }

    private void performEnPassant(ChessPosition start, ChessPosition end, ChessPiece piece) {
//        Check that there's nothing in the diagonal spot being moved to
        if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN && start.getColumn() != end.getColumn() && getPiece(end) == null) {
            removePiece(new ChessPosition(start.getRow(), end.getColumn()));
        }
    }

    private void setBackRow(ChessGame.TeamColor color) {
        int row;
        if (color == ChessGame.TeamColor.WHITE) {
            row = 0;
        } else {
            row = 7;
        }
        board[row][0] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        board[row][1] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        board[row][2] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        board[row][3] = new ChessPiece(color, ChessPiece.PieceType.QUEEN);
        board[row][4] = new ChessPiece(color, ChessPiece.PieceType.KING);
        board[row][5] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        board[row][6] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        board[row][7] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
    }

    private void setPawns(ChessGame.TeamColor color) {
        int row;
        if (color == ChessGame.TeamColor.WHITE) {
            row = 1;
        } else {
            row = 6;
        }

        for (int i = 0; i < 8; i++) {
            board[row][i] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
        }
    }

    private void clearMiddleSpace() {
        for (int row = 2; row < 6; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        }
    }
}
