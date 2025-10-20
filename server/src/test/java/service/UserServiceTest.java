package service;

import chess.*;
import dataaccess.MemoryDataAccess;
import org.eclipse.jetty.util.log.Log;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private final UserData user = new UserData("Joe", "myPassword", "jj@j.com");

    private UserService newService() {
        MemoryDataAccess db = new MemoryDataAccess();
        return new UserService(db);
    }

    @Test
    void clear() throws Exception {
        var service = newService();
        service.register(user);
        service.clear();
        assertThrows(Exception.class, () -> {
            service.login(new LoginRequest(user.username(), user.password()));
        });
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
        assertThrows(BadRequestException.class, () -> {
            service.register(user);
        });

//        Try a field as empty string
        var user2 = new UserData("Joe", "", "jj@j.com");
        assertThrows(BadRequestException.class, () -> {
            service.register(user2);
        });

//        Try registering the same user twice
        var user3 = new UserData("Joe", "myPassword", "jj@j.com");
        var user4 = new UserData("Joe", "myPassword", "jj@j.com");
        service.register(user3);
        assertThrows(UsernameTakenException.class, () -> {
            service.register(user4);
        });
    }

    @Test
    void login() throws Exception {
        var service = newService();
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
        assertThrows(BadRequestException.class, () -> {
            service.login(new LoginRequest(null, user.password()));
        });
        assertThrows(BadRequestException.class, () -> {
            service.login(new LoginRequest("", user.password()));
        });

//        Incorrect username or password
        assertThrows(InvalidAuthException.class, () -> {
            service.login(new LoginRequest(user.username(), "Hello there"));
        });
        assertThrows(InvalidAuthException.class, () -> {
            service.login(new LoginRequest("Bob", user.password()));
        });
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
        var authData = service.login(new LoginRequest(user.username(), user.password()));

        assertThrows(InvalidAuthException.class, () -> {
            service.logout("");
        });
        assertThrows(InvalidAuthException.class, () -> {
            service.logout(null);
        });
        assertThrows(InvalidAuthException.class, () -> {
            service.logout("xyz");
        });
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
        assertThrows(InvalidAuthException.class, () -> {
            service.listGames("Hello there");
        });
    }

    @Test
    void createGame() throws Exception {
        var service = newService();
        service.register(user);
        var authData = service.login(new LoginRequest(user.username(), user.password()));

        var gameName = "MyNewGame";
        var gameID = service.createGame(authData.authToken(), gameName);
        var sameGame = new GameData(gameID, "", "", gameName, new ChessGame());

        var games = service.listGames(authData.authToken());

        assertTrue(games.contains(sameGame));
    }

    @Test
    void createGameInvalid() throws Exception {
        var service = newService();
        service.register(user);
        var authData = service.login(new LoginRequest(user.username(), user.password()));

        var gameName = "MyNewGame";
        var gameID = service.createGame(authData.authToken(), gameName);
        var sameGame = new GameData(gameID, "", "", gameName, new ChessGame());

        assertThrows(BadRequestException.class, () -> {
            service.createGame(authData.authToken(), null);
        });
        assertThrows(BadRequestException.class, () -> {
            service.createGame(authData.authToken(), "");
        });
        assertThrows(InvalidAuthException.class, () -> {
            service.createGame("xyz", gameName);
        });
    }
}