package dataaccess;

import chess.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    final UserData user = new UserData("Joe", "jj@j.com", "myPassword");

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
    void getUser() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        db.createUser(user);
        assertEquals(db.getUser(user.username()), user);
    }
}