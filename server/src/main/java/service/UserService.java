package service;

import chess.*;

public class UserService {

    public AuthData register(UserData user) {
        return new AuthData(generateAuthToken(), user.username());
    }

    private String generateAuthToken() {
        return "xyz";
    }
}
