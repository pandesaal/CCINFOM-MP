import java.sql.*;
import java.util.*;

public class RestaurantRecordsMgt {
    Connection connection;

    public RestaurantRecordsMgt(Connection connection) {
        this.connection = connection;
    }

    public void showMenu() {

        boolean programRun = true;
        boolean inputRun;

        while (programRun) {
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
            String query = "SELECT product_id, product_name, quantity FROM Inventory;";

            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet resultSet = pstmt.executeQuery()) {

                List<Integer> rows = new ArrayList<>();

                System.out.println("List of all items in the inventory: ");
                while (resultSet.next()) {
                    int id = resultSet.getInt("product_id");
                    String name = resultSet.getString("product_name");
                    int qty = resultSet.getInt("quantity");

                    rows.add(id);

                    System.out.printf("[%d] %s (Stock: %d)\n", id, name, qty);
                }

                boolean inputRun = true;
                while (inputRun) {
                    try {
                        int id = Utilities.getUserInput("Product ID of item to view: ");

                        if (rows.contains(id)) {
                            // fetches all orders that contain a product belonging to the inventory (tama ba)
                            query = """
                                    SELECT o.order_id, oi.quantity AS quantity_ordered,\s
                                           o.total_amount AS total_amount_of_order, o.order_datetime
                                    FROM Inventory i\s
                                    JOIN Order_Item oi ON i.product_id = oi.product_id\s
                                    JOIN Orders o ON o.order_id = oi.order_id\s
                                    WHERE i.product_id = ?
                                    ORDER BY oi.order_item_id;
                                   \s""";

                            try (PreparedStatement detailStmt = connection.prepareStatement(query)) {
                                detailStmt.setInt(1, id);
                                try (ResultSet detailResult = detailStmt.executeQuery()) {

                                    System.out.println("\nOrder Details for Product ID: " + id);
                                    System.out.println("-".repeat(100));
                                    System.out.printf("%-10s %-15s %-15s %-15s %-20s\n", "Order ID", "Quantity", "Total Amount", "Order Date");
                                    System.out.println("-".repeat(100));

                                    boolean hasRecords = false;

                                    while (detailResult.next()) {
                                        hasRecords = true;

                                        int orderId = detailResult.getInt("order_id");
                                        int quantityOrdered = detailResult.getInt("quantity_ordered");
                                        double totalAmount = detailResult.getDouble("total_amount_of_order");
                                        String orderDate = detailResult.getString("order_datetime");

                                        System.out.printf("%-10d %-15d %-15.2f %-20s\n",
                                                orderId, quantityOrdered, totalAmount, orderDate);
                                    }

                                    if (!hasRecords) {
                                        System.out.println("No order records found for this product.");
                                    }

                                    System.out.println("-".repeat(100));
                                }
                            }

                            boolean validChoice = false;
                            while (!validChoice) {
                                int choice = Utilities.getUserInput("Continue viewing product records? (1 - yes, 2 - no): ");

                                switch (choice) {
                                    case 1 -> validChoice = true;
                                    case 2 -> {
                                        System.out.println("Exiting view products menu...");
                                        programRun = false;
                                        inputRun = false;
                                        validChoice = true;
                                    }
                                    default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                                }
                            }

                        } else {
                            throw new InputMismatchException("Product ID not found.");
                        }
                    } catch (InputMismatchException e) {
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
