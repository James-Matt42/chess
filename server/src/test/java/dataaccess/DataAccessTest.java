package dataaccess;

import chess.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    @Test
    void getUser() {
    }

    @Test
    void createUser() throws DataAccessException {
        MemoryDataAccess db = new MemoryDataAccess();
        var user = new UserData("Joe", "jj@j.com", "myPassword");
        db.createUser(user);
        assertEquals(user, db.getUser(user.username()));
    }

    @Test
    void clear() throws DataAccessException {
        MemoryDataAccess db = new MemoryDataAccess();
        var user = new UserData("Joe", "jj@j.com", "myPassword");
        db.createUser(user);
        db.clear();
        assertNull(db.getUser("Joe"));
    }
}