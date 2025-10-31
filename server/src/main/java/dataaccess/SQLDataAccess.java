package dataaccess;

import chess.AuthData;
import chess.ChessGame;
import chess.GameData;
import chess.UserData;
import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.HashSet;

public class SQLDataAccess implements DataAccess {
    public SQLDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        setup();
    }

    private void executeStatement(String statement, String[] injections) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                for (int i = 1; i < (injections.length + 1); i++) {
                    preparedStatement.setString(i, injections[i - 1]);
                }
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
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

    private void setup() throws DataAccessException {
//        Change the current database to the chess database
        DatabaseManager.createDatabase();
//        Create the tables with the necessary components
        var makeUserTable = """
                CREATE TABLE IF NOT EXISTS users (
                    userID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                    username VARCHAR(50) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL
                )""";
        var makeGameTable = """
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT PRIMARY KEY NOT NULL,
                    gameName varchar(255) NOT NULL,
                    whiteUser varchar(50) DEFAULT NULL,
                    blackUser varchar(50) DEFAULT NULL,
                    gameString LONGTEXT NOT NULL
                )""";
        var makeAuthTable = """
                CREATE TABLE IF NOT EXISTS authData (
                	authToken VARCHAR(255) PRIMARY KEY NOT NULL,
                	username VARCHAR(50) NOT NULL
                )""";

        executeStatement(makeUserTable);
        executeStatement(makeGameTable);
        executeStatement(makeAuthTable);
    }

    @Override
    public void clear() throws DataAccessException {
        executeStatement("TRUNCATE games");
        executeStatement("TRUNCATE users");
        executeStatement("TRUNCATE authData");
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        var newUser = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        executeStatement(newUser, new String[]{user.username(), user.password(), user.email()});
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM users WHERE username=?;")) {
                preparedStatement.setString(1, username);
                var rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    String password = rs.getString(3);
                    String email = rs.getString(4);
                    return new UserData(username, password, email);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException {
        var serializer = new Gson();
        var gameString = serializer.toJson(gameData.game());
        var gameID = gameData.gameID();
        var gameName = gameData.gameName();
        var whiteUser = gameData.whiteUsername();
        var blackUser = gameData.blackUsername();

        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("INSERT INTO games (gameID, gameName, gameString) VALUES (?, ?, ?)")) {
                preparedStatement.setInt(1, gameID);
                preparedStatement.setString(2, gameName);
                preparedStatement.setString(3, gameString);
                preparedStatement.execute();
            }
            if (whiteUser != null && !whiteUser.isBlank()) {
                try (var preparedStatement = conn.prepareStatement("UPDATE games SET whiteUser = ? WHERE gameID = ?;")) {
                    preparedStatement.setString(1, whiteUser);
                    preparedStatement.setInt(2, gameID);
                    preparedStatement.execute();
                }
            }
            if (blackUser != null && !blackUser.isBlank()) {
                try (var preparedStatement = conn.prepareStatement("UPDATE games SET blackUser = ? WHERE gameID = ?;")) {
                    preparedStatement.setString(1, blackUser);
                    preparedStatement.setInt(2, gameID);
                    preparedStatement.execute();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM games WHERE gameID=?")) {
                preparedStatement.setInt(1, gameID);
                var rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    String gameName = rs.getString(2);
                    String whiteUser = rs.getString(3);
                    String blackUser = rs.getString(4);
                    String gameString = rs.getString(5);
                    ChessGame game = new Gson().fromJson(gameString, ChessGame.class);

                    return new GameData(gameID, whiteUser, blackUser, gameName, game);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public HashSet<GameData> listGames() throws DataAccessException {
        var games = new HashSet<GameData>();

        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM games")) {
                var rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    int gameID = rs.getInt(1);
                    String gameName = rs.getString(2);
                    String whiteUser = rs.getString(3);
                    String blackUser = rs.getString(4);
                    String gameString = rs.getString(5);
                    ChessGame game = new Gson().fromJson(gameString, ChessGame.class);

                    games.add(new GameData(gameID, whiteUser, blackUser, gameName, game));
                }
                return games;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        String statement = "UPDATE games SET gameID=?, gameName=?, whiteUser=?, blackUser=?, gameString=? WHERE gameID = ?;";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                int newGameID = gameData.gameID();
                String newGameName = gameData.gameName();
                String newWhiteUser = gameData.whiteUsername();
                String newBlackUser = gameData.blackUsername();
                String newGame = new Gson().toJson(gameData.game());

                preparedStatement.setInt(1, newGameID);
                preparedStatement.setString(2, newGameName);
                preparedStatement.setString(3, newWhiteUser);
                preparedStatement.setString(4, newBlackUser);
                preparedStatement.setString(5, newGame);
                preparedStatement.setInt(6, gameID);

                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("INSERT INTO authData (authToken, username) VALUES (?, ?)")) {
                preparedStatement.setString(1, authData.authToken());
                preparedStatement.setString(2, authData.username());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public HashSet<AuthData> getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM authData WHERE authToken=?")) {
                preparedStatement.setString(1, authToken);
                var rs = preparedStatement.executeQuery();
                var authDatas = new HashSet<AuthData>();
                while (rs.next()) {
                    String username = rs.getString(2);
                    authDatas.add(new AuthData(authToken, username));
                }
                return authDatas;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM authData WHERE authToken=?")) {
                preparedStatement.setString(1, authToken);
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
