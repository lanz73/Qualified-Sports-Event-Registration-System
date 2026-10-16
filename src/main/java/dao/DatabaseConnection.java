package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:sports.db";

    private static final String CREATE_USERS_TABLE = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                roles TEXT NOT NULL
                    CHECK (roles IN ('ADMIN', 'PARTICIPANT', 'GUEST')),
                status TEXT NOT NULL DEFAULT 'PENDING'
            )
            """;

    private static final String CREATE_SPORTS_TABLE = """
            CREATE TABLE IF NOT EXISTS sports (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            )
            """;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(CREATE_SPORTS_TABLE);

            String tableSQL = null;

            try (ResultSet result = statement.executeQuery(
                    "SELECT sql FROM sqlite_master " +
                            "WHERE type = 'table' AND name = 'users'")) {

                if (result.next()) {
                    tableSQL = result.getString("sql");
                }
            }

            // Create the table if it does not exist yet.
            if (tableSQL == null) {
                statement.execute(CREATE_USERS_TABLE);
                return;
            }

            // Migrate the old TEACHER/STUDENT role restriction.
            if (tableSQL.contains("TEACHER")
                    || tableSQL.contains("STUDENT")) {

                connection.setAutoCommit(false);

                try {
                    statement.execute(
                            "ALTER TABLE users RENAME TO users_old"
                    );

                    statement.execute(CREATE_USERS_TABLE);

                    statement.execute("""
                            INSERT INTO users
                                (id, username, password, roles, status)
                            SELECT
                                id, username, password,
                                'PARTICIPANT', status
                            FROM users_old
                            """);

                    statement.execute("DROP TABLE users_old");

                    connection.commit();

                    System.out.println(
                            "Database roles updated successfully."
                    );

                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }
        }
    }
}