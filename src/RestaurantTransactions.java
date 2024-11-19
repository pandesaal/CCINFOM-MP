import java.sql.*;
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

                            }
                        }

                    } else {
                        throw new InputMismatchException();
                    }
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please try again.");
            } catch (SQLException e) {
                System.out.println("Query error, edit MySQL database and try again.");
            }

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
                        validChoice = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter 1 or 2.");
                        break;
                }
            }
        }
    }

    private void processPayment() {
    boolean programRun = true;

    while (programRun) {
        try {
            String query = "SELECT order_id, customer_id, total_amount, payment_status " +
                           "FROM Orders WHERE payment_status = 'Unpaid'";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.isBeforeFirst()) {
                    System.out.println("No unpaid orders found.");
                    return;
                }

                System.out.println("Unpaid Orders:");
                List<List<Object>> orders = new ArrayList<>();

                while (resultSet.next()) {
                    int orderId = resultSet.getInt("order_id");
                    int customerId = resultSet.getInt("customer_id");
                    double totalAmount = resultSet.getDouble("total_amount");

                    orders.add(List.of(orderId, customerId, totalAmount));
                    System.out.printf("[Order ID: %d] Customer ID: %d, Total Amount: %.2f\n",
                                      orderId, customerId, totalAmount);
                }

                boolean inputRun = true;
                while (inputRun) {
                    try {
                        int orderId = Utilities.getUserInput("Enter Order ID to process payment: ");
                        List<Object> order = orders.stream()
                                                    .filter(o -> (int) o.get(0) == orderId)
                                                    .findFirst()
                                                    .orElse(null);

                        if (order == null) {
                            throw new InputMismatchException("Invalid Order ID.");
                        }

                        double totalAmount = (double) order.get(2);
                        int customerId = (int) order.get(1);
                        System.out.printf("Processing payment for Order ID %d (Total Amount: %.2f)\n", orderId, totalAmount);

                        System.out.println("Payment Methods:");
                        System.out.println("[1] Cash");
                        System.out.println("[2] Credit Card");
                        int paymentMethodChoice = Utilities.getUserInput("Choose payment method (1 or 2): ");
                        String paymentMethod = switch (paymentMethodChoice) {
                            case 1 -> "Cash";
                            case 2 -> "Credit Card";
                            default -> throw new InputMismatchException("Invalid payment method choice.");
                        };

                        double paymentAmount = Utilities.getUserInput("Enter payment amount: ");
                        if (paymentAmount < totalAmount) {
                            throw new InputMismatchException("Payment amount is less than the total amount.");
                        }

                        connection.setAutoCommit(false);

                        query = "INSERT INTO Payment (order_id, amount_paid, payment_method, transaction_date) " +
                                "VALUES (?, ?, ?, NOW())";
                        try (PreparedStatement paymentStmt = connection.prepareStatement(query)) {
                            paymentStmt.setInt(1, orderId);
                            paymentStmt.setDouble(2, paymentAmount);
                            paymentStmt.setString(3, paymentMethod);
                            paymentStmt.executeUpdate();
                        }

                        query = "UPDATE Orders SET payment_status = 'Paid' WHERE order_id = ?";
                        try (PreparedStatement orderStmt = connection.prepareStatement(query)) {
                            orderStmt.setInt(1, orderId);
                            orderStmt.executeUpdate();
                        }

                        connection.commit();

                        System.out.printf("Payment for Order ID %d processed successfully.\n", orderId);
                        System.out.println("Receipt:");
                        System.out.printf("Order ID: %d\n", orderId);
                        System.out.printf("Customer ID: %d\n", customerId);
                        System.out.printf("Total Amount: %.2f\n", totalAmount);
                        System.out.printf("Amount Paid: %.2f\n", paymentAmount);
                        System.out.printf("Payment Method: %s\n", paymentMethod);
                        System.out.printf("Change: %.2f\n", paymentAmount - totalAmount);

                        inputRun = false;
                    } catch (InputMismatchException e) {
                        System.out.println(e.getMessage());
                    } catch (SQLException e) {
                        System.out.println("Error processing payment. Rolling back transaction...");
                        connection.rollback();
                        inputRun = false;
                    } finally {
                        connection.setAutoCommit(true);
                    }
                }
            }
        } catch (SQLException e) { //for runtime errors
            System.out.println("Error fetching unpaid orders. Please try again later."); 
        }

        boolean validChoice = false;
        while (!validChoice) {
            try {
                int choice = Utilities.getUserInput("Process another payment? (1 - Yes, 2 - No): ");
                switch (choice) {
                    case 1:
                        validChoice = true;
                        break;
                    case 2:
                        System.out.println("Exiting process payment menu...");
                        programRun = false;
                        validChoice = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }
}

        private void assignShift() {
        boolean programRun = true;

        while (programRun) {
            try {
                // Get Employees without a Shift
                String query = """
                                SELECT e.employee_id, e.first_name, e.last_name, r.role_name
                                FROM Employee e
                                JOIN Roles r ON e.role_id = r.role_id
                                WHERE e.time_shiftid IS NULL
                                ORDER BY role_name;
                                """;

                List<List<Object>> employeesWithoutShift = new ArrayList<>();

                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    System.out.println("Employees without an assigned shift:");
                    while (resultSet.next()) {
                        int id = resultSet.getInt("employee_id");
                        String firstName = resultSet.getString("first_name");
                        String lastName = resultSet.getString("last_name");
                        String roleName = resultSet.getString("role_name");

                        employeesWithoutShift.add(List.of(id, firstName, lastName, roleName));
                        System.out.printf("[%d] %s %s (%s)\n", id, firstName, lastName, roleName);
                    }
                    System.out.println("[0] Exit");

                    if (employeesWithoutShift.isEmpty()) {
                        System.out.println("No employees without a shift found. Exiting...");
                        break;
                    }
                }

                // Ask user to select an Employee
                boolean inputRun = true;
                while (inputRun) {
                    try {
                        int selectedEmployeeId = Utilities.getUserInput("Enter the Employee ID to assign a shift: ");

                        if (selectedEmployeeId == 0) {
                            System.out.println("Exiting shift assignment menu...");
                            programRun = false;
                            inputRun = false;
                            continue;
                        }

                        List<Object> selectedEmployee = employeesWithoutShift.stream()
                                .filter(emp -> (int) emp.getFirst() == selectedEmployeeId)
                                .findFirst()
                                .orElse(null);

                        if (selectedEmployee == null) {
                            throw new InputMismatchException("Invalid Employee ID.");
                        }

                        String employeeRole = (String) selectedEmployee.get(3);

                        // Check for Available Shifts (1:1 role-to-timeshift ratio)
                        query = """
                    SELECT ts.time_shiftid, ts.shift_type, ts.time_start, ts.time_end
                    FROM TimeShift ts
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM Employee e
                        JOIN Roles r ON e.role_id = r.role_id
                        WHERE e.time_shiftid = ts.time_shiftid
                        AND r.role_name = ?
                    )
                    LIMIT 1;
                    """;

                        try (PreparedStatement shiftStmt = connection.prepareStatement(query)) {
                            shiftStmt.setString(1, employeeRole);

                            try (ResultSet shiftResult = shiftStmt.executeQuery()) {
                                if (!shiftResult.next()) {
                                    System.out.println("No available shifts for the selected employee's role. Exiting...");
                                    break;
                                }

                                // Automatically fetch the first available shift
                                int shiftId = shiftResult.getInt("time_shiftid");
                                String shiftType = shiftResult.getString("shift_type");
                                Time startTime = shiftResult.getTime("time_start");
                                Time endTime = shiftResult.getTime("time_end");

                                System.out.printf("First available shift: [%d] %s (%s - %s)\n",
                                        shiftId, shiftType, startTime, endTime);

                                // Confirm shift assignment
                                boolean confirmAssignment = false;
                                System.out.printf("Assign this shift to %s %s (%s)? (1 - Yes, 2 - No)\n",
                                        selectedEmployee.get(1), selectedEmployee.get(2), employeeRole);
                                int choice = Utilities.getUserInput("Confirm choice: ");

                                switch (choice) {
                                    case 1 -> confirmAssignment = true;
                                    case 2 -> {
                                        System.out.println("Shift assignment canceled. Returning to menu...");
                                        continue;
                                    }
                                    default -> {
                                        System.out.println("Invalid choice. Returning to menu...");
                                        continue;
                                    }
                                }

                                // Validate 1:1 (role-to-timeshift) before assignment
                                query = """
                                        SELECT COUNT(*) AS role_count
                                        FROM Employee e
                                        JOIN Roles r ON e.role_id = r.role_id
                                        WHERE e.time_shiftid = ? AND r.role_name = ?;
                                        """;

                                try (PreparedStatement validationStmt = connection.prepareStatement(query)) {
                                    validationStmt.setInt(1, shiftId);
                                    validationStmt.setString(2, employeeRole);

                                    try (ResultSet validationResult = validationStmt.executeQuery()) {
                                        if (validationResult.next() && validationResult.getInt("role_count") > 0) {
                                            System.out.println("Error: This shift is already taken by another employee in the same role.");
                                            continue;
                                        }
                                    }
                                }

                                // Assign the shift to the employee
                                query = "UPDATE Employee SET time_shiftid = ? WHERE employee_id = ?";
                                try (PreparedStatement updateStmt = connection.prepareStatement(query)) {
                                    updateStmt.setInt(1, shiftId);
                                    updateStmt.setInt(2, selectedEmployeeId);

                                    int rowsAffected = updateStmt.executeUpdate();
                                    if (rowsAffected > 0) {
                                        System.out.printf("Shift successfully assigned to %s %s (%s).\n",
                                                selectedEmployee.get(1), selectedEmployee.get(2), employeeRole);
                                    } else {
                                        System.out.println("Failed to assign shift. Please try again.");
                                    }
                                }
                            }
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please try again.");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    
}
