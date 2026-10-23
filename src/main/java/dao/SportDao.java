package dao;

import model.Sport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SportDao {
    public List<Sport> getAllSports() throws SQLException {
        List<Sport> sports = new ArrayList<>();
        String sql = "SELECT id, name FROM sports ORDER BY name";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) sports.add(new Sport(result.getInt("id"), result.getString("name")));
        }
        return sports;
    }

    public boolean addSport(String name) throws SQLException {
        String sql = "INSERT INTO sports (name) VALUES (?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name.trim());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteSport(int id) throws SQLException {
        String sql = "DELETE FROM sports WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    public void saveEvaluation(String sportName, boolean venue, boolean equipment,
                               boolean safety, boolean budget, boolean participants,
                               boolean officials, String result, String adminUsername)
            throws SQLException {
        String sql = """
                INSERT INTO sport_evaluations
                (sport_name, venue_available, equipment_available, safety_ready,
                 within_budget, enough_participants, officials_available, result, evaluated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sportName.trim());
            statement.setInt(2, venue ? 1 : 0);
            statement.setInt(3, equipment ? 1 : 0);
            statement.setInt(4, safety ? 1 : 0);
            statement.setInt(5, budget ? 1 : 0);
            statement.setInt(6, participants ? 1 : 0);
            statement.setInt(7, officials ? 1 : 0);
            statement.setString(8, result);
            statement.setString(9, adminUsername);
            statement.executeUpdate();
        }
    }
}
