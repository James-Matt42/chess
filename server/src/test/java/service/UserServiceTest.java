package service;

import chess.*;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.SQLDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private final UserData user = new UserData("Joe", "myPassword", "jj@j.com");

    private UserService newService() throws DataAccessException {
        SQLDataAccess db = new SQLDataAccess();
        return new UserService(db);
    }

    @BeforeEach
    void setup() throws DataAccessException {
        var service = newService();
        service.clear();
    }

    @Test
    void clear() throws Exception {
        var service = newService();
        service.register(user);
        service.clear();
        assertThrows(Exception.class, () -> service.login(new LoginRequest(user.username(), user.password())));
    }

    @Test
    void register() throws Exception {
        var service = newService();
        var authData = service.register(user);
        assertNotNull(authData);
        assertEquals(user.username(), authData.username());
        assertFalse(authData.authToken().isEmpty());
    }

    @Test
    void registerInvalid() throws Exception {
        var service = newService();

//        Try a field as null
        var user = new UserData(null, "myPassword", "jj@j.com");
        assertThrows(BadRequestException.class, () -> service.register(user));

//        Try a field as empty string
        var user2 = new UserData("Joe", "", "jj@j.com");
        assertThrows(BadRequestException.class, () -> service.register(user2));

//        Try registering the same user twice
        var user3 = new UserData("Joe", "myPassword", "jj@j.com");
        var user4 = new UserData("Joe", "myPassword", "jj@j.com");
        service.register(user3);
        assertThrows(AlreadyTakenException.class, () -> service.register(user4));
    }

    @Test
    void login() throws Exception {
        var service = newService();
        service.clear();
        service.register(user);
        AuthData authData = service.login(new LoginRequest(user.username(), user.password()));
        assertNotNull(authData);
        assertEquals(user.username(), authData.username());
        assertFalse(authData.authToken().isEmpty());
    }

    @Test
    void loginInvalid() throws Exception {
        var service = newService();
        service.register(user);

//        Not valid username or password
        assertThrows(BadRequestException.class, () -> service.login(new LoginRequest(null, user.password())));
        assertThrows(BadRequestException.class, () -> service.login(new LoginRequest("", user.password())));

//        Incorrect username or password
        assertThrows(InvalidAuthException.class, () -> service.login(new LoginRequest(user.username(), "Hello there")));
        assertThrows(InvalidAuthException.class, () -> service.login(new LoginRequest("Bob", user.password())));
    }

    @Test
    void logout() throws Exception {
        var service = newService();
        service.register(user);
        var authData = service.login(new LoginRequest(user.username(), user.password()));
        service.logout(authData.authToken());
    }

    @Test
    void logoutInvalid() throws Exception {
        var service = newService();
        service.register(user);
        service.login(new LoginRequest(user.username(), user.password()));

        assertThrows(InvalidAuthException.class, () -> service.logout(""));
        assertThrows(InvalidAuthException.class, () -> service.logout(null));
        assertThrows(InvalidAuthException.class, () -> service.logout("xyz"));
    }

    @Test
    void listGames() throws Exception {
        var service = newService();
        service.register(user);
        var authData = service.login(new LoginRequest(user.username(), user.password()));
        var games = service.listGames(authData.authToken());
        assertTrue(games.isEmpty());
    }

    @Test
    void listGamesInvalid() throws Exception {
        var service = newService();
        service.register(user);
        service.login(new LoginRequest(user.username(), user.password()));
        assertThrows(InvalidAuthException.class, () -> service.listGames("Hello there"));
    }

    @Test
    void createGame() throws Exception {
        var service = newService();
        service.register(user);
        var authData = service.login(new LoginRequest(user.username(), user.password()));

        var gameName = "MyNewGame";
        var gameID = service.createGame(authData.authToken(), gameName);
        var sameGame = new ReturnGameData(gameID, null, null, gameName);

        var games = service.listGames(authData.authToken());

        assertTrue(games.contains(sameGame));
    }

    @Test
    void createGameInvalid() throws Exception {
        var service = newService();
        service.register(user);
        var authData = service.login(new LoginRequest(user.username(), user.password()));

        var gameName = "MyNewGame";

        assertThrows(BadRequestException.class, () -> service.createGame(authData.authToken(), null));
        assertThrows(BadRequestException.class, () -> service.createGame(authData.authToken(), ""));
        assertThrows(InvalidAuthException.class, () -> service.createGame("xyz", gameName));
    }

    @Test
    void joinGame() throws Exception {
        var service = newService();
        service.register(user);
        var authData = service.login(new LoginRequest(user.username(), user.password()));

        var user2 = new UserData("Bob", "goodPassword", "bob@builder.com");
        service.register(user2);
        var authData2 = service.login(new LoginRequest(user2.username(), user2.password()));

        var gameName = "MyNewGame";
        var gameID = service.createGame(authData.authToken(), gameName);

        service.joinGame(authData.authToken(), "WHITE", gameID);
        service.joinGame(authData2.authToken(), "BLACK", gameID);
    }

    @Test
    void joinGameInvalid() throws Exception {
        var service = newService();
        service.register(user);
        var authData = service.login(new LoginRequest(user.username(), user.password()));

        var user2 = new UserData("Bob", "goodPassword", "bob@builder.com");
        service.register(user2);
        var authData2 = service.login(new LoginRequest(user2.username(), user2.password()));

        var user3 = new UserData("Bill", "anotherPassword4Me", "Gates@money.com");
        service.register(user3);
        var authData3 = service.login(new LoginRequest(user3.username(), user3.password()));

        var gameName = "MyNewGame";
        var gameID = service.createGame(authData.authToken(), gameName);

        assertThrows(InvalidAuthException.class, () -> service.joinGame("Hey there", "WHITE", gameID));
        assertThrows(BadRequestException.class, () -> service.joinGame(authData.authToken(), "BEIGE", gameID));
        assertThrows(BadRequestException.class, () -> service.joinGame(authData.authToken(), "WHITE", gameID + 42));

        service.joinGame(authData.authToken(), "WHITE", gameID);
        assertThrows(AlreadyTakenException.class, () -> service.joinGame(authData3.authToken(), "WHITE", gameID));

        service.joinGame(authData2.authToken(), "BLACK", gameID);
        assertThrows(AlreadyTakenException.class, () -> service.joinGame(authData3.authToken(), "BLACK", gameID));
    }
}