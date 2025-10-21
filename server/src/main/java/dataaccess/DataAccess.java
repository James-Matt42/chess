package dataaccess;

import chess.*;

import java.util.HashSet;

public interface DataAccess {
    void clear() throws DataAccessException;

    void createUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void createGame(GameData gameData) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    HashSet<GameData> listGames() throws DataAccessException;

    void updateGame(int gameID, GameData gameData) throws DataAccessException;

    void createAuth(AuthData authData) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    void deleteAuth(String authToken) throws DataAccessException;
}
