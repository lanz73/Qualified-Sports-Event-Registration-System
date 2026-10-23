package ui;

import dao.SportDao;
import model.Sport;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class GuestMenu {

    private final SportDao sportDao;
    private final Scanner scanner;

    public GuestMenu(SportDao sportDao, Scanner scanner) {
        this.sportDao = sportDao;
        this.scanner = scanner;
    }

    public void show() {
        System.out.println("\n=== GUEST: BROWSE SPORTS ===");

        try {
            List<Sport> sports = sportDao.getAllSports();

            if (sports.isEmpty()) {
                System.out.println("No sports are currently listed.");
            } else {
                for (Sport sport : sports) {
                    System.out.println(sport.id() + ". " + sport.name());
                }
            }
        } catch (SQLException e) {
            System.out.println("Unable to load sports: " + e.getMessage());
        }

        System.out.println("\nPress Enter to return to the main menu.");
        scanner.nextLine();
    }
}