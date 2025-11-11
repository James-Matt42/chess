package client;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

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

//    @Test
//    public void __() throws Exception {
//
//    }

}
