package service;

import chess.*;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;
    private int gameID = 0;

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
        this.gameID++;
        var game = new GameData(gameID, null, null, gameName, new ChessGame());
        dataAccess.createGame(game);
        return gameID;
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
                var newGameData = new GameData(gameData.gameID(), authData.username(),
                        gameData.blackUsername(), gameData.gameName(), gameData.game());
                dataAccess.updateGame(gameID, newGameData);
            } else {
                throw new AlreadyTakenException("already taken");
            }
        } else if (playerColor.equals("BLACK")) {
            if (gameData.blackUsername() == null || gameData.blackUsername().isEmpty()) {
                var newGameData = new GameData(gameData.gameID(),
                        gameData.whiteUsername(), authData.username(), gameData.gameName(), gameData.game());
                dataAccess.updateGame(gameID, newGameData);
            } else {
                throw new AlreadyTakenException("already taken");
            }
        } else {
            throw new BadRequestException("bad request");
        }
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
}
