package dao;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public boolean register(String username, String password, String role)
            throws SQLException {

        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, ?, ?)
                """;

        // Admins are gated by the admin code, so they start APPROVED.
        String status = role.equals("ADMIN") ? "APPROVED" : "PENDING";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);
            statement.setString(4, status);

            return statement.executeUpdate() == 1;
        }
    }

    public User login(String username, String password)
            throws SQLException {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ? AND password = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new User(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("roles"),
                            result.getString("status")
                    );
                }
            }
        }

        return null;
    }

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

    // Returns every user whose status is PENDING.
    public List<User> findPendingUsers() throws SQLException {
        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE status = 'PENDING'
                ORDER BY id
                """;

        List<User> pendingUsers = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                pendingUsers.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("roles"),
                        result.getString("status")
                ));
            }
        }

        return pendingUsers;
    }

    // Changes one PENDING user to APPROVED. Returns false if no such pending user.
    public boolean approveUser(int id) throws SQLException {
        String sql = """
                UPDATE users
                SET status = 'APPROVED'
                WHERE id = ? AND status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() == 1;
        }
    }
}