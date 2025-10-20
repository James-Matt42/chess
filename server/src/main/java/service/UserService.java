package service;

import chess.*;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() throws Exception {
        dataAccess.clear();
    }

    public AuthData register(UserData user) throws Exception {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new BadRequestException("bad request");
        }
        if (user.username().isBlank() || user.password().isBlank() || user.email().isBlank()) {
            throw new BadRequestException("bad request");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new UsernameTakenException("already taken");
        }

        dataAccess.createUser(user);
        return new AuthData(generateAuthToken(), user.username());
    }

    public AuthData login(LoginRequest loginRequest) throws Exception {
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw new BadRequestException("bad request");
        }
        if (loginRequest.username().isBlank() || loginRequest.password().isBlank()) {
            throw new BadRequestException("bad request");
        }

        var userData = dataAccess.getUser(loginRequest.username());
        if (userData == null) {
            throw new InvalidAuthException("unauthorized");
        }
//            Check provided password
        if (!userData.password().equals(loginRequest.password())) {
            throw new InvalidAuthException("unauthorized");
        }
        var authData = new AuthData(generateAuthToken(), loginRequest.username());
        dataAccess.createAuth(authData);
        return authData;
    }

    public void logout(String authToken) {

    }

    public static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
