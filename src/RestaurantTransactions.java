import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class RestaurantTransactions {
    Connection connection;
    Statement statement;

    public RestaurantTransactions(Connection connection) {
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
            System.out.println("\nRMS Transaction Queries");
            System.out.println("[1] Place an Order");
            System.out.println("[2] Restock Inventory");
            System.out.println("[3] Process Payment");
            System.out.println("[4] Assign Shift to Employee");
            System.out.println("[5] Exit Program");

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

    private void placeOrder(){

    }

    private void restockInventory(){

        boolean programRun = true;
        while (programRun) {
            String query = "SELECT * FROM Inventory;";

            try {
                ResultSet resultSet = statement.executeQuery(query);
                List<List<Object>> rows = new ArrayList<>();

                System.out.println("Items with low stocks: ");
                while (resultSet.next()){
                    int id = resultSet.getInt("product_id");
                    String name = resultSet.getString("product_name");
                    int qty = resultSet.getInt("quantity");

                    rows.add(List.of(id, name, qty));

                    System.out.printf("[%d] %s (Stock: %d)\n", id, name, qty);
                }

                boolean inputRun = true;
                while (inputRun){
                    try {
                        int id = Utilities.getUserInput("Product ID of item to update stock of: ");

                        // filters through the rows list and returns the asked row entry acc'd to id number, null if it doesn't exist
                        List<Object> row = rows.stream()
                                .filter(r -> (int) r.getFirst() == id)
                                .findFirst()
                                .orElse(null);

                        if (row != null){

                            int qty = Utilities.getUserInput("Amount to restock to: ");
                            int employee = Utilities.getUserInput("Employee ID of restocker: ");

                            Set<Integer> validEmployeeIds = new HashSet<>();

                            query = "SELECT employee_id FROM Employee;";
                            try {
                                resultSet = statement.executeQuery(query);

                                while (resultSet.next()) {
                                    validEmployeeIds.add(resultSet.getInt("employee_id"));
                                }
                            } catch (SQLException e) {
                                System.out.println("Error fetching employee data. Please try again later.");
                            }

                            boolean continueChange = false;

                            if (qty > (int) row.get(2) && validEmployeeIds.contains(employee)){
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
                                        "SET quantity = " + qty + ", last_restock = CURRENT_TIMESTAMP, last_restocked_by = " + employee + " " +
                                        "WHERE product_id = " + id + ";";

                                System.out.println(query);

                                statement.executeUpdate(query);
                                System.out.printf("Successfully updated the %s entry.\n", row.get(1));


                                boolean validChoice = false;
                                while (!validChoice) {
                                    int choice = Utilities.getUserInput("Continue restocking inventory? (1 - yes, 2 - no): ");

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

    private void processPayment(){

    }

    private void assignShift(){

    }

}
