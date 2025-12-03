package chess;

public record GameData(int gameID, String whiteUsername, String whiteAuthToken, String blackUsername,
                       String blackAuthToken, String gameName, ChessGame game) {
}
