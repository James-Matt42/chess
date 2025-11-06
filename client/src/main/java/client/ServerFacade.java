package client;

import java.net.http.HttpClient;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public ServerFacade(int port) {
        serverUrl = String.format("http://localhost:%d", port);
    }

    public void clear() {

    }
}
