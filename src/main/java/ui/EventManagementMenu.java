package ui;

import dao.EventDao;
import dao.ActivityLogDao;
import model.User;
import dao.SportDao;
import model.Event;
import model.Sport;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

// Minimal administrator event controls required to publish open events for Participants.
public class EventManagementMenu {
    private final EventDao eventDao;
    private final SportDao sportDao;
    private final Scanner scanner;
    private final User admin;

    public EventManagementMenu(EventDao eventDao, SportDao sportDao, Scanner scanner) {
        this(eventDao, sportDao, scanner, null);
    }

    public EventManagementMenu(EventDao eventDao, SportDao sportDao, Scanner scanner, User admin) {
        this.eventDao = eventDao; this.sportDao = sportDao; this.scanner = scanner; this.admin = admin;
    }

    public void show() {
        int choice;
        do {
            System.out.println("\n--- EVENT MANAGEMENT ---");
            System.out.println("1. View All Events");
            System.out.println("2. Create Event for Qualified Sport");
            System.out.println("3. Open / Close Event");
            System.out.println("0. Back");
            choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> viewEvents();
                case 2 -> createEvent();
                case 3 -> changeStatus();
                case 0 -> { }
                default -> System.out.println("Invalid input, try again.");
            }
        } while (choice != 0);
    }

    private void viewEvents() {
        try {
            List<Event> events = eventDao.getAllEvents();
            if (events.isEmpty()) { System.out.println("No events have been created."); return; }
            for (Event e : events) System.out.printf("%d | %s | %s | %s | %s | %s%n", e.id(), e.title(), e.sportName(), e.eventDate(), e.venue(), e.status());
        } catch (SQLException e) { System.out.println("Unable to load events: " + e.getMessage()); }
    }

    private void createEvent() {
        try {
            List<Sport> sports = sportDao.getAllSports();
            if (sports.isEmpty()) { System.out.println("There are no qualified sports. Evaluate and add a sport first."); return; }
            System.out.println("Qualified sports:");
            for (Sport s : sports) System.out.println(s.id() + ". " + s.name());
            int sportId = readInt("Choose Sport ID (0 to cancel): ");
            if (sportId == 0) return;
            if (sports.stream().noneMatch(s -> s.id() == sportId)) { System.out.println("Invalid sport ID."); return; }
            System.out.print("Event title: "); String title = scanner.nextLine().trim();
            System.out.print("Description: "); String description = scanner.nextLine().trim();
            System.out.print("Event date (YYYY-MM-DD): "); String date = scanner.nextLine().trim();
            try { LocalDate.parse(date); } catch (DateTimeParseException ex) { System.out.println("Invalid date. Use YYYY-MM-DD."); return; }
            System.out.print("Venue: "); String venue = scanner.nextLine().trim();
            if (title.isBlank() || venue.isBlank()) { System.out.println("Title and venue are required."); return; }
            if (eventDao.createEvent(sportId, title, description, date, venue)) {
                if (admin != null) new ActivityLogDao().logAction(admin.id(), "EVENT_CREATED",
                        "Admin " + admin.id() + " created event " + title + " for Sport " + sportId + ".");
                System.out.println("Event created and published as OPEN.");
            } else System.out.println("Event could not be created.");
        } catch (SQLException e) { System.out.println("Unable to create event: " + e.getMessage()); }
    }

    private void changeStatus() {
        viewEvents();
        int id = readInt("Event ID to update (0 to cancel): ");
        if (id == 0) return;
        System.out.print("New status (OPEN/CLOSED): ");
        String status = scanner.nextLine().trim().toUpperCase();
        try {
            if (eventDao.updateEventStatus(id, status, admin == null ? 0 : admin.id())) System.out.println("Event status updated.");
            else System.out.println("Invalid status or event ID.");
        } catch (SQLException e) { System.out.println("Unable to update event: " + e.getMessage()); }
    }

    private int readInt(String prompt) {
        while (true) { System.out.print(prompt); try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Please enter a number."); } }
    }
}
