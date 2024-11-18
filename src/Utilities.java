import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Utilities {

    static Scanner sc = new Scanner(System.in);

    @SuppressWarnings("Nullability")
    public static Connection setupConnection(){

        Connection connection = null;
        while (connection == null){
            System.out.print("Connection URL ('host':'portnumber'): ");
            String url = sc.nextLine().trim();

            System.out.print("Username: ");
            String root = sc.nextLine().trim();

            System.out.print("Password: ");
            String password = sc.nextLine().trim();

            try {
                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + url + "/s17_group8",
                        root, //username of db
                        password); //password of db
                System.out.println("Connection successful!\n");

            } catch (SQLException e) {
                System.out.println("Troubleshoot: check if connection is on or login credentials are correct.");
            }
        }

        return connection;
    }

    public static int getUserInput(String prompt) {
        int choice = -1;
        boolean validInput = false;
        while (!validInput) {
            try {
                System.out.print(prompt);
                choice = Integer.parseInt(sc.nextLine().trim());
                validInput = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
        return choice;
    }

}
