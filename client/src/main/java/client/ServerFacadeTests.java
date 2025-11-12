package client;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions.*;
import server.Server;

import static client.ServerFacade.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    private final String username = "Joe";
    private final String password = "mypassword";
    private final String email = "j@j.com";

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(String.format("http://localhost:%d", port));
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
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
        assertEquals(LOGGED_OUT, facade.login(username, password + "2"));
    }
}
