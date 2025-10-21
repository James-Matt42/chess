package service;

import chess.*;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;

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

        dataAccess.createUser(user);
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
//            Check provided password
        if (!userData.password().equals(loginRequest.password())) {
            throw new InvalidAuthException("unauthorized");
        }

        var authData = new AuthData(generateAuthToken(), loginRequest.username());
        dataAccess.createAuth(authData);
        return authData;
    }

    public void logout(String authToken) throws DataAccessException {
        var authData = verifyAuth(authToken);
        dataAccess.deleteAuth(authData.authToken());
    }

    public HashSet<GameData> listGames(String authToken) throws InvalidAuthException, DataAccessException {
        verifyAuth(authToken);
        return dataAccess.listGames();
    }

    public int createGame(String authToken, String gameName) throws BadRequestException, InvalidAuthException, DataAccessException {
        if (gameName == null || gameName.isBlank()) {
            throw new BadRequestException("bad request");
        }

        verifyAuth(authToken);
        this.gameID++;
        var game = new GameData(gameID, "", "", gameName, new ChessGame());
        dataAccess.createGame(game);
        return gameID;
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws AlreadyTakenException, BadRequestException, InvalidAuthException, DataAccessException {
        var authData = verifyAuth(authToken);
        var gameData = dataAccess.getGame(gameID);

        if (gameData == null) {
            throw new BadRequestException("bad request");
        }

        if (playerColor.equals("WHITE")) {
            if (gameData.whiteUsername().isEmpty()) {
                var newGameData = new GameData(gameData.gameID(), authData.username(), gameData.blackUsername(), gameData.gameName(), gameData.game());
                dataAccess.updateGame(gameID, newGameData);
            } else {
                throw new AlreadyTakenException("already taken");
            }
        } else if (playerColor.equals("BLACK")) {
            if (gameData.blackUsername().isEmpty()) {
                var newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), authData.username(), gameData.gameName(), gameData.game());
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

        var authData = dataAccess.getAuth(authToken);

        if (authData == null) {
            throw new InvalidAuthException("unauthorized");
        }

        return authData;
    }

    public static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
