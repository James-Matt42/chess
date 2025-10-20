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
        if (dataAccess.getUser(user.username()) != null) {
            throw new UsernameTakenException("already taken");
        }
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new InvalidAuthException("bad request");
        }
        if (user.username().isBlank() || user.password().isBlank() || user.email().isBlank()) {
            throw new InvalidAuthException("bad request");
        }

        dataAccess.createUser(user);
        return new AuthData(generateAuthToken(), user.username());
    }

    public AuthData login(LoginRequest loginRequest) throws Exception {
        var userData = dataAccess.getUser(loginRequest.username());
        if (userData == null) {
//                User not found exception?
            throw new Exception();
        }
//            Check provided password
        if (!userData.password().equals(loginRequest.password())) {
//                Invalid password exception?
            throw new Exception();
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
