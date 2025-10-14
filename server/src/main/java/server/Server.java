package server;

import chess.UserData;
import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;

import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService;

    public Server() {
        this.userService = new UserService();
        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", ctx -> register(ctx));

    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

    private void register(Context ctx) {
        var serializer = new Gson();
        String requestJson = ctx.body();
        var user = serializer.fromJson(requestJson, UserData.class);

//        call to service and register
        var authData = userService.register(user);

        ctx.result(serializer.toJson(authData));
    }
}
