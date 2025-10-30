package dataaccess;

import chess.ChessGame;
import chess.GameData;
import chess.UserData;
import org.junit.jupiter.api.Test;
import service.BadRequestException;

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
        GameData gameData = new GameData(1, "Bob", null, "MyGame", new ChessGame());
//        Successfully create game without throwing an error
        db.createGame(gameData);
    }

    @Test
    void createGameFails() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        GameData gameData = new GameData(1, "Bob", null, "MyGame", new ChessGame());
        db.createGame(gameData);
//        We can't use the same gameID twice
        assertThrows(DataAccessException.class, () -> db.createGame(gameData));
    }

    @Test
    void getGame() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        GameData gameData = new GameData(1, "Bob", null, "MyGame", new ChessGame());
        db.createGame(gameData);
        assertEquals(gameData, db.getGame(1));
    }

    @Test
    void getGameFails() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.clear();
        GameData gameData = new GameData(1, "Bob", null, "MyGame", new ChessGame());
        db.createGame(gameData);
        assertNull(db.getGame(42));
    }
}