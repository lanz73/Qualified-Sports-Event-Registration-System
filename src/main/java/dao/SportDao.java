package dao;

import model.Sport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SportDao {

    // Returns every registered sport, ordered by name.
    public List<Sport> findAll() throws SQLException {
        String sql = "SELECT id, name FROM sports ORDER BY name";

        List<Sport> sports = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                sports.add(new Sport(
                        result.getInt("id"),
                        result.getString("name")
                ));
            }
        }

        return sports;
    }
}