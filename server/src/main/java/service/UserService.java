package service;

import chess.*;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws Exception {
        if (dataAccess.getUser(user.username()) != null) {
//            Check pet shop for better implementation of errors
            throw new Exception("Already exists");
        }
        dataAccess.createUser(user);
        var authData = new AuthData(generateAuthToken(), user.username());

        return authData;
    }

    private String generateAuthToken() {
        return "xyz";
    }
}
