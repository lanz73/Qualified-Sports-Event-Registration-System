package ui;

import dao.SportDao;
import dao.UserDao;
import model.Sport;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final SportDao sportDao;
    private final Scanner scanner;
    private final User admin;

    public AdminMenu(
            UserDao userDao,
            SportDao sportDao,
            Scanner scanner,
            User admin
    ) {
        this.userDao = userDao;
        this.sportDao = sportDao;
        this.scanner = scanner;
        this.admin = admin;
    }

    public void show() {
        int choice;

        do {
            System.out.println("\n=== ADMIN DASHBOARD ===");
            System.out.println("Logged in as: " + admin.username());
            System.out.println("1. View All Users");
            System.out.println("2. Approve / Reject Participants");
            System.out.println("3. View Sports");
            System.out.println("4. Evaluate and Add Sport");
            System.out.println("5. Delete Sport");
            System.out.println("6. View Sports Evaluation History");
            System.out.println("0. Logout");

            choice = readInt("Choice: ");

            switch (choice) {
                case 1 -> viewUsers();
                case 2 -> managePendingUsers();
                case 3 -> viewSports();
                case 4 -> evaluateAndAddSport();
                case 5 -> deleteSport();
                case 6 -> viewEvaluationHistory();
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid input, try again.");
            }
        } while (choice != 0);
    }

    private void viewUsers() {
        try {
            List<User> users = userDao.getAllUsers();

            System.out.printf(
                    "%-5s %-20s %-15s %-12s%n",
                    "ID", "Username", "Role", "Status"
            );

            for (User user : users) {
                System.out.printf(
                        "%-5d %-20s %-15s %-12s%n",
                        user.id(), user.username(),
                        user.roles(), user.status()
                );
            }
        } catch (SQLException e) {
            System.out.println("Unable to load users: " + e.getMessage());
        }
    }

    private void managePendingUsers() {
        try {
            List<User> pending = userDao.getPendingUsers();

            if (pending.isEmpty()) {
                System.out.println("There are no pending participant accounts.");
                return;
            }

            System.out.println("\n--- PENDING PARTICIPANTS ---");
            for (User user : pending) {
                System.out.println(
                        "ID: " + user.id()
                                + " | Username: " + user.username()
                                + " | Status: " + user.status()
                );
            }

            int id = readInt("Enter participant ID: ");

            System.out.println("1. Approve");
            System.out.println("2. Reject");
            int action = readInt("Choice: ");

            String status;

            if (action == 1) {
                status = "APPROVED";
            } else if (action == 2) {
                status = "REJECTED";
            } else {
                System.out.println("Invalid input, try again.");
                return;
            }

            if (userDao.updateStatus(id, status)) {
                userDao.logActivity(admin.username(), "PARTICIPANT_STATUS_CHANGED",
                        "Participant ID " + id + " set to " + status);
                System.out.println("Participant status updated to " + status + ".");
            } else {
                System.out.println("No matching participant was updated.");
            }
        } catch (SQLException e) {
            System.out.println("Unable to manage accounts: " + e.getMessage());
        }
    }

    private void viewSports() {
        try {
            List<Sport> sports = sportDao.getAllSports();

            if (sports.isEmpty()) {
                System.out.println("No sports are listed.");
                return;
            }

            for (Sport sport : sports) {
                System.out.println(sport.id() + ". " + sport.name());
            }
        } catch (SQLException e) {
            System.out.println("Unable to load sports: " + e.getMessage());
        }
    }

    private void evaluateAndAddSport() {
        System.out.print("Enter proposed sport name: ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) {
            System.out.println("Sport name cannot be empty.");
            return;
        }

        try {
            for (Sport sport : sportDao.getAllSports()) {
                if (sport.name().equalsIgnoreCase(name)) {
                    System.out.println("That sport is already in the approved sports list.");
                    return;
                }
            }

            System.out.println("Answer each requirement with y or n.");
            boolean venue = askYesNo("Is a suitable venue available? ");
            boolean equipment = askYesNo("Is the required equipment available? ");
            boolean safety = askYesNo("Can the safety requirements be met? ");
            boolean budget = askYesNo("Is the sport within the available budget? ");
            boolean participants = askYesNo("Are enough participants expected? ");
            boolean officials = askYesNo("Are rules and officials/referees available? ");

            boolean applicable = venue && equipment && safety && budget && participants && officials;
            String result = applicable ? "APPLICABLE" : "NOT_APPLICABLE";
            sportDao.saveEvaluation(name, venue, equipment, safety, budget,
                    participants, officials, result, admin.username());
            userDao.logActivity(admin.username(), "SPORT_EVALUATION",
                    name + " evaluated as " + result);

            if (!applicable) {
                System.out.println("Evaluation result: NOT APPLICABLE at this time.");
                System.out.println("The evaluation was saved. The sport was NOT added.");
                System.out.println("Review the failed requirements before evaluating it again.");
                return;
            }

            if (sportDao.addSport(name)) {
                userDao.logActivity(admin.username(), "SPORT_ADDED", name);
                System.out.println("Evaluation result: APPLICABLE.");
                System.out.println("Sport approved and added successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Could not evaluate/add sport: " + e.getMessage());
        }
    }

    private boolean askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String answer = scanner.nextLine().trim().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) return true;
            if (answer.equals("n") || answer.equals("no")) return false;
            System.out.println("Invalid input, try again. Enter y or n.");
        }
    }

    private void viewEvaluationHistory() {
        String sql = "SELECT id, sport_name, result, evaluated_by, evaluated_at FROM sport_evaluations ORDER BY id DESC";
        try (var connection = dao.DatabaseConnection.getConnection();
             var statement = connection.prepareStatement(sql);
             var result = statement.executeQuery()) {
            System.out.println("\n--- SPORTS EVALUATION HISTORY ---");
            boolean any = false;
            while (result.next()) {
                any = true;
                System.out.printf("#%d | %s | %s | By: %s | %s%n",
                        result.getInt("id"), result.getString("sport_name"),
                        result.getString("result"), result.getString("evaluated_by"),
                        result.getString("evaluated_at"));
            }
            if (!any) System.out.println("No evaluations recorded yet.");
        } catch (SQLException e) {
            System.out.println("Unable to load evaluation history: " + e.getMessage());
        }
    }

    private void deleteSport() {
        viewSports();
        int id = readInt("Enter sport ID to delete: ");

        try {
            if (sportDao.deleteSport(id)) {
                userDao.logActivity(admin.username(), "SPORT_DELETED", "Sport ID " + id);
                System.out.println("Sport deleted successfully.");
            } else {
                System.out.println("Sport ID not found.");
            }
        } catch (SQLException e) {
            System.out.println("Unable to delete sport: " + e.getMessage());
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