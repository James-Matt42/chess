package service;

import chess.*;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import org.mindrot.jbcrypt.BCrypt;
import websocket.commands.MakeMoveCommand;

import java.util.HashSet;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() throws DataAccessException {
        dataAccess.clear();
    }

    private String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public AuthData register(UserData user) throws AlreadyTakenException, BadRequestException, DataAccessException {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new BadRequestException("bad request");
        }
        if (user.username().isBlank() || user.password().isBlank() || user.email().isBlank()) {
            throw new BadRequestException("bad request");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new AlreadyTakenException("already taken");
        }

        dataAccess.createUser(new UserData(user.username(), encryptPassword(user.password()), user.email()));
        var authData = new AuthData(generateAuthToken(), user.username());
        dataAccess.createAuth(authData);
        return authData;
    }

    public AuthData login(LoginRequest loginRequest) throws Exception {
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw new BadRequestException("bad request");
        }
        if (loginRequest.username().isBlank() || loginRequest.password().isBlank()) {
            throw new BadRequestException("bad request");
        }

        var userData = dataAccess.getUser(loginRequest.username());
        if (userData == null) {
            throw new InvalidAuthException("unauthorized");
        }
        if (!BCrypt.checkpw(loginRequest.password(), userData.password())) {
            throw new InvalidAuthException("unauthorized");
        }

        var authData = new AuthData(generateAuthToken(), loginRequest.username());
        dataAccess.createAuth(authData);
        return authData;
    }

    public void logout(String authToken) throws DataAccessException {
        verifyAuth(authToken);
        dataAccess.deleteAuth(authToken);
    }

    public HashSet<ReturnGameData> listGames(String authToken) throws InvalidAuthException, DataAccessException {
        verifyAuth(authToken);
        var games = dataAccess.listGames();
        var newGames = new HashSet<ReturnGameData>();
        for (var game : games) {
            var newGame = new ReturnGameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName());
            newGames.add(newGame);
        }
        return newGames;
    }

    public int createGame(String authToken, String gameName) throws BadRequestException, InvalidAuthException, DataAccessException {
        if (gameName == null || gameName.isBlank()) {
            throw new BadRequestException("bad request");
        }

        verifyAuth(authToken);
        var game = new GameData(0, null, null, null, null, gameName, new ChessGame());
        return dataAccess.createGame(game);
    }

    public void joinGame(String authToken, String playerColor, int gameID)
            throws AlreadyTakenException, BadRequestException, InvalidAuthException, DataAccessException {
        var authData = verifyAuth(authToken);
        var gameData = dataAccess.getGame(gameID);

        if (gameData == null) {
            throw new BadRequestException("bad request");
        }

        if (playerColor == null) {
            throw new BadRequestException("bad request");
        } else if (playerColor.equals("WHITE")) {
            if (gameData.whiteUsername() == null || gameData.whiteUsername().isEmpty()) {
                var newGameData = new GameData(gameData.gameID(), authData.username(), authData.authToken(),
                        gameData.blackUsername(), gameData.blackAuthToken(), gameData.gameName(), gameData.game());
                dataAccess.updateGame(gameID, newGameData);
            } else {
                throw new AlreadyTakenException("already taken");
            }
        } else if (playerColor.equals("BLACK")) {
            if (gameData.blackUsername() == null || gameData.blackUsername().isEmpty()) {
                var newGameData = new GameData(gameData.gameID(),
                        gameData.whiteUsername(), gameData.whiteAuthToken(),
                        authData.username(), authData.authToken(), gameData.gameName(), gameData.game());
                dataAccess.updateGame(gameID, newGameData);
            } else {
                throw new AlreadyTakenException("already taken");
            }
        } else {
            throw new BadRequestException("bad request");
        }
    }

    public GameData getGame(String authToken, int gameID) throws Exception {
        var authData = verifyAuth(authToken);
        var gameData = dataAccess.getGame(gameID);

        if (gameData == null) {
            throw new BadRequestException("bad request");
        }

//        Validate that the authData username matches one of the usernames from the game
        if (!(authData.username().equals(gameData.whiteUsername()) || authData.username().equals(gameData.blackUsername()))) {
            throw new InvalidAuthException("unauthorized");
        }

        return gameData;
    }

    //    For getting the game for observers
    public GameData getGame(int gameID) throws Exception {
        var gameData = dataAccess.getGame(gameID);

        if (gameData == null) {
            throw new BadRequestException("bad request");
        }

        return gameData;
    }

    private AuthData verifyAuth(String authToken) throws InvalidAuthException, DataAccessException {
        if (authToken == null || authToken.isBlank()) {
            throw new InvalidAuthException("unauthorized");
        }

        AuthData data = null;
        var authData = dataAccess.getAuth(authToken);

        for (var d : authData) {
            if (d.authToken().equals(authToken)) {
                data = d;
            }
        }

        if (data == null) {
            throw new InvalidAuthException("unauthorized");
        }

        return data;
    }

    public static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    public void makeMove(MakeMoveCommand command) throws Exception {
        GameData gameData = dataAccess.getGame(command.getGameID());
        ChessGame game = gameData.game();

//        Make sure that the player is authorized
        var playerTurn = game.getTeamTurn();
        String player;
        if (playerTurn.equals(ChessGame.TeamColor.WHITE)) {
            player = gameData.whiteUsername();
        } else {
            player = gameData.blackUsername();
        }
        var user = getUser(command.getAuthToken());
        if (user == null) {
            throw new Exception("User not found");
        }
        if (!user.equals(player)) {
            throw new Exception("You are not authorized to make that move");
        }

        game.makeMove(command.getMove());
        dataAccess.updateGame(gameData.gameID(), new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.whiteAuthToken(),
                gameData.blackUsername(), gameData.blackAuthToken(), gameData.gameName(), game));
    }

    public String getUser(String authToken) throws Exception {
        var authDatas = dataAccess.getAuth(authToken);

        for (var authData : authDatas) {
            if (authData.authToken().equals(authToken)) {
                return authData.username();
            }
        }
        throw new Exception("unauthorized");
    }

    public void removeUserFromGame(String authToken, int gameID) throws Exception {
        var game = getGame(gameID);
        if (game == null) {
            return;
        }
        if (game.whiteAuthToken() != null && game.whiteAuthToken().equals(authToken)) {
            dataAccess.updateGame(gameID, new GameData(gameID, null, null,
                    game.blackUsername(), game.blackAuthToken(), game.gameName(), game.game()));
        } else if (game.blackAuthToken() != null && game.blackAuthToken().equals(authToken)) {
            dataAccess.updateGame(gameID, new GameData(gameID, game.whiteUsername(), game.whiteAuthToken(),
                    null, null, game.gameName(), game.game()));
        }
    }
}
