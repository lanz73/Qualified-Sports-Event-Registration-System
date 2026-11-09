package ui;

import dao.EventDao;
import dao.RegistrationDao;
import dao.SportDao;
import dao.UserDao;
import model.Event;
import model.Registration;
import model.Sport;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class UserMenu {
    private User user;
    private final UserDao userDao;
    private final SportDao sportDao;
    private final EventDao eventDao;
    private final RegistrationDao registrationDao;
    private final Scanner scanner;

    public UserMenu(User user, UserDao userDao, SportDao sportDao, EventDao eventDao,
                    RegistrationDao registrationDao, Scanner scanner) {
        this.user = user;
        this.userDao = userDao;
        this.sportDao = sportDao;
        this.eventDao = eventDao;
        this.registrationDao = registrationDao;
        this.scanner = scanner;
    }

    public void show() {
        int choice;
        do {
            System.out.println("\n================================================");
            System.out.println("              PARTICIPANT DASHBOARD");
            System.out.println("================================================");
            System.out.println("Welcome, " + user.username());
            System.out.println("1. View My Profile");
            System.out.println("2. Edit My Profile");
            System.out.println("3. View Qualified Sports");
            System.out.println("4. View Open Events");
            System.out.println("5. Register for an Event");
            System.out.println("6. View My Registration Records");
            System.out.println("7. Cancel My Registration");
            System.out.println("0. Logout");
            choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> viewProfile();
                case 2 -> editProfile();
                case 3 -> viewSports();
                case 4 -> viewOpenEvents();
                case 5 -> registerForEvent();
                case 6 -> viewMyRegistrations();
                case 7 -> cancelRegistration();
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid input, try again.");
            }
        } while (choice != 0);
    }

    private void viewProfile() {
        System.out.println("\n--- MY PROFILE ---");
        System.out.println("User ID: " + user.id());
        System.out.println("Username: " + user.username());
        System.out.println("Role: " + user.roles());
        System.out.println("Status: " + user.status());
    }

    private void editProfile() {
        System.out.println("\n--- EDIT MY PROFILE ---");
        System.out.print("New username: ");
        String username = scanner.nextLine().trim();
        System.out.print("New password: ");
        String password = scanner.nextLine();
        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }
        try {
            if (userDao.updateOwnProfile(user.id(), username, password)) {
                user = new User(user.id(), username, password, user.roles(), user.status());
                System.out.println("Your profile was updated successfully.");
            } else {
                System.out.println("Profile update failed. The username may already be taken.");
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique"))
                System.out.println("That username is already in use.");
            else System.out.println("Unable to update profile: " + e.getMessage());
        }
    }

    private void viewSports() {
        try {
            List<Sport> sports = sportDao.getAllSports();
            System.out.println("\n--- QUALIFIED SPORTS ---");
            if (sports.isEmpty()) { System.out.println("No qualified sports are currently available."); return; }
            System.out.printf("%-6s %-25s%n", "ID", "SPORT");
            for (Sport sport : sports) System.out.printf("%-6d %-25s%n", sport.id(), sport.name());
        } catch (SQLException e) { System.out.println("Unable to load qualified sports: " + e.getMessage()); }
    }

    private void viewOpenEvents() {
        try {
            List<Event> events = eventDao.getOpenEvents();
            System.out.println("\n--- OPEN EVENTS ---");
            if (events.isEmpty()) { System.out.println("There are currently no open events."); return; }
            displayEvents(events);
        } catch (SQLException e) { System.out.println("Unable to load events: " + e.getMessage()); }
    }

    private void registerForEvent() {
        try {
            List<Event> events = eventDao.getOpenEvents();
            if (events.isEmpty()) { System.out.println("There are no open events to register for."); return; }
            displayEvents(events);
            int eventId = readInt("Enter Event ID to register (0 to cancel): ");
            if (eventId == 0) { System.out.println("Registration cancelled."); return; }
            boolean exists = events.stream().anyMatch(event -> event.id() == eventId);
            if (!exists) { System.out.println("That event is not available for registration."); return; }
            if (registrationDao.registerForEvent(user.id(), eventId))
                System.out.println("You have successfully registered for the event.");
            else System.out.println("Registration failed. You may already be registered, or the event is no longer open.");
        } catch (SQLException e) { System.out.println("Unable to register: " + e.getMessage()); }
    }

    private void viewMyRegistrations() {
        try {
            List<Registration> rows = registrationDao.getRegistrationsForUser(user.id());
            System.out.println("\n--- MY REGISTRATION RECORDS ---");
            if (rows.isEmpty()) { System.out.println("You have no registration records."); return; }
            System.out.printf("%-5s %-22s %-18s %-14s %-12s %-12s%n", "REG#", "EVENT", "SPORT", "DATE", "VENUE", "STATUS");
            for (Registration r : rows)
                System.out.printf("%-5d %-22s %-18s %-14s %-12s %-12s%n", r.id(), cut(r.eventTitle(),22), cut(r.sportName(),18), cut(r.eventDate(),14), cut(r.venue(),12), r.status());
        } catch (SQLException e) { System.out.println("Unable to load your registrations: " + e.getMessage()); }
    }

    private void cancelRegistration() {
        try {
            List<Registration> rows = registrationDao.getRegistrationsForUser(user.id());
            if (rows.isEmpty()) { System.out.println("You have no registrations to cancel."); return; }
            for (Registration r : rows)
                System.out.printf("Registration ID: %d | %s | %s | %s%n", r.id(), r.eventTitle(), r.eventDate(), r.status());
            int id = readInt("Enter your Registration ID to cancel (0 to go back): ");
            if (id == 0) { System.out.println("Cancellation stopped."); return; }
            if (registrationDao.cancelOwnRegistration(user.id(), id))
                System.out.println("Your event registration has been cancelled.");
            else System.out.println("Registration not found in your account or already cancelled.");
        } catch (SQLException e) { System.out.println("Unable to cancel registration: " + e.getMessage()); }
    }

    private void displayEvents(List<Event> events) {
        System.out.printf("%-5s %-22s %-18s %-14s %-18s %-8s%n", "ID", "EVENT", "SPORT", "DATE", "VENUE", "STATUS");
        for (Event e : events) {
            System.out.printf("%-5d %-22s %-18s %-14s %-18s %-8s%n", e.id(), cut(e.title(),22), cut(e.sportName(),18), cut(e.eventDate(),14), cut(e.venue(),18), e.status());
            System.out.println("      Details: " + (e.description() == null || e.description().isBlank() ? "No additional details." : e.description()));
        }
    }

    private String cut(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Invalid input, try again. Enter a number."); }
        }
    }
}
