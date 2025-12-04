package client;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private final String serverUrl;
    private String authToken;
    private final int port;
    private static WsClient wsClient = null;
    private static String username;

    public static final int LOGGED_IN = 1;

    public ServerFacade(int port) {
        serverUrl = String.format("http://localhost:%d", port);
        this.port = port;
    }

    public int login(String username, String password) throws Exception {
        HttpRequest request = buildRequest("POST", "/session", Map.of("username", username, "password", password), null);
        var response = sendRequest(request);

        throwIfException(response);

        var authData = new Gson().fromJson(response.body(), AuthData.class);
        authToken = authData.authToken();
        ServerFacade.username = username;
        return LOGGED_IN;
    }

    public void logout() throws Exception {
        HttpRequest request = buildRequest("DELETE", "/session", null, new String[]{"authorization", authToken});
        var response = sendRequest(request);

        throwIfException(response);

        username = null;
        authToken = "";
    }

    public void register(String username, String password, String email) throws Exception {
        HttpRequest request = buildRequest("POST", "/user", Map.of("username", username, "password", password, "email", email), null);
        var response = sendRequest(request);

        throwIfException(response);

        var authData = new Gson().fromJson(response.body(), AuthData.class);
        ServerFacade.username = username;
        authToken = authData.authToken();
    }

    public List<GameData> listGames() throws Exception {
        HttpRequest request = buildRequest("GET", "/game", null, new String[]{"authorization", authToken});
        var response = sendRequest(request);

        throwIfException(response);

        var mapType = new TypeToken<Map<String, ArrayList<GameData>>>() {
        }.getType();

        @SuppressWarnings("unchecked") var gamesString = (Map<String, ArrayList<GameData>>) new Gson().fromJson(response.body(), mapType);
        return gamesString.get("games");
    }

    public int createGame(String gameName) throws Exception {
        HttpRequest request = buildRequest("POST", "/game", Map.of("gameName", gameName), new String[]{"authorization", authToken});
        var response = sendRequest(request);

        throwIfException(response);

        var mapType = new TypeToken<Map<String, Integer>>() {
        }.getType();
        @SuppressWarnings("unchecked") var body = (Map<String, Integer>) new Gson().fromJson(response.body(), mapType);
        return body.get("gameID");
    }

    public void joinGame(String playerColor, int gameID) throws Exception {
        ChessGame.TeamColor color;
        if (playerColor.equals("WHITE")) {
            color = ChessGame.TeamColor.WHITE;
        } else if (playerColor.equals("BLACK")) {
            color = ChessGame.TeamColor.BLACK;
        } else {
            throw new Exception("Color must be 'WHITE' or 'BLACK'");
        }

        HttpRequest request = buildRequest("PUT", "/game",
                Map.of("playerColor", playerColor, "gameID", gameID), new String[]{"authorization", authToken});
        var response = sendRequest(request);

        throwIfException(response);

        startWebsocket(gameID, color);
    }

    public void observeGame(int gameID) throws Exception {
        startWebsocket(gameID, null);
    }

    public void makeMove(ChessMove move, int gameID) throws IOException {
        var command = new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move);
        String message = new Gson().toJson(command);

        wsClient.send(message);
    }

    public void leaveGame(int gameID) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        String message = new Gson().toJson(command);

        wsClient.send(message);
        wsClient.close();
    }

    public void resignGame(int gameID) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        String message = new Gson().toJson(command);

        wsClient.send(message);
    }

    public ChessBoard getBoard() {
        return wsClient.getGame().getBoard();
    }

    public ChessGame getGame() {
        return wsClient.getGame();
    }

    private void startWebsocket(int gameID, ChessGame.TeamColor playerColor) throws Exception {
//        Create websocket connection
        wsClient = new WsClient(port, playerColor);
        var message = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        var command = new Gson().toJson(message);
        wsClient.send(command);
    }

    private void throwIfException(HttpResponse<String> response) throws Exception {
        if (!isSuccessful(response.statusCode())) {
            var body = new Gson().fromJson(response.body(), Map.class);
            throw new Exception((String) body.get("message"));
        }
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

    private HttpResponse<String> sendRequest(HttpRequest request) {
        try {
            return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
