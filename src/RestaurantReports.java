import java.sql.*;
import java.util.InputMismatchException;

public class RestaurantReports {

    Connection connection;
    Statement statement;

    public RestaurantReports(Connection connection) {
        this.connection = connection;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void showMenu() {

        // TODO: figure out how to troubleshoot queries

        boolean programRun = true;
        boolean inputRun;

        while (programRun){
            System.out.println("\nRMS Report Queries");
            System.out.println("[1] View Sales Report");
            System.out.println("[2] View Customer Order Report");
            System.out.println("[3] View Employee Shift Report");
            System.out.println("[4] View Profit Margin Report");
            System.out.println("[5] Exit Program");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = Utilities.getUserInput("Choice: ");

                    inputRun = switch (choice) {
                        case 1 -> {
                            showSalesReport();
                            yield false;
                        }
                        case 2 -> {
                            showCustomerOrderReport();
                            yield false;
                        }
                        case 3 -> {
                            showEmployeeShiftReport();
                            yield false;
                        }
                        case 4 -> {
                            showProfitMarginReport();
                            yield false;
                        }
                        case 5 -> {
                            programRun = false;
                            yield false;
                        }
                        default -> throw new InputMismatchException("Invalid input.");
                    };
                } catch (InputMismatchException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private void showSalesReport() {
    }

    private void showCustomerOrderReport() {

        String query = "SELECT c.customer_name, o.orderid, o.orderdatetime, p.product_name, oi.quantity, oi.total_price " + // not final will adjust accdg to final table
        "FROM orders o " +
        "JOIN customers c ON o.customerid = c.customerid " +
        "JOIN order_items oi ON o.orderid = oi.orderid " +
        "JOIN products p ON oi.productid = p.productid " +
        "ORDER BY o.orderdatetime DESC";

        ResultSet resultSet = null;
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

    private void showEmployeeShiftReport() {
    //TODO: Number of shifts per employee and total hours worked within a given year and month.


    }

    private void showProfitMarginReport() {
        // TODO: Profit margins for each menu item within a given day, month, or year

        boolean programRun = true;
//        while (programRun) {
//            String query = "SELECT * FROM Inventory;";
//
//            try {
//                ResultSet resultSet = statement.executeQuery(query);
//                List<Integer> rows = new ArrayList<>();
//
//                System.out.println("List of all items in the inventory: ");
//                while (resultSet.next()){
//                    int id = resultSet.getInt("product_id");
//                    String name = resultSet.getString("product_name");
//                    int qty = resultSet.getInt("quantity");
//
//                    rows.add(id);
//
//                    System.out.printf("[%d] %s (Stock: %d)\n", id, name, qty);
//                }
//
//                boolean inputRun = true;
//                while (inputRun){
//                    try {
//                        int id = Utilities.getUserInput("Product ID of item to view: ");
//
//                        if (rows.contains(id)){
//
//                            boolean validInputs = false;
//                            while (!validInputs){
//                                int day = Utilities.getUserInput("Enter day (0 if skip): ");
//                                int month = Utilities.getUserInput("Enter month (0 if skip): ");
//                                int year = Utilities.getUserInput("Enter year (0 if skip): ");
//
//
//                            }
//
//                        } else {
//                            throw new InputMismatchException();
//                        }
//                    } catch (InputMismatchException e){
//                        System.out.println("Invalid input. Please try again.");
//                    }
//                }
//
//            } catch (SQLException e) {
//                System.out.println("Query error, edit MySQL database and try again.");
//            }
//        }
    }
}
