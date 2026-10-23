
package dao;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // Check whether an administrator account already exists.
    public boolean hasAdmin() throws SQLException {
        String sql = "SELECT 1 FROM users WHERE roles = 'ADMIN' LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            return result.next();
        }
    }

    // Create the first administrator account.
    public boolean createAdmin(String username, String password)
            throws SQLException {

        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, 'ADMIN', 'APPROVED')
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            return statement.executeUpdate() == 1;
        }
    }

    // Record system activity.
    public void logActivity(String username, String action, String details)
            throws SQLException {

        String sql = """
                INSERT INTO activity_logs (username, action, details)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, action);
            statement.setString(3, details);

            statement.executeUpdate();
        }
    }

    // Register a participant account.
    // New accounts must be approved by an administrator.
    public boolean register(String username, String password)
            throws SQLException {

        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, 'PARTICIPANT', 'PENDING')
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            return statement.executeUpdate() == 1;
        }
    }

    // Check whether a username is already registered.
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // Login only if username, password, role, and account status match.
    public User login(String username, String password, String requiredRole)
            throws SQLException {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ?
                  AND password = ?
                  AND roles = ?
                  AND status = 'APPROVED'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, requiredRole);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return readUser(result);
                }
            }
        }

        return null;
    }

    // Requirement 1: Return all accounts, regardless of role or status.
    public List<User> getAllUsers() throws SQLException {
        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                ORDER BY id
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(readUser(result));
            }
        }

        return users;
    }

    // Requirement 2: Return pending accounts across ALL roles.
    public List<User> getPendingUsers() throws SQLException {
        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE status = 'PENDING'
                ORDER BY id
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(readUser(result));
            }
        }

        return users;
    }

    // Requirement 3: Approve a pending account by its ID.
    public boolean approveUser(int userId) throws SQLException {
        String sql = """
                UPDATE users
                SET status = 'APPROVED'
                WHERE id = ? AND status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() == 1;
        }
    }

    // Keep the existing status-management feature.
    public boolean updateStatus(int id, String status) throws SQLException {
        if (!status.equals("APPROVED")
                && !status.equals("REJECTED")
                && !status.equals("PENDING")) {
            return false;
        }

        String sql = """
                UPDATE users
                SET status = ?
                WHERE id = ? AND roles = 'PARTICIPANT'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, id);

            return statement.executeUpdate() == 1;
        }
    }

    // Convert a database row into a User record.
    private User readUser(ResultSet result) throws SQLException {
        return new User(
                result.getInt("id"),
                result.getString("username"),
                result.getString("password"),
                result.getString("roles"),
                result.getString("status")
        );
    }
}
