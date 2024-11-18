import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RestaurantTransactions {
    Connection connection;

    public RestaurantTransactions(Connection connection) {
        this.connection = connection;
    }

    public void showMenu() {

        boolean programRun = true;
        boolean inputRun;

        while (programRun) {
            System.out.println("\nRMS Transaction Queries");
            System.out.println("[1] Place an Order");
            System.out.println("[2] Restock Inventory");
            System.out.println("[3] Process Payment");
            System.out.println("[4] Assign Shift to Employee");
            System.out.println("[5] Exit Transactions Menu");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = Utilities.getUserInput("Choice: ");

                    switch (choice) {
                        case 1:
                            inputRun = false;
                            placeOrder();
                            break;
                        case 2:
                            inputRun = false;
                            restockInventory();
                            break;
                        case 3:
                            inputRun = false;
                            processPayment();
                            break;
                        case 4:
                            inputRun = false;
                            assignShift();
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

    private void placeOrder() {
    }

    private void restockInventory() {
        boolean programRun = true;
        while (programRun) {
            try {
                int qtyBound = Utilities.getUserInput("Enter max low stock quantity: ");
                if (qtyBound <= 0) throw new InputMismatchException();

                String query = "SELECT * FROM Inventory WHERE quantity <= ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, qtyBound);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    List<List<Object>> rows = new ArrayList<>();

                    System.out.println("Items with low stocks: ");
                    while (resultSet.next()) {
                        int id = resultSet.getInt("product_id");
                        String name = resultSet.getString("product_name");
                        int qty = resultSet.getInt("quantity");

                        rows.add(List.of(id, name, qty));

                        System.out.printf("[%d] %s (Stock: %d)\n", id, name, qty);
                    }

                    boolean inputRun = true;
                    while (inputRun) {
                        try {
                            int id = Utilities.getUserInput("Product ID of item to update stock of: ");

                            // filters through the rows list and returns the asked row entry acc'd to id number, null if it doesn't exist
                            List<Object> row = rows.stream()
                                    .filter(r -> (int) r.getFirst() == id)
                                    .findFirst()
                                    .orElse(null);

                            if (row != null) {
                                int qty = Utilities.getUserInput("Amount to restock to: ");
                                int employee = Utilities.getUserInput("Employee ID of restocker: ");

                                Set<Integer> validEmployeeIds = new HashSet<>();
                                query = "SELECT employee_id FROM Employee;";
                                try (PreparedStatement employeePstmt = connection.prepareStatement(query)) {
                                    ResultSet employeeResultSet = employeePstmt.executeQuery();

                                    while (employeeResultSet.next()) {
                                        validEmployeeIds.add(employeeResultSet.getInt("employee_id"));
                                    }
                                } catch (SQLException e) {
                                    System.out.println("Error fetching employee data. Please try again later.");
                                }

                                boolean continueChange = false;

                                if (qty > (int) row.get(2) && validEmployeeIds.contains(employee)) {
                                    System.out.printf("Updating %s (current stock: %d) to %d. Continue? (1 - yes, 2 - no)\n",
                                            row.get(1), (int) row.get(2), qty);

                                    int choice = Utilities.getUserInput("Choice: ");

                                    switch (choice) {
                                        case 1 -> continueChange = true;
                                        case 2 -> {
                                            System.out.println("Exiting restock inventory menu...");
                                            programRun = false;
                                            inputRun = false;
                                        }
                                        default -> throw new InputMismatchException();
                                    }
                                } else {
                                    throw new InputMismatchException();
                                }

                                if (continueChange) {
                                    query = "UPDATE Inventory " +
                                            "SET quantity = ?, last_restocked_by = ? " +
                                            "WHERE product_id = ?";

                                    try (PreparedStatement updatePstmt = connection.prepareStatement(query)) {
                                        updatePstmt.setInt(1, qty);
                                        updatePstmt.setInt(2, employee);
                                        updatePstmt.setInt(3, id);

                                        updatePstmt.executeUpdate();
                                        System.out.printf("Successfully updated the %s entry.\n", row.get(1));

                                        boolean validChoice = false;
                                        while (!validChoice) {
                                            int nextChoice = Utilities.getUserInput("Continue restocking inventory? (1 - yes, 2 - no): ");

                                            switch (nextChoice) {
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
                                    }

                                }

                            } else {
                                throw new InputMismatchException();
                            }
                        } catch (InputMismatchException e) {
                            System.out.println("Invalid input. Please try again.");
                        }
                    }

                } catch (SQLException e) {
                    System.out.println("Query error, edit MySQL database and try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private void processPayment() {
    }

    private void assignShift() {
    }
}
