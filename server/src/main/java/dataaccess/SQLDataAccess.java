package dataaccess;

import chess.AuthData;
import chess.GameData;
import chess.UserData;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class SQLDataAccess implements DataAccess {
    public SQLDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        setup();
    }

    private void executeStatement(String statement) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private void executeStatement(String[] statements) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : statements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.execute();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private void setup() throws DataAccessException {
//        Change the current database to the chess database
//        Create the tables with the necessary components
        var makeUserTable = """
                CREATE TABLE IF NOT EXISTS users (
                    userID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                    username VARCHAR(50) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    authToken VARCHAR(255) DEFAULT NULL
                );""";
        var makeGameTable = """
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                    gameName varchar(255) NOT NULL,
                    whiteUser varchar(50) DEFAULT NULL,
                    blackUser varchar(50) DEFAULT NULL,
                    gameString LONGTEXT NOT NULL
                );""";
        executeStatement(new String[]{makeUserTable, makeGameTable});
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("drop database chess")) {
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public HashSet<GameData> listGames() throws DataAccessException {
        return null;
    }

    @Override
    public void updateGame(int gameID, GameData gameData) throws DataAccessException {

    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }
}
