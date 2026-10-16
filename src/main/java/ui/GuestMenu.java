package ui;

import dao.SportDao;
import model.Sport;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class GuestMenu {

    private final Scanner scanner;
    private final SportDao sportDao;

    public GuestMenu(Scanner scanner, SportDao sportDao) {
        this.scanner = scanner;
        this.sportDao = sportDao;
    }

    public void show() {
        boolean browsing = true;

        while (browsing) {
            System.out.println("\n=== Guest Menu ===");
            System.out.println("You are browsing as a guest (no account).");
            System.out.println("1. View Sports");
            System.out.println("2. Back to Main Menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewSports();
                case "2" -> browsing = false;
                default -> System.out.println("Invalid input, try again.");
            }
        }
    }

    private void viewSports() {
        try {
            List<Sport> sports = sportDao.findAll();

            System.out.println("\n--- Sports ---");

            if (sports.isEmpty()) {
                System.out.println("No sports registered yet.");
                return;
            }

            for (Sport sport : sports) {
                System.out.println("- " + sport.name());
            }

            System.out.println("\nIf you want to join, please register.");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}