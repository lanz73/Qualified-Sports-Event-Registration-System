
import dao.DatabaseConnection;
import dao.UserDao;
import ui.MainMenu;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            DatabaseConnection.initializeDatabase();
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.out.println("Database setup failed: " + e.getMessage());
            System.out.println(
                    "Check that Maven downloaded the SQLite JDBC dependency."
            );
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            MainMenu menu = new MainMenu(scanner, new UserDao());
            menu.show();
        }
    }
}
