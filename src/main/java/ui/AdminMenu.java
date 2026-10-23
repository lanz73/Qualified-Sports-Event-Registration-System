
package ui;

import dao.DatabaseConnection;
import dao.SportDao;
import dao.UserDao;
import model.Sport;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Reject Participant Account");
            System.out.println("5. View Sports");
            System.out.println("6. Evaluate and Add Sport");
            System.out.println("7. Delete Sport");
            System.out.println("8. View Sports Evaluation History");
            System.out.println("0. Logout");

            choice = readInt("Choice: ");

            switch (choice) {
                case 1 -> viewUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approvePendingUser();
                case 4 -> rejectParticipant();
                case 5 -> viewSports();
                case 6 -> evaluateAndAddSport();
                case 7 -> deleteSport();
                case 8 -> viewEvaluationHistory();
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid input, try again.");
            }

        } while (choice != 0);
    }

    // Display every account regardless of role or status.
    private void viewUsers() {
        try {
            List<User> users = userDao.getAllUsers();
            displayUsers(users);
        } catch (SQLException e) {
            System.out.println("Unable to load users: " + e.getMessage());
        }
    }

    // Display all accounts with PENDING status.
    private void viewPendingUsers() {
        try {
            List<User> users = userDao.getPendingUsers();

            if (users.isEmpty()) {
                System.out.println("There are no pending registrations.");
                return;
            }

            System.out.println("\n--- PENDING REGISTRATIONS ---");
            displayUsers(users);

        } catch (SQLException e) {
            System.out.println("Unable to load pending users: "
                    + e.getMessage());
        }
    }

    // Shared table helper for all user lists.
    private void displayUsers(List<User> users) {
        System.out.println();

        System.out.printf(
                "%-5s %-15s %-12s %-10s%n",
                "ID", "Username", "Role", "Status"
        );

        System.out.println("--------------------------------------------");

        if (users.isEmpty()) {
            System.out.println("No user accounts found.");
        } else {
            for (User user : users) {
                System.out.printf(
                        "%-5d %-15s %-12s %-10s%n",
                        user.id(),
                        user.username(),
                        user.roles(),
                        user.status()
                );
            }
        }

        System.out.println("--------------------------------------------");
    }

    // Approve an account only if its ID is in the current pending list.
    private void approvePendingUser() {
        try {
            List<User> pendingUsers = userDao.getPendingUsers();

            if (pendingUsers.isEmpty()) {
                System.out.println("There are no pending accounts to approve.");
                return;
            }

            System.out.println("\n--- PENDING REGISTRATIONS ---");
            displayUsers(pendingUsers);

            int id = readInt("Enter target user ID (0 to cancel): ");

            // Input cancellation.
            if (id == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            if (id < 0) {
                System.out.println("Invalid ID. Approval cancelled.");
                return;
            }

            // Verify that the selected ID is in the pending list.
            boolean found = false;

            for (User user : pendingUsers) {
                if (user.id() == id) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println(
                        "That ID is not in the pending registrations list."
                );
                return;
            }

            if (userDao.approveUser(id)) {
                userDao.logActivity(
                        admin.username(),
                        "USER_APPROVED",
                        "Approved user ID " + id
                );

                System.out.println("User account approved successfully.");
            } else {
                System.out.println(
                        "The account was not approved. It may no longer be pending."
                );
            }

        } catch (SQLException e) {
            System.out.println("Unable to approve account: " + e.getMessage());
        }
    }

    // Preserve the existing participant rejection feature.
    private void rejectParticipant() {
        try {
            List<User> pendingUsers = userDao.getPendingUsers();

            if (pendingUsers.isEmpty()) {
                System.out.println("There are no pending accounts to reject.");
                return;
            }

            displayUsers(pendingUsers);

            int id = readInt("Enter participant ID (0 to cancel): ");

            if (id == 0) {
                System.out.println("Rejection cancelled.");
                return;
            }

            if (id < 0) {
                System.out.println("Invalid ID. Rejection cancelled.");
                return;
            }

            boolean foundParticipant = false;

            for (User user : pendingUsers) {
                if (user.id() == id
                        && user.roles().equals("PARTICIPANT")) {
                    foundParticipant = true;
                    break;
                }
            }

            if (!foundParticipant) {
                System.out.println(
                        "No pending participant account matches that ID."
                );
                return;
            }

            if (userDao.updateStatus(id, "REJECTED")) {
                userDao.logActivity(
                        admin.username(),
                        "USER_REJECTED",
                        "Rejected participant ID " + id
                );

                System.out.println("Participant account rejected.");
            } else {
                System.out.println("Unable to reject that account.");
            }

        } catch (SQLException e) {
            System.out.println("Unable to reject account: " + e.getMessage());
        }
    }

    // ---------- SPORTS MANAGEMENT ----------

    private void viewSports() {
        try {
            List<Sport> sports = sportDao.getAllSports();

            if (sports.isEmpty()) {
                System.out.println("No sports are listed.");
                return;
            }

            System.out.println("\n--- SPORTS LIST ---");

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
                    System.out.println(
                            "That sport is already in the approved sports list."
                    );
                    return;
                }
            }

            System.out.println("Answer each requirement with y or n.");

            boolean venue = askYesNo(
                    "Is a suitable venue available? "
            );
            boolean equipment = askYesNo(
                    "Is the required equipment available? "
            );
            boolean safety = askYesNo(
                    "Can the safety requirements be met? "
            );
            boolean budget = askYesNo(
                    "Is the sport within the available budget? "
            );
            boolean participants = askYesNo(
                    "Are enough participants expected? "
            );
            boolean officials = askYesNo(
                    "Are rules and officials/referees available? "
            );

            boolean applicable = venue
                    && equipment
                    && safety
                    && budget
                    && participants
                    && officials;

            String result = applicable
                    ? "APPLICABLE"
                    : "NOT_APPLICABLE";

            sportDao.saveEvaluation(
                    name,
                    venue,
                    equipment,
                    safety,
                    budget,
                    participants,
                    officials,
                    result,
                    admin.username()
            );

            userDao.logActivity(
                    admin.username(),
                    "SPORT_EVALUATION",
                    name + " evaluated as " + result
            );

            if (!applicable) {
                System.out.println(
                        "Evaluation result: NOT APPLICABLE at this time."
                );
                System.out.println(
                        "The evaluation was saved. The sport was NOT added."
                );
                System.out.println(
                        "Review the failed requirements before evaluating again."
                );
                return;
            }

            if (sportDao.addSport(name)) {
                userDao.logActivity(
                        admin.username(),
                        "SPORT_ADDED",
                        name
                );

                System.out.println("Evaluation result: APPLICABLE.");
                System.out.println("Sport approved and added successfully.");
            } else {
                System.out.println(
                        "The sport could not be added. Check whether it already exists."
                );
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not evaluate/add sport: " + e.getMessage()
            );
        }
    }

    private boolean askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String answer = scanner.nextLine().trim().toLowerCase();

            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }

            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }

            System.out.println(
                    "Invalid input, try again. Enter y or n."
            );
        }
    }

    private void viewEvaluationHistory() {
        String sql = """
                SELECT id, sport_name, result, evaluated_by, evaluated_at
                FROM sport_evaluations
                ORDER BY id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            System.out.println("\n--- SPORTS EVALUATION HISTORY ---");
            boolean any = false;

            while (result.next()) {
                any = true;

                System.out.printf(
                        "#%d | %s | %s | By: %s | %s%n",
                        result.getInt("id"),
                        result.getString("sport_name"),
                        result.getString("result"),
                        result.getString("evaluated_by"),
                        result.getString("evaluated_at")
                );
            }

            if (!any) {
                System.out.println("No evaluations recorded yet.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Unable to load evaluation history: " + e.getMessage()
            );
        }
    }

    private void deleteSport() {
        viewSports();

        int id = readInt("Enter sport ID to delete (0 to cancel): ");

        if (id == 0) {
            System.out.println("Deletion cancelled.");
            return;
        }

        if (id < 0) {
            System.out.println("Invalid ID.");
            return;
        }

        try {
            if (sportDao.deleteSport(id)) {
                userDao.logActivity(
                        admin.username(),
                        "SPORT_DELETED",
                        "Sport ID " + id
                );

                System.out.println("Sport deleted successfully.");
            } else {
                System.out.println("Sport ID not found.");
            }

        } catch (SQLException e) {
            System.out.println("Unable to delete sport: " + e.getMessage());
        }
    }

    // ---------- INPUT HELPER ----------

    private int readInt(String prompt) {
        System.out.print(prompt);

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
