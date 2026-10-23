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
                    CREATE TABLE IF NOT EXISTS activity_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL,
                        action TEXT NOT NULL,
                        details TEXT NOT NULL,
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            // Deliberately do not insert sample sports or sample participant accounts.
        }
    }
}
