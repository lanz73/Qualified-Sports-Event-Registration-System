package dao;

import model.Event;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDao {
    public List<Event> getOpenEvents() throws SQLException {
        String sql = "SELECT e.id, e.sport_id, s.name AS sport_name, e.title, e.description, e.event_date, e.venue, e.status " +
                "FROM events e JOIN sports s ON s.id=e.sport_id WHERE e.status='OPEN' ORDER BY e.event_date, e.id";
        return queryEvents(sql, null);
    }

    public List<Event> getAllEvents() throws SQLException {
        String sql = "SELECT e.id, e.sport_id, s.name AS sport_name, e.title, e.description, e.event_date, e.venue, e.status " +
                "FROM events e JOIN sports s ON s.id=e.sport_id ORDER BY e.event_date, e.id";
        return queryEvents(sql, null);
    }

    private List<Event> queryEvents(String sql, Integer ignored) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) events.add(new Event(rs.getInt("id"), rs.getInt("sport_id"), rs.getString("sport_name"),
                    rs.getString("title"), rs.getString("description"), rs.getString("event_date"), rs.getString("venue"), rs.getString("status")));
        }
        return events;
    }

    public boolean createEvent(int sportId, String title, String description, String eventDate, String venue) throws SQLException {
        String sql = "INSERT INTO events (sport_id,title,description,event_date,venue,status) VALUES (?,?,?,?,?,'OPEN')";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sportId); ps.setString(2, title); ps.setString(3, description);
            ps.setString(4, eventDate); ps.setString(5, venue);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateEventStatus(int eventId, String status) throws SQLException {
        return updateEventStatus(eventId, status, 0);
    }

    public boolean updateEventStatus(int eventId, String status, int adminId) throws SQLException {
        if (!"OPEN".equals(status) && !"CLOSED".equals(status)) return false;
        String previousStatus = null;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement find = c.prepareStatement("SELECT status FROM events WHERE id=?")) {
            find.setInt(1, eventId);
            try (ResultSet rs = find.executeQuery()) { if (rs.next()) previousStatus = rs.getString("status"); }
        }
        if (previousStatus == null) return false;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE events SET status=? WHERE id=?")) {
            ps.setString(1, status); ps.setInt(2, eventId);
            boolean updated = ps.executeUpdate() == 1;
            if (updated && !previousStatus.equals(status) && adminId > 0) {
                new ActivityLogDao().logAction(adminId, "EVENT_STATUS_UPDATED",
                        "Admin " + adminId + " changed Event " + eventId + " status from "
                                + previousStatus + " to " + status + ".");
            }
            return updated;
        }
    }
}
