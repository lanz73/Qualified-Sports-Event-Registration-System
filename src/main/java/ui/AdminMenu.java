package ui;

import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final User admin;
    private final UserDao userDao;

    public AdminMenu(Scanner scanner, User admin, UserDao userDao) {
        this.scanner = scanner;
        this.admin = admin;
        this.userDao = userDao;
    }

    public void show() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("Welcome, " + admin.username());
            System.out.println("1. View Account");
            System.out.println("2. Approve Registrations");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.println("\n--- Account Details ---");
                    System.out.println("ID: " + admin.id());
                    System.out.println("Username: " + admin.username());
                    System.out.println("Role: " + admin.roles());
                    System.out.println("Status: " + admin.status());
                }
                case "2" -> approveRegistrations();
                case "3" -> {
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                }
                default -> System.out.println("Invalid input, try again.");
            }
        }
    }

    private void approveRegistrations() {
        try {
            List<User> pendingUsers = userDao.findPendingUsers();

            System.out.println("\n--- Pending Registrations ---");

            if (pendingUsers.isEmpty()) {
                System.out.println("No pending registrations.");
                return;
            }

            for (User pending : pendingUsers) {
                System.out.println("ID: " + pending.id()
                        + " | Username: " + pending.username()
                        + " | Role: " + pending.roles());
            }

            System.out.print("Enter the ID to approve (or press Enter to cancel): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Cancelled.");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID. Please enter a number.");
                return;
            }

            if (userDao.approveUser(id)) {
                System.out.println("User " + id + " approved.");
            } else {
                System.out.println("No pending user with that ID.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}