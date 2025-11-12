package client;

import chess.AuthData;
import chess.GameData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    private static final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private String authToken;

    static final int LOGGED_OUT = 0;
    static final int LOGGED_IN = 1;
    static final int IN_GAME = 2;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
        authToken = "";
    }

    public ServerFacade(int port) {
        serverUrl = String.format("http://localhost:%d", port);
    }

    public static void main(String[] args) throws Exception {

    }


    public void clear() throws Exception {
        HttpRequest request = buildRequest("DELETE", "/db", null, null);
        sendRequest(request);
    }

    public int login(String username, String password) throws Exception {
        HttpRequest request = buildRequest("POST", "/session", Map.of("username", username, "password", password), null);
        var response = sendRequest(request);
        if (!isSuccessful(response.statusCode())) {
            var body = new Gson().fromJson(response.body(), Map.class);
            throw new Exception(body.get("message").toString());
        }
        var authData = new Gson().fromJson(response.body(), AuthData.class);
        authToken = authData.authToken();
        return LOGGED_IN;
    }

    public void logout() throws Exception {
        HttpRequest request = buildRequest("DELETE", "/session", null, new String[]{"authorization", authToken});
        var response = sendRequest(request);
        if (isSuccessful(response.statusCode())) {
            authToken = "";
        } else {
            throw new Exception(new Gson().fromJson(response.body(), Map.class).get("message").toString());
        }
    }

    public boolean register(String username, String password, String email) throws Exception {
        HttpRequest request = buildRequest("POST", "/user", Map.of("username", username, "password", password, "email", email), null);
        var response = sendRequest(request);
        if (!isSuccessful(response.statusCode())) {
            return false;
        }
        var authData = new Gson().fromJson(response.body(), AuthData.class);
        authToken = authData.authToken();
        return true;
    }

    public List<GameData> listGames() throws Exception {
        HttpRequest request = buildRequest("GET", "/game", null, new String[]{"authorization", authToken});
        var response = sendRequest(request);

        if (!isSuccessful(response.statusCode())) {
            var body = new Gson().fromJson(response.body(), Map.class);
            throw new Exception((String) body.get("message"));
        }

        var mapType = new TypeToken<Map<String, ArrayList<GameData>>>() {
        }.getType();

        var games_string = (Map<String, ArrayList<GameData>>) new Gson().fromJson(response.body(), mapType);
        return games_string.get("games");
    }

    public int createGame(String gameName) throws Exception {
        HttpRequest request = buildRequest("POST", "/game", Map.of("gameName", gameName), new String[]{"authorization", authToken});
        var response = sendRequest(request);

        if (!isSuccessful(response.statusCode())) {
            var body = new Gson().fromJson(response.body(), Map.class);
            throw new Exception((String) body.get("message"));
        }

        var mapType = new TypeToken<Map<String, Integer>>() {
        }.getType();
        var body = (Map<String, Integer>) new Gson().fromJson(response.body(), mapType);
        return body.get("gameID");
    }

    private HttpRequest buildRequest(String method, String path, Object body, String[] header) {
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

    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return HttpRequest.BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws Exception {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            return null;
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }
        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
