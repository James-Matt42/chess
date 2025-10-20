package dataaccess;

import chess.AuthData;
import chess.UserAuth;
import chess.UserData;

import java.util.HashMap;
import java.util.List;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createGame(int ID) {

    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }


    @Override
    public void getGame() {

    }

    @Override
    public List<String> listGames() {
        return List.of();
    }

    @Override
    public void updateGame() {

    }

    @Override
    public void createAuth(AuthData authData) {

    }

    @Override
    public UserAuth getAuth(AuthData authData) {
        return null;
    }

    @Override
    public void deleteAuth() {

    }
}
