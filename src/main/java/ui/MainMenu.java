package ui;

import dao.SportDao;
import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class MainMenu {

    private static final String ADMIN_CODE = "1111";

    private final Scanner scanner;
    private final UserDao userDao;

    public MainMenu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void show() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Qualified Sports Event Registration System ===");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Continue as Guest");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> new GuestMenu(scanner, new SportDao()).show();
                case "4" -> {
                    running = false;
                    System.out.println("Application closed. Goodbye!");
                }
                default -> System.out.println("Invalid input, try again.");
            }
        }
    }

    // Asks for the admin code and returns true only if it matches.
    private boolean askAdminCode() {
        System.out.print("Enter admin code: ");
        String code = scanner.nextLine().trim();

        if (!code.equals(ADMIN_CODE)) {
            System.out.println("Wrong admin code.");
            return false;
        }

        return true;
    }

    private void register() {
        try {
            System.out.println("\n--- Register Account ---");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            if (username.isEmpty()) {
                System.out.println("Username cannot be empty.");
                return;
            }

            if (userDao.usernameExists(username)) {
                System.out.println("Username already exists.");
                return;
            }

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            if (password.isBlank()) {
                System.out.println("Password cannot be empty.");
                return;
            }

            System.out.println("Select role:");
            System.out.println("1. PARTICIPANT");
            System.out.println("2. ADMIN");
            System.out.print("Choose a role: ");

            String roleChoice = scanner.nextLine().trim();

            String role = switch (roleChoice) {
                case "1" -> "PARTICIPANT";
                case "2" -> "ADMIN";
                default -> null;
            };

            if (role == null) {
                System.out.println("Invalid role selection. Registration cancelled.");
                return;
            }

            if (role.equals("ADMIN") && !askAdminCode()) {
                System.out.println("Registration cancelled.");
                return;
            }

            if (userDao.register(username, password, role)) {
                System.out.println("Account registered successfully!");
                System.out.println("Role: " + role);

                if (role.equals("ADMIN")) {
                    System.out.println("Status: APPROVED");
                } else {
                    System.out.println("Status: PENDING");
                    System.out.println("An admin must approve your account before you can log in.");
                }
            } else {
                System.out.println("Registration failed.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void login() {
        try {
            System.out.println("\n--- Login ---");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            User user = userDao.login(username, password);

            if (user == null) {
                System.out.println("Invalid username or password.");
                return;
            }

            if (!user.status().equals("APPROVED")) {
                System.out.println("Your account is still pending approval.");
                System.out.println("Please wait for an admin to approve it.");
                return;
            }

            if (user.roles().equals("ADMIN") && !askAdminCode()) {
                System.out.println("Login cancelled.");
                return;
            }

            System.out.println("Login successful!");

            if (user.roles().equals("ADMIN")) {
                new AdminMenu(scanner, user, userDao).show();
            } else {
                new UserMenu(scanner, user).show();
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}