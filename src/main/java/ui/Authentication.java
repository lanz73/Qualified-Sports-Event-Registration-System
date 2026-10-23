package ui;

import dao.SportDao;
import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Authentication {

    private static final String ADMIN_CODE = "1234";

    private final UserDao userDao;
    private final SportDao sportDao;
    private final Scanner scanner;

    public Authentication(
            UserDao userDao,
            SportDao sportDao,
            Scanner scanner
    ) {
        this.userDao = userDao;
        this.sportDao = sportDao;
        this.scanner = scanner;
    }

    public void start() {
        int choice;

        do {
            System.out.println("\n=== QUALIFIED SPORTS EVENT REGISTRATION SYSTEM ===");
            System.out.println("1. Admin Login");
            System.out.println("2. Participant Login");
            System.out.println("3. Create Participant Account");
            System.out.println("4. Browse Sports as Guest");
            System.out.println("0. Exit");

            choice = readInt("Choice: ");

            switch (choice) {
                case 1 -> login("ADMIN");
                case 2 -> login("PARTICIPANT");
                case 3 -> registerParticipant();
                case 4 -> new GuestMenu(sportDao, scanner).show();
                case 0 -> System.out.println("Goodbye!");
                default -> System.out.println("Invalid input, try again.");
            }
        } while (choice != 0);
    }

    private void login(String role) {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            User user = userDao.login(username, password, role);

            if (user == null) {
                System.out.println(
                        "Login failed. Check your credentials, role, and account approval."
                );
                return;
            }

            System.out.println("Welcome, " + user.username() + "!");

            if (role.equals("ADMIN")) {
                new AdminMenu(userDao, sportDao, scanner, user).show();
            } else {
                new UserMenu(user, sportDao, scanner).show();
            }

            System.out.println("Logged out.");
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }

    private void registerParticipant() {
        try {
            System.out.println("\n--- CREATE PARTICIPANT ACCOUNT ---");

            System.out.print("Choose username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Choose password: ");
            String password = scanner.nextLine();

            if (username.isBlank() || password.isBlank()) {
                System.out.println("All fields are required.");
                return;
            }

            if (userDao.usernameExists(username)) {
                System.out.println("Username already exists.");
                return;
            }

            if (userDao.register(username, password)) {
                System.out.println(
                        "Account created. Status: PENDING. Wait for admin approval."
                );
            } else {
                System.out.println("Registration failed.");
            }
        } catch (SQLException e) {
            System.out.println("Registration error: " + e.getMessage());
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