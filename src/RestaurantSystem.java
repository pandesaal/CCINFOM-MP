import java.sql.*;
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
                            String input = sc.nextLine().trim();

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

        String query = "SELECT c.customer_name, o.orderid, o.orderdatetime, p.product_name, oi.quantity, oi.total_price " + // not final will adjust accdg to final table
        "FROM orders o " +
        "JOIN customers c ON o.customerid = c.customerid " +
        "JOIN order_items oi ON o.orderid = oi.orderid " +
        "JOIN products p ON oi.productid = p.productid " +
        "ORDER BY o.orderdatetime DESC";

        try {
            resultSet = statement.executeQuery(query);

            System.out.println("Customer Order Report:");
            System.out.println("-------------------------------------------------------------");
            System.out.printf("%-20s %-10s %-15s %-20s %-8s %-10s\n", "Customer Name", "Order ID", "Order Date", "Product", "Quantity", "Total Price");
            System.out.println("-------------------------------------------------------------");

            while (resultSet.next()) {
                String customerName = resultSet.getString("customer_name");
                int orderId = resultSet.getInt("order_id");
                Date orderDate = resultSet.getDate("order_date");
                String productName = resultSet.getString("product_name");
                int quantity = resultSet.getInt("quantity");
                double totalPrice = resultSet.getDouble("total_price");

                System.out.printf("%-20s %-10d %-15s %-20s %-8d %-10.2f\n",
                customerName, orderId, orderDate.toString(), productName, quantity, totalPrice);
            }

        //i chat gpted this part so idk if tama to
        } catch (SQLException e) {
            System.out.println("Error fetching customer order report: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public void showEmployeeShiftReport() {    
    //TODO: Number of shifts per employee and total hours worked within a given year and month.


    }

    public void showProfitMarginReport() {

    }
}
