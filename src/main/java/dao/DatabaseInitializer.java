package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public void initialize() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        roles TEXT NOT NULL CHECK (roles IN ('ADMIN', 'PARTICIPANT')),
                        status TEXT NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS sports (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL UNIQUE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS sport_evaluations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        sport_name TEXT NOT NULL,
                        venue_available INTEGER NOT NULL,
                        equipment_available INTEGER NOT NULL,
                        safety_ready INTEGER NOT NULL,
                        within_budget INTEGER NOT NULL,
                        enough_participants INTEGER NOT NULL,
                        officials_available INTEGER NOT NULL,
                        result TEXT NOT NULL CHECK (result IN ('APPLICABLE', 'NOT_APPLICABLE')),
                        evaluated_by TEXT NOT NULL,
                        evaluated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        sport_id INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        event_date TEXT NOT NULL,
                        venue TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED')),
                        FOREIGN KEY (sport_id) REFERENCES sports(id) ON DELETE RESTRICT
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS registrations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        event_id INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'REGISTERED' CHECK (status IN ('REGISTERED', 'CANCELLED')),
                        registered_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE (user_id, event_id),
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
                    )
                    """);

            // Upgrade the earlier username/action/details logger, if this database has it.
            if (tableExists(connection, "activity_logs")
                    && !columnExists(connection, "activity_logs", "user_id")) {
                if (!tableExists(connection, "activity_logs_legacy")) {
                    statement.execute("ALTER TABLE activity_logs RENAME TO activity_logs_legacy");
                } else {
                    statement.execute("DROP TABLE activity_logs");
                }
            }

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS activity_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        action_type TEXT NOT NULL,
                        description TEXT NOT NULL,
                        timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                    """);

            // Preserve legacy history where the old username can be matched to a user.
            if (tableExists(connection, "activity_logs_legacy")) {
                statement.execute("""
                        INSERT INTO activity_logs (user_id, action_type, description, timestamp)
                        SELECT u.id, l.action, l.details, COALESCE(l.created_at, CURRENT_TIMESTAMP)
                        FROM activity_logs_legacy l
                        LEFT JOIN users u ON u.username = l.username
                        WHERE u.id IS NOT NULL
                        """);
                statement.execute("DROP TABLE activity_logs_legacy");
            }
            // Deliberately do not insert sample sports or sample participant accounts.
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?")) {
            statement.setString(1, tableName);
            try (var result = statement.executeQuery()) { return result.next(); }
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName)
            throws SQLException {
        try (Statement statement = connection.createStatement();
             var result = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (result.next()) if (columnName.equalsIgnoreCase(result.getString("name"))) return true;
        }
        return false;
    }
}
