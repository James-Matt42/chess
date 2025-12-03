package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.BLACK_BISHOP;
import static ui.EscapeSequences.BLACK_KING;
import static ui.EscapeSequences.BLACK_KNIGHT;
import static ui.EscapeSequences.BLACK_PAWN;
import static ui.EscapeSequences.BLACK_QUEEN;
import static ui.EscapeSequences.BLACK_ROOK;
import static ui.EscapeSequences.EMPTY;
import static ui.EscapeSequences.RESET_BG_COLOR;
import static ui.EscapeSequences.SET_BG_COLOR_DARK_GREY;
import static ui.EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
import static ui.EscapeSequences.WHITE_BISHOP;
import static ui.EscapeSequences.WHITE_KING;
import static ui.EscapeSequences.WHITE_KNIGHT;
import static ui.EscapeSequences.WHITE_PAWN;
import static ui.EscapeSequences.WHITE_QUEEN;
import static ui.EscapeSequences.WHITE_ROOK;

public class DrawBoard {

    public static void drawBoard(ChessBoard board, ChessGame.TeamColor playerColor) {
        final String borderColor = SET_BG_COLOR_DARK_GREEN;
        int colStart = 8;
        int colStop = 0;
        int colStep = -1;

        int rowStart = 1;
        int rowStop = 9;
        int rowStep = 1;
        if (Objects.equals(playerColor, ChessGame.TeamColor.BLACK)) {
            colStart = 1;
            colStop = 9;
            colStep = 1;

            rowStart = 8;
            rowStop = 0;
            rowStep = -1;
        }

        StringBuilder boardString = new StringBuilder();

        String topBottomRow = getTopBottomRow(borderColor, playerColor);
        boardString.append(topBottomRow);

        for (int i = colStart; i != colStop; i += colStep) {
            boardString.append(borderColor).append(" ").append(i).append(" ");
            for (int j = rowStart; j != rowStop; j += rowStep) {
                var piece = board.getPiece(new ChessPosition(i, j));
                boardString.append(getBGColor(i, j)).append(getPieceType(piece));
            }
            boardString.append(borderColor).append(" ").append(i).append(" ").append(RESET_BG_COLOR).append("\n");
        }

        boardString.append(topBottomRow);

        System.out.println(boardString);
    }

    private static String getTopBottomRow(String borderColor, ChessGame.TeamColor playerColor) {
        ArrayList<String> rowChars = new ArrayList<>(List.of(" A ", " B ", " C ", " D ", " E ", " F ", " G ", " H "));
        if (Objects.equals(playerColor, ChessGame.TeamColor.BLACK)) {
            rowChars = new ArrayList<>(rowChars.reversed());
        }

        StringBuilder topBottomRow = new StringBuilder();
        topBottomRow.append(borderColor).append(EMPTY);
        for (var c : rowChars) {
            topBottomRow.append(c);
        }
        topBottomRow.append(EMPTY + RESET_BG_COLOR + "\n");

        return topBottomRow.toString();
    }

    private static String getBGColor(int i, int j) {
        if ((i + j) % 2 == 0) {
            return SET_BG_COLOR_DARK_GREY;
        }
        return SET_BG_COLOR_LIGHT_GREY;
    }

    private static String getPieceType(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }

        return switch (piece.getTeamColor()) {
            case WHITE -> {
                switch (piece.getPieceType()) {
                    case KING -> {
                        yield WHITE_KING;
                    }
                    case QUEEN -> {
                        yield WHITE_QUEEN;
                    }
                    case BISHOP -> {
                        yield WHITE_BISHOP;
                    }
                    case KNIGHT -> {
                        yield WHITE_KNIGHT;
                    }
                    case ROOK -> {
                        yield WHITE_ROOK;
                    }
                    case PAWN -> {
                        yield WHITE_PAWN;
                    }
                    default -> {
                        yield "";
                    }
                }
            }
            case BLACK -> {
                switch (piece.getPieceType()) {
                    case KING -> {
                        yield BLACK_KING;
                    }
                    case QUEEN -> {
                        yield BLACK_QUEEN;
                    }
                    case BISHOP -> {
                        yield BLACK_BISHOP;
                    }
                    case KNIGHT -> {
                        yield BLACK_KNIGHT;
                    }
                    case ROOK -> {
                        yield BLACK_ROOK;
                    }
                    case PAWN -> {
                        yield BLACK_PAWN;
                    }
                    default -> {
                        yield "";
                    }
                }
            }
        };
    }
}
