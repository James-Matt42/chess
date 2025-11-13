package dataaccess;

import chess.AuthData;
import chess.GameData;
import chess.UserData;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, AuthData> authTokens = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        try {
            users.clear();
            authTokens.clear();
            games.clear();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try {
            return users.get(username);
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public int createGame(GameData gameData) throws DataAccessException {
        int gameID;
        if (games.isEmpty()) {
            gameID = 1;
        } else {
            gameID = games.keySet().stream().max(Comparator.naturalOrder()).orElse(0) + 1;
        }
        try {
            games.put(gameID, gameData);
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        return gameID;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try {
            users.put(user.username(), user);
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try {
            return games.get(gameID);
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public HashSet<GameData> listGames() throws DataAccessException {
        try {
            return new HashSet<>(games.values());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        try {
            games.replace(gameID, gameData);
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        try {
            authTokens.put(authData.authToken(), authData);
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public HashSet<AuthData> getAuth(String authToken) throws DataAccessException {
        try {
            var authDatas = new HashSet<AuthData>();
            authDatas.add(authTokens.get(authToken));
            return authDatas;
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try {
            authTokens.remove(authToken);
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
