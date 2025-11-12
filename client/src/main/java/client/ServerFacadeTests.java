package client;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions.*;
import server.Server;

import static client.ServerFacade.*;
import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    private static int port;
    private final String username = "Joe";
    private final String password = "mypassword";
    private final String email = "j@j.com";

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    public void setup() throws Exception {
        facade = new ServerFacade(String.format("http://localhost:%d", port));
        clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void clear() throws Exception {
//        Makes sure the server clears without an exception
        facade.clear();
    }

    @Test
    public void register() throws Exception {
        facade.register(username, password, email);
    }

    @Test
    public void registerFails() throws Exception {
        assertThrows(Exception.class, () -> facade.register(username, password, null));
    }

    @Test
    public void login() throws Exception {
        facade.register(username, password, email);
        assertEquals(LOGGED_IN, facade.login(username, password));
    }

    @Test
    public void loginFails() throws Exception {
        facade.register(username, password, email);
        assertThrows(Exception.class, () -> facade.login(username, password + "2"));
    }

    @Test
    public void logout() throws Exception {
        facade.register(username, password, email);
        facade.login(username, password);

        facade.logout();
    }

    @Test
    public void logoutFails() throws Exception {
        assertThrows(Exception.class, () -> facade.logout());
    }

    @Test
    public void createGame() throws Exception {
        facade.register(username, password, email);
        facade.login(username, password);

        var gameID = facade.createGame("myGame");
        var gameID2 = facade.createGame("myGame2");
    }

    @Test
    public void createGameFails() throws Exception {
        assertThrows(Exception.class, () -> facade.createGame("myGame"));
    }

    @Test
    public void listGames() throws Exception {
        facade.register(username, password, email);
        facade.login(username, password);

        var games = facade.listGames();
        assertEquals(0, games.size());

        var gameID = facade.createGame("myGame");
        assertEquals(gameID, facade.listGames().getFirst().gameID());
    }

    @Test
    public void listGamesFails() throws Exception {
        assertThrows(Exception.class, () -> facade.listGames());
    }

    @Test
    public void joinGame() throws Exception {
        facade.register(username, password, email);
        facade.login(username, password);

        var gameID = facade.createGame("myGame");

        facade.joinGame("WHITE", gameID);
    }

    @Test
    public void joinGameFails() throws Exception {
        assertThrows(Exception.class, () -> facade.joinGame("WHITE", 1));

        facade.register(username, password, email);
        facade.login(username, password);

        var gameID = facade.createGame("myGame");

        assertThrows(Exception.class, () -> facade.joinGame("BEIGE", gameID));
    }
}
