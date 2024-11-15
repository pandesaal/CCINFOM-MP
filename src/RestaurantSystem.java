import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;

public class RestaurantSystem {

    Scanner sc = new Scanner(System.in);
    Connection connection;
    Statement statement;
    ResultSet resultSet;

    public RestaurantSystem() {
        connection = null;
    }

    public void showReportsMenu() throws SQLException {

        // TODO: figure out how to troubleshoot queries

        if (connection == null) {
            connection = Utilities.setupConnection();
            statement = connection.createStatement();
        }

        boolean programRun = true;
        boolean inputRun = true;

        while (programRun){
            System.out.println("Restaurant Management System Report Queries");
            System.out.println("[1] View Sales Report");
            System.out.println("[2] View Customer Order Report");
            System.out.println("[3] View Employee Shift Report");
            System.out.println("[4] View Profit Margin Report");
            System.out.println("[5] Exit Program");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = Utilities.getUserInput();

                    switch (choice) {
                        case 1:
                            inputRun = false;
                            showSalesReport();
                            break;
                        case 2:
                            inputRun = false;
                            showCustomerOrderReport();
                            break;
                        case 3:
                            inputRun = false;
                            showEmployeeShiftReport();
                            break;
                        case 4:
                            inputRun = false;
                            showProfitMarginReport();
                            break;
                        case 5:
                            inputRun = false;
                            programRun = false;
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

    public void showSalesReport() {
        boolean programRun = true;
        boolean inputRun = true;
        String query = null;

        while (programRun){
            System.out.println("Sales Report");
            System.out.println("[1] See Total Sales");
            System.out.println("[2] See Average Sales");
            System.out.println("[3] See Top Selling Products");
            System.out.println("[4] See All 3 Options");
            System.out.println("[5] Exit Sales Report Menu");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = Utilities.getUserInput();

                    switch (choice) {
                        case 1:
                            inputRun = false;
                            break;
                        case 2:
                            inputRun = false;
                            break;
                        case 3:
                            inputRun = false;
                            break;
                        case 4:
                            inputRun = false;
                            break;
                        case 5:
                            inputRun = false;
                            programRun = false;
                            System.out.println("Exiting sales report menu...");
                            break;
                        default:
                            throw new InputMismatchException("Invalid input.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println(e.getMessage());
                }
            }

            if (query != null) {
                resultSet = statement.executeQuery(query);
            }
        }
    }

    public void showCustomerOrderReport() {

    }

    public void showEmployeeShiftReport() {

    }

    public void showProfitMarginReport() {

    }
}
