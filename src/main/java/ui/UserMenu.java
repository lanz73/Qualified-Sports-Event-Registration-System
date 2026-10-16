
package ui;

import model.User;
import java.util.Scanner;

public class UserMenu {

    private final Scanner scanner;
    private final User user;

    public UserMenu(Scanner scanner, User user) {
        this.scanner = scanner;
        this.user = user;
    }

    public void show() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n=== User Menu ===");
            System.out.println("Welcome, " + user.username());
            System.out.println("Role: " + user.roles());
            System.out.println("Status: " + user.status());
            System.out.println("1. View Account");
            System.out.println("2. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.println("\n--- Account Details ---");
                    System.out.println("ID: " + user.id());
                    System.out.println("Username: " + user.username());
                    System.out.println("Role: " + user.roles());
                    System.out.println("Status: " + user.status());
                }
                case "2" -> {
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                }
                default -> System.out.println("Invalid input, try again.");
            }
        }
    }
}
