package dao;

import model.Registration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDao {
    public boolean registerForEvent(int userId, int eventId) throws SQLException {
        String sql = "INSERT INTO registrations (user_id,event_id,status) " +
                "SELECT ?, id, 'REGISTERED' FROM events WHERE id=? AND status='OPEN' " +
                "ON CONFLICT(user_id,event_id) DO UPDATE SET status='REGISTERED', registered_at=CURRENT_TIMESTAMP " +
                "WHERE registrations.status='CANCELLED'";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, eventId);
            boolean saved = ps.executeUpdate() == 1;
            if (saved) new ActivityLogDao().logAction(userId, "EVENT_REGISTERED",
                    "Participant " + userId + " registered for Event " + eventId + ".");
            return saved;
        }
    }

    public List<Registration> getRegistrationsForUser(int userId) throws SQLException {
        String sql = "SELECT r.id,r.event_id,e.title,s.name AS sport_name,e.event_date,e.venue,r.status,r.registered_at " +
                "FROM registrations r JOIN events e ON e.id=r.event_id JOIN sports s ON s.id=e.sport_id " +
                "WHERE r.user_id=? ORDER BY r.registered_at DESC";
        List<Registration> rows = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new Registration(rs.getInt("id"), rs.getInt("event_id"), rs.getString("title"),
                        rs.getString("sport_name"), rs.getString("event_date"), rs.getString("venue"),
                        rs.getString("status"), rs.getString("registered_at")));
            }
        }
        return rows;
    }

    public boolean cancelOwnRegistration(int userId, int registrationId) throws SQLException {
        String sql = "UPDATE registrations SET status='CANCELLED' WHERE id=? AND user_id=? AND status='REGISTERED'";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, registrationId); ps.setInt(2, userId);
            boolean saved = ps.executeUpdate() == 1;
            if (saved) new ActivityLogDao().logAction(userId, "REGISTRATION_CANCELLED",
                    "Participant " + userId + " cancelled Registration " + registrationId + ".");
            return saved;
        }
    }
}
