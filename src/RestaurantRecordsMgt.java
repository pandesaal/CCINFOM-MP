import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class RestaurantRecordsMgt {
    Connection connection;
    Statement statement;

    public RestaurantRecordsMgt(Connection connection) {
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
            System.out.println("\nRMS Records Management Queries");
            System.out.println("[1] View a product record and the list of orders containing that product");
            System.out.println("[2] View a customer record and their order history");
            System.out.println("[3] View an employee and the shifts assigned to them");
            System.out.println("[4] View an order and the inventory it affected");
            System.out.println("[5] Exit Program");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = Utilities.getUserInput("Choice: ");

                    inputRun = switch (choice) {
                        case 1 -> {
                            viewProduct();
                            yield false;
                        }
                        case 2 -> {
                            viewCustomer();
                            yield false;
                        }
                        case 3 -> {
                            viewEmployee();
                            yield false;
                        }
                        case 4 -> {
                            viewOrder();
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

    private void viewProduct() {
        boolean programRun = true;
        while (programRun) {
            String query = "SELECT * FROM Inventory;";

            try {
                ResultSet resultSet = statement.executeQuery(query);
                List<Integer> rows = new ArrayList<>();

                System.out.println("List of all items in the inventory: ");
                while (resultSet.next()){
                    int id = resultSet.getInt("product_id");
                    String name = resultSet.getString("product_name");
                    int qty = resultSet.getInt("quantity");

                    rows.add(id);

                    System.out.printf("[%d] %s (Stock: %d)\n", id, name, qty);
                }

                boolean inputRun = true;
                while (inputRun){
                    try {
                        int id = Utilities.getUserInput("Product ID of item to view: ");

                        if (rows.contains(id)){

                            query = "SELECT o.order_id, oi.quantity AS quantity_ordered, oi.price_per_unit, o.total_amount AS total_amount_of_order, o.order_datetime\n" +
                                    "FROM Inventory i " +
                                    "JOIN Order_Item oi ON i.product_id = oi.product_id " +
                                    "JOIN Orders o ON o.order_id = oi.order_id " +
                                    "WHERE i.product_id = 15;";

                            try {
                                resultSet = statement.executeQuery(query);

                                System.out.println("\nOrder Details for Product ID: " + id);
                                System.out.println("-----------------------------------------------------");

                                boolean hasRecords = false;

                                while (resultSet.next()) {
                                    hasRecords = true;

                                    int orderId = resultSet.getInt("order_id");
                                    int quantityOrdered = resultSet.getInt("quantity_ordered");
                                    double pricePerUnit = resultSet.getDouble("price_per_unit");
                                    double totalAmount = resultSet.getDouble("total_amount_of_order");
                                    String orderDate = resultSet.getString("order_datetime");

                                    System.out.printf("Order ID: %d | Quantity: %d | Price per unit: %.2f | Total: %.2f | Date: %s\n",
                                            orderId, quantityOrdered, pricePerUnit, totalAmount, orderDate);
                                }

                                if (!hasRecords) {
                                    System.out.println("No order records found for this product.");
                                }

                                System.out.println("-----------------------------------------------------");


                                boolean validChoice = false;
                                while (!validChoice) {
                                    int choice = Utilities.getUserInput("Continue viewing product records? (1 - yes, 2 - no): ");

                                    switch (choice) {
                                        case 1:
                                            validChoice = true;
                                            break;
                                        case 2:
                                            System.out.println("Exiting view products menu...");
                                            programRun = false;
                                            inputRun = false;
                                            validChoice = true;
                                            break;
                                        default:
                                            System.out.println("Invalid choice. Please enter 1 or 2.");
                                            break;
                                    }
                                }
                            } catch (SQLException e) {
                                System.out.println("Query error, edit MySQL database and try again.");
                            }

                        } else {
                            throw new InputMismatchException();
                        }
                    } catch (InputMismatchException e){
                        System.out.println("Invalid input. Please try again.");
                    }
                }

            } catch (SQLException e) {
                System.out.println("Query error, edit MySQL database and try again.");
            }
        }


    }

    private void viewCustomer() {
    }

    private void viewEmployee() {
    }

    private void viewOrder() {
    }

}
