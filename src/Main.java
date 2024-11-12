import java.sql.*;
import java.util.InputMismatchException;

public class Main {

    public static void main(String[] args) throws SQLException {

        RestaurantSystem system = new RestaurantSystem();
        boolean programRun = true;
        boolean inputRun = true;

        while (programRun){
            System.out.println("Restaurant Management System");
            System.out.println("[1] Start Connection");
            System.out.println("[2] Exit Program");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = Utilities.getUserInput();

                    switch (choice) {
                        case 1:
                            inputRun = false;
                            system.showReportsMenu();
                            break;
                        case 2:
                            inputRun = false;
                            programRun = false;
                            System.out.println("Exiting program...");
                            break;
                        default:
                            throw new InputMismatchException("Invalid input.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

    }
}
