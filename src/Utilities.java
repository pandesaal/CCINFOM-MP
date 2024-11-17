import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Utilities {

    static Scanner sc = new Scanner(System.in);
    public static final int DBSELECT = 1;
    public static final int DBINSERT = 2;

    public static Connection setupConnection(){

        Connection connection = null;
        while (connection == null){
            System.out.print("Connection URL: ");
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

            } catch (SQLException e) {
                System.out.println("Check if your setup for root and password are the same on your pc, change accordingly and run again.");
            }
        }

        return connection;
    }

    public static int getUserInput() {
        int choice = -1;
        boolean validInput = false;
        while (!validInput) {
            try {
                System.out.print("Choice: ");
                choice = Integer.parseInt(sc.nextLine());
                validInput = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
        return choice;
    }

}
