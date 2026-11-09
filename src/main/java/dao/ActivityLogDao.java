package dao;

import model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** All database operations for the system audit trail. */
public class ActivityLogDao {

    public void logAction(int userId, String actionType, String description)
            throws SQLException {
        String sql = "INSERT INTO activity_logs (user_id, action_type, description) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (userId > 0) statement.setInt(1, userId);
            else statement.setNull(1, Types.INTEGER);
            statement.setString(2, actionType);
            statement.setString(3, description);
            statement.executeUpdate();
        }
    }

    public List<ActivityLog> getAllLogs() throws SQLException {
        String sql = "SELECT id, user_id, action_type, description, timestamp " +
                "FROM activity_logs ORDER BY timestamp DESC, id DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) logs.add(readLog(result));
        }
        return logs;
    }

    public List<ActivityLog> getLogsByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, action_type, description, timestamp " +
                "FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC, id DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) logs.add(readLog(result));
            }
        }
        return logs;
    }

    /** Optional history cleanup; called only after an administrator confirms. */
    public int clearAllLogs() throws SQLException {
        String sql = "DELETE FROM activity_logs";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        }
    }

    private ActivityLog readLog(ResultSet result) throws SQLException {
        int userId = result.getInt("user_id");
        if (result.wasNull()) userId = 0;
        return new ActivityLog(result.getInt("id"), userId,
                result.getString("action_type"), result.getString("description"),
                result.getString("timestamp"));
    }
}
