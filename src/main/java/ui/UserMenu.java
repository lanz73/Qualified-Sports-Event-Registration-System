package ui;

import dao.SportDao;
import model.Sport;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class UserMenu {

    private final User user;
    private final SportDao sportDao;
    private final Scanner scanner;

    public UserMenu(User user, SportDao sportDao, Scanner scanner) {
        this.user = user;
        this.sportDao = sportDao;
        this.scanner = scanner;
    }

    public void show() {
        int choice;

        do {
            System.out.println("\n=== PARTICIPANT DASHBOARD ===");
            System.out.println("Welcome, " + user.username());
            System.out.println("1. View Profile");
            System.out.println("2. View Sports");
            System.out.println("0. Logout");

            choice = readInt("Choice: ");

            switch (choice) {
                case 1 -> viewProfile();
                case 2 -> viewSports();
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

    private void viewSports() {
        try {
            List<Sport> sports = sportDao.getAllSports();

            System.out.println("\n--- AVAILABLE SPORTS ---");

            if (sports.isEmpty()) {
                System.out.println("No sports are currently listed.");
                return;
            }

            for (Sport sport : sports) {
                System.out.println(sport.id() + ". " + sport.name());
            }
        } catch (SQLException e) {
            System.out.println("Unable to load sports: " + e.getMessage());
        }
    }

    private int readInt(String prompt) {
        System.out.print(prompt);

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
