package dataaccess;

import chess.*;

import java.util.List;

public interface DataAccess {
    void clear();

    void createUser(UserData user);

    UserData getUser(String username);

    void createGame(int ID);

    void getGame();

    List<String> listGames();

    void updateGame();

    void createAuth(AuthData authData);

    AuthData getAuth(String authToken);

    void deleteAuth(String authToken);
}
