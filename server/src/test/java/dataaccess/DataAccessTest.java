package dataaccess;

import chess.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    final UserData user = new UserData("Joe", "myPassword", "jj@j.com");

    @Test
    void clear() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.createUser(user);
        db.clear();
        assertNull(db.getUser("Joe"));
    }

    @Test
    void createUser() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.createUser(user);
        assertEquals(user, db.getUser(user.username()));
    }

    @Test
    void createUserFails() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
//        No username more than 50 characters long
        String newUsername = "SamTheFamWhoLikesSpamJamAndButteredYamOnHisHamSammiches";
        assertThrows(DataAccessException.class, () -> db.createUser(new UserData(newUsername,
                "myPassword", "jj@j.com")));
    }

    @Test
    void getUser() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        db.createUser(user);
        assertEquals(db.getUser(user.username()), user);
//        Make sure that passwords and emails don't have to be unique
        var newUser = new UserData("Bob", user.password(), user.email());
        db.createUser(newUser);
        assertEquals(db.getUser(newUser.username()), newUser);
    }

    @Test
    void getUserFails() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        assertNull(db.getUser(user.username()));
        db.createUser(user);
        assertNull(db.getUser(user.username() + "2.0"));
    }

    @Test
    void createGame() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        GameData gameData = new GameData(1, "Bob", null, null, null, "MyGame", new ChessGame());
//        Successfully create game without throwing an error
        db.createGame(gameData);
    }

    @Test
    void createGameFails() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        GameData gameData = new GameData(1, "Bob", null, null, null, "MyGame", new ChessGame());
        db.createGame(gameData);
//        We can't have null as game name
        assertThrows(DataAccessException.class, () -> db.createGame(new GameData(1, "Bob", null, null, null, null, new ChessGame())));
    }

    @Test
    void getGame() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        GameData gameData = new GameData(1, "Bob", null, null, null, "MyGame", new ChessGame());
        db.createGame(gameData);
        assertEquals(gameData, db.getGame(1));
        GameData gameData2 = new GameData(2, null, null, "Bob", null, "MyGame2", new ChessGame());
        db.createGame(gameData2);
        assertEquals(gameData2, db.getGame(2));
    }

    @Test
    void getGameFails() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        GameData gameData = new GameData(1, "Bob", null, null, null, "MyGame", new ChessGame());
        db.createGame(gameData);
        assertNull(db.getGame(42));
    }

    @Test
    void listGames() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();

        GameData gameData = new GameData(1, "Bob", null, null, null, "MyGame", new ChessGame());
        db.createGame(gameData);
        GameData gameData2 = new GameData(2, null, null, "joe", null, "Game2", new ChessGame());
        db.createGame(gameData2);

        var games = db.listGames();

//        Both games should be found
        assertTrue(games.contains(gameData));
        assertTrue(games.contains(gameData2));
    }

    @Test
    void listGamesFails() throws DataAccessException {
//        There isn't a way to fail listGames(), so we'll make sure there aren't unexpected games
        SQLDataAccess db = new SQLDataAccess();
        db.clear();

        var nada = db.listGames();
        assertEquals(new HashSet<GameData>(), nada);
    }

    @Test
    void updateGame() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        GameData gameData = new GameData(1, "Bob", null, null, null, "MyGame", new ChessGame());
        db.createGame(gameData);
        GameData gameData2 = new GameData(2, null, null, "Bob", null, "MyGame2", new ChessGame());

        db.updateGame(1, gameData2);

        var games = db.listGames();
        assertEquals(1, games.size());
        assertTrue(games.contains(gameData2));
    }

    @Test
    void updateGameFails() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        GameData gameData = new GameData(1, "Bob", null, null, null, "MyGame", new ChessGame());
        db.createGame(gameData);
        GameData gameData2 = new GameData(2, null, null, "Bob", null, "MyGame2", new ChessGame());
        db.createGame(gameData2);

//        Try and make two copies of the same game through updateGame()
        assertThrows(DataAccessException.class, () -> db.updateGame(1, gameData2));
    }

    @Test
    void createAuth() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();

//        Shouldn't throw an error (we'll test for if it's persistent in the getAuth() test)
        db.createAuth(new AuthData("12345", "Bob"));
    }

    @Test
    void createAuthFails() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();

        assertThrows(Exception.class, () -> db.createAuth(null));
    }

    @Test
    void getAuth() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        db.createUser(user);
        var authData = new AuthData("12345", user.username());
        db.createAuth(authData);
        assertTrue(db.getAuth(authData.authToken()).contains(authData));
    }

    @Test
    void getAuthFails() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        db.createUser(user);
        var authData = new AuthData("12345", user.username());
        db.createAuth(authData);
        assertTrue(db.getAuth(authData.authToken() + "2.0").isEmpty());
    }

    @Test
    void deleteAuth() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        db.createUser(user);

//        Make sure it deletes all occurrences of the given authToken
        var authData = new AuthData("12345", user.username());
        db.createAuth(authData);

        db.deleteAuth(authData.authToken());

        assertTrue(db.getAuth(authData.authToken()).isEmpty());
    }

    @Test
    void deleteAuthFails() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        db.createUser(user);
        var authData = new AuthData("12345", user.username());
        db.createAuth(authData);

//        Attempt something malicious
        db.deleteAuth("*");
        db.deleteAuth("%");
        db.deleteAuth("x'; DROP DATABASE chess;");

        assertTrue(db.getAuth(authData.authToken()).contains(authData));
    }
}