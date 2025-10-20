package dataaccess;

import chess.AuthData;
import chess.GameData;
import chess.UserData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, AuthData> authTokens = new HashMap<>();
    private final HashSet<GameData> games = new HashSet<>();

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createGame(GameData gameData) {
        games.add(gameData);
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }


    @Override
    public void getGame() {

    }

    @Override
    public HashSet<GameData> listGames() {
        return games;
    }

    @Override
    public void updateGame() {

    }

    @Override
    public void createAuth(AuthData authData) {
        authTokens.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }
}
