package server;

import chess.LoginRequest;
import chess.UserData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import dataaccess.SQLDataAccess;
import io.javalin.*;
import io.javalin.http.Context;
import service.AlreadyTakenException;
import service.BadRequestException;
import service.InvalidAuthException;
import service.UserService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService;

    public Server() {
        try {
            this.userService = new UserService(new SQLDataAccess());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        server = Javalin.create(config -> config.staticFiles.add("web"));

        var wsHandler = new WsRequestHandler(userService);

//        Clear database
        server.delete("db", this::clear);
//        Register a user
        server.post("user", this::register);
//        Login a user
        server.post("session", this::login);
//        Logout a user
        server.delete("session", this::logout);
//        List all games
        server.get("game", this::listGames);
//        Create a game
        server.post("game", this::createGame);
//        Join a game
        server.put("game", this::joinGame);
//        Websocket request
        server.ws("/ws", ws -> {
            ws.onConnect(wsHandler);
            ws.onMessage(wsHandler);
            ws.onClose(wsHandler);
        });
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

    /*
    Handler functions for processing the server requests
    */

    private void clear(Context ctx) {
        try {
            userService.clear();
            ctx.status(200).result("{}");
        } catch (Exception ex) {
            var msg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500).result(msg);
        }
    }

    private void register(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, UserData.class);

            var authData = userService.register(user);
            ctx.status(200).result(serializer.toJson(authData));

        } catch (BadRequestException e) {
            ctx.status(400).result(getMessage(e));
        } catch (AlreadyTakenException e) {
            ctx.status(403).result(getMessage(e));
        } catch (Exception e) {
            ctx.status(500).result(getMessage(e));
        }
    }

    private void login(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var loginRequest = serializer.fromJson(requestJson, LoginRequest.class);

            var authData = userService.login(loginRequest);
            ctx.status(200).result(serializer.toJson(authData));

        } catch (BadRequestException e) {
            ctx.status(400).result(getMessage(e));
        } catch (InvalidAuthException e) {
            ctx.status(401).result(getMessage(e));
        } catch (Exception e) {
            ctx.status(500).result(getMessage(e));
        }
    }

    private void logout(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.header("authorization");
            var authToken = serializer.fromJson(requestJson, String.class);

            userService.logout(authToken);
            ctx.status(200).result("{}");

        } catch (InvalidAuthException e) {
            ctx.status(401).result(getMessage(e));
        } catch (Exception e) {
            ctx.status(500).result(getMessage(e));
        }
    }

    private void listGames(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.header("authorization");
            var authToken = serializer.fromJson(requestJson, String.class);

            var games = userService.listGames(authToken);

            var response = new HashMap<String, HashSet<chess.ReturnGameData>>();
            response.put("games", games);

            var result = serializer.toJson(response);
            ctx.status(200).result(result);

        } catch (InvalidAuthException e) {
            ctx.status(401).result(getMessage(e));
        } catch (Exception e) {
            ctx.status(500).result(getMessage(e));
        }
    }

    private void createGame(Context ctx) {
        try {
            var serializer = new Gson();

            String authHeader = ctx.header("authorization");
            String requestJson = ctx.body();

            var authToken = serializer.fromJson(authHeader, String.class);
            String gameName = (String) serializer.fromJson(requestJson, Map.class).get("gameName");

            var gameID = userService.createGame(authToken, gameName);
            var response = new HashMap<String, Integer>();
            response.put("gameID", gameID);
            ctx.status(200).result(serializer.toJson(response));

        } catch (BadRequestException e) {
            ctx.status(400).result(getMessage(e));
        } catch (InvalidAuthException e) {
            ctx.status(401).result(getMessage(e));
        } catch (Exception e) {
            ctx.status(500).result(getMessage(e));
        }
    }

    private void joinGame(Context ctx) {
        try {
            var serializer = new Gson();

            String authHeader = ctx.header("authorization");
            String requestJson = ctx.body();

            String authToken;
            try {
                authToken = serializer.fromJson(authHeader, String.class);
            } catch (JsonSyntaxException e) {
                throw new InvalidAuthException("unauthorized");
            }
            String playerColor;
            try {
                playerColor = (String) serializer.fromJson(requestJson, Map.class).get("playerColor");
            } catch (Exception e) {
                throw new RuntimeException("bad request");
            }
            int gameID;
            try {
                gameID = (int) (double) serializer.fromJson(requestJson, Map.class).get("gameID");
            } catch (Exception e) {
                throw new BadRequestException("bad request");
            }

            userService.joinGame(authToken, playerColor, gameID);
            ctx.status(200).result("{}");

        } catch (BadRequestException e) {
            ctx.status(400).result(getMessage(e));
        } catch (AlreadyTakenException e) {
            ctx.status(403).result(getMessage(e));
        } catch (InvalidAuthException e) {
            ctx.status(401).result(getMessage(e));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            ctx.status(500).result(getMessage(e));
        }
    }

    private String getMessage(Exception ex) {
        return String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
    }
}
