package client;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import server.Server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static client.ServerFacade.*;
import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static int port;
    private final String username = "Joe";
    private final String password = "mypassword";
    private final String email = "j@j.com";

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(String.format("http://localhost:%d", port));

        HttpRequest request = buildRequest("DELETE", "/db", null, null);
        sendRequest(request);
    }

    @AfterEach
    public void setup() {
        HttpRequest request = buildRequest("DELETE", "/db", null, null);
        sendRequest(request);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void register() {
        assertDoesNotThrow(() -> facade.register(username, password, email));
    }

    @Test
    public void registerFails() {
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

        assertDoesNotThrow(() -> facade.logout());
    }

    @Test
    public void logoutFails() throws Exception {
        assertThrows(Exception.class, () -> facade.logout());

        facade.register(username, password, email);
        facade.login(username, password);
        facade.logout();

        assertThrows(Exception.class, () -> facade.logout());
    }

    @Test
    public void createGame() throws Exception {
        facade.register(username, password, email);
        facade.login(username, password);

        facade.createGame("myGame");
        assertDoesNotThrow(() -> facade.createGame("myGame"));
    }

    @Test
    public void createGameFails() throws Exception {
        assertThrows(Exception.class, () -> facade.createGame("myGame"));

        facade.register(username, password, email);
        facade.login(username, password);
        facade.logout();

        assertThrows(Exception.class, () -> facade.createGame(""));
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
    public void listGamesFails() {
        assertThrows(Exception.class, () -> facade.listGames());
    }

    @Test
    public void joinGame() throws Exception {
        facade.register(username, password, email);
        facade.login(username, password);

        var gameID = facade.createGame("myGame");

        assertDoesNotThrow(() -> facade.joinGame("WHITE", gameID));
    }

    @Test
    public void joinGameFails() throws Exception {
        assertThrows(Exception.class, () -> facade.joinGame("WHITE", 1));

        facade.register(username, password, email);
        facade.login(username, password);

        var gameID = facade.createGame("myGame");

        assertThrows(Exception.class, () -> facade.joinGame("BEIGE", gameID));
    }

    private static HttpRequest buildRequest(String method, String path, Object body, String[] header) {
        String serverUrl = String.format("http://localhost:%d", port);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (header != null) {
            request.setHeader(header[0], header[1]);
        } else if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private static HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return HttpRequest.BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private static HttpResponse<String> sendRequest(HttpRequest request) {
        try {
            return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
