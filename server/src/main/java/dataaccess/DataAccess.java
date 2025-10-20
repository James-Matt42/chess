package dataaccess;

import chess.*;

import java.util.HashSet;
import java.util.List;

public interface DataAccess {
    void clear();

    void createUser(UserData user);

    UserData getUser(String username);

    void createGame(GameData gameData);

    void getGame();

    HashSet<GameData> listGames();

    void updateGame();

    void createAuth(AuthData authData);

    AuthData getAuth(String authToken);

    void deleteAuth(String authToken);
}
