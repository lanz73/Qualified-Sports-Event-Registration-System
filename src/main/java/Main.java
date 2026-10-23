import dao.DatabaseInitializer;
import dao.SportDao;
import dao.UserDao;
import ui.Authentication;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserDao userDao = new UserDao();
        Scanner scanner = new Scanner(System.in);
        try {
            new DatabaseInitializer().initialize();
            System.out.println("Database ready. No sample sports or participants were added.");
            if (!userDao.hasAdmin()) {
                System.out.println("\n=== FIRST-TIME ADMIN SETUP ===");
                String username;
                do {
                    System.out.print("Create admin username: ");
                    username = scanner.nextLine().trim();
                    if (username.isBlank()) System.out.println("Username cannot be empty.");
                } while (username.isBlank());
                String password;
                do {
                    System.out.print("Create admin password: ");
                    password = scanner.nextLine();
                    if (password.isBlank()) System.out.println("Password cannot be empty.");
                } while (password.isBlank());
                userDao.createAdmin(username, password);
                System.out.println("Administrator account created.");
            }
        } catch (SQLException e) {
            System.out.println("Database setup failed: " + e.getMessage());
            scanner.close();
            return;
        }
        Authentication authentication = new Authentication(userDao, new SportDao(), scanner);
        authentication.start();
        scanner.close();
    }
}
