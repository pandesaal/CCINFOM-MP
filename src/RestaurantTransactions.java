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
            System.out.println("[4] Manage Employee Shifts");
            System.out.println("[5] Exit Transactions");

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
    /*
        Read the Inventory to check if the selected dish is available
        Record order details: customer ID, products ordered, total amount, assigned employees.
        Update inventory to deduct the quantity of products used for the order from inventory.
        Update customer's order history to reflect the new order.
    */
    
        boolean programRun = true;
        while (programRun) {

        try {
            String showCustomersQuery = "SELECT customer_id, first_name, last_name FROM Customers";
            try (PreparedStatement stmt = connection.prepareStatement(showCustomersQuery);
                ResultSet rs = stmt.executeQuery()) {
                List<List<Object>> rowsCustomer = new ArrayList<>();
               
                System.out.println("Current customers in the database:");
                while (rs.next()) {
                    int customerId = rs.getInt("customer_id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                
                    rowsCustomer.add(List.of(customerId, firstName, lastName));
                    System.out.printf("[%d] %s %s\n", customerId, firstName, lastName);
                }
            }

            // Not needed?
           /* List<Object> rowsCustomer = rowsCustomer.stream()
                        .filter(r -> (int) r.get(0) == customerId)
                        .findFirst()
                        .orElse(null);
            */
            int customerId = Utilities.getUserInput("Enter Customer ID (or 0 to create a new customer): ");
            boolean isCustomerValid = false;

            if (customerId != 0) {
                // Validate if the entered customer ID exists
                String customerQuery = "SELECT customer_id FROM Customers WHERE customer_id = ?";
                try (PreparedStatement customerPstmt = connection.prepareStatement(customerQuery)) {
                    customerPstmt.setInt(1, customerId);
                    ResultSet customerRs = customerPstmt.executeQuery();
                    isCustomerValid = customerRs.next();
                }

                if (!isCustomerValid) {
                    throw new InputMismatchException();
                }
            } else {
                // Option to create a new customer
                String customerLastName = Utilities.getStringInput("Enter last name: ");
                String customerFirstName = Utilities.getStringInput("Enter first name: ");
                String customerPhone = Utilities.getStringInput("Enter phone number: ");
                String customerEmail = Utilities.getStringInput("Enter email: ");
                String customerAddress = Utilities.getStringInput("Enter address: ");

                // Insert new customer into database
                String insertCustomerQuery = "INSERT INTO Customers (last_name, first_name, email, phone_number, address) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertCustomerPstmt = connection.prepareStatement(insertCustomerQuery)) {
                    insertCustomerPstmt.setString(1, customerLastName);
                    insertCustomerPstmt.setString(1, customerFirstName);
                    insertCustomerPstmt.setString(1, customerPhone);
                    insertCustomerPstmt.setString(2, customerEmail);
                    insertCustomerPstmt.setString(3, customerAddress);
                    insertCustomerPstmt.executeUpdate();
                    System.out.println("New customer created successfully!");
                    
                    // Fetch the newly created customer ID (assuming it's auto-generated)
                    try (Statement stmt = connection.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID();")) {
                        if (rs.next()) {
                            customerId = rs.getInt(1);
                        }
                    }
                }
            }

            System.out.println("\nFood categories:");
            System.out.println("[1] Main Course");
            System.out.println("[2] Desserts");
            System.out.println("[3] Beverages");
            System.out.println("[4] Sides");

            int category = Utilities.getUserInput("Enter category to order: ");
            String categoryTxt = switch (category) {
                case 1 -> "Main Course";
                case 2 -> "Desserts";
                case 3 -> "Beverages";
                case 4 -> "Sides";
                default -> throw new InputMismatchException();
            };

            String query = "SELECT * FROM Inventory WHERE category = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, category);
                ResultSet resultSet = preparedStatement.executeQuery();
                List<List<Object>> rows = new ArrayList<>();

                System.out.printf("Dishes in %s category: \n", categoryTxt);
                while (resultSet.next()) {
                    int productID = resultSet.getInt("product_id");
                    String name = resultSet.getString("product_name");
                    int qty = resultSet.getInt("quantity");
                    double sellPrice = resultSet.getDouble("sell_price");

                    rows.add(List.of(productID, name, qty, sellPrice));

                    System.out.printf("[%d] %s (STOCK: %d, PRICE: %.2f)\n", productID, name, qty, sellPrice);
                }

                int productID = Utilities.getUserInput("Product ID of item to order: ");
                List<Object> row = rows.stream()
                        .filter(r -> (int) r.getFirst() == productID)
                        .findFirst()
                        .orElse(null);

                if (row != null) {
                    int orderQty, updatedQuantity, orderType;

                    do {
                        orderQty = Utilities.getUserInput("Amount to order: ");
                        updatedQuantity = (int) row.get(2) - orderQty;
                    } while (updatedQuantity < 0);

                    do {
                        System.out.print("1 - Dine-in\n2 - Takeout\n3 - Delivery\n");
                        orderType = Utilities.getUserInput("Order type: ");
                    } while (orderType < 1 || orderType > 3);

                    int numOfEmployees = Utilities.getUserInput("Number of employees assigned to order: ");
                    ArrayList<Integer> employeesAssigned = new ArrayList<>();

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

                    for (int i = 0; i < numOfEmployees; i++) {
                        int employeeId = Utilities.getUserInput("Employee ID to assign: ");
                        employeesAssigned.add(employeeId);
                    }
                    boolean continueChange = false;

                    if (validEmployeeIds.containsAll(employeesAssigned)) {
                        System.out.printf("Deducting order amount of %s from stock (current stock: %d) to %d. Please confirm (1 - yes, 2 - no)\n",
                                row.get(1), (int) row.get(2), (int) row.get(2) - orderQty);

                        int choice = Utilities.getUserInput("Choice: ");

                        switch (choice) {
                            case 1 -> continueChange = true;
                            case 2 -> {
                                System.out.println("Order not saved.");
                                System.out.println("Exiting place order menu...");
                                programRun = false;
                            }
                            default -> throw new InputMismatchException();
                        }
                        if (continueChange) {
                            query = "UPDATE Inventory " +
                                    "SET quantity = ? " +
                                    "WHERE product_id = ?";

                            try (PreparedStatement updatePstmt = connection.prepareStatement(query)) {
                                updatePstmt.setInt(1, updatedQuantity);
                                updatePstmt.setInt(2, productID);
                                updatePstmt.executeUpdate();
                                System.out.printf("Successfully updated the %s entry.\n", row.get(1));
                            }
                            query = """
                                    INSERT INTO Orders (customer_id, order_datetime, order_type, order_status)
                                    VALUES (?, ?, ?, ?);
                                    """;
                            try (PreparedStatement orderPstmt = connection.prepareStatement(query)) {
                                orderPstmt.setInt(1, customerId);
                                orderPstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                                orderPstmt.setInt(3, orderType);
                                orderPstmt.setString(4, "In Progress");

                                orderPstmt.executeUpdate();
                            }

                            for (int employeeID : employeesAssigned) {
                                query = """
                                    INSERT INTO Assigned_Employee_to_Order (order_id, employee_id)
                                    VALUES ((SELECT order_id
                                    FROM Orders
                                    ORDER BY order_id DESC
                                    LIMIT 1), ?);
                                    """;
                                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                                    pstmt.setInt(1, employeeID);
                                    pstmt.executeUpdate();
                                } catch (SQLException e) {
                                    System.out.println("Error inserting assigned employee: " + e.getMessage());
                                }
                            }
                        }
                    } else {
                        System.out.println("Invalid employee IDs. Please try again.");
                    }

                } else {
                    System.out.println("Product not found. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error processing order: " + e.getMessage());
        } catch (InputMismatchException e) {
            System.out.println("Invalid input, try again.");
        }
    }
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
            List<List<Object>> orders = new ArrayList<>();
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                if (!resultSet.isBeforeFirst()) {
                    System.out.println("No unpaid orders found.");
                    return;
                }

                System.out.println("Unpaid Orders:");
                while (resultSet.next()) {
                    int orderId = resultSet.getInt("order_id");
                    int customerId = resultSet.getInt("customer_id");
                    double totalAmount = resultSet.getDouble("total_amount");

                    orders.add(List.of(orderId, customerId, totalAmount));
                    System.out.printf("[Order ID: %d] Customer ID: %d, Total Amount: %.2f\n",
                                      orderId, customerId, totalAmount);
                }
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
        } catch (SQLException e) {
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
            System.out.println("\n--- Shift Management Menu --- \n NOTE: only one role per shift is allowed");
            System.out.println("[1] Remove Employee from Shift");
            System.out.println("[2] Add Shift to an Employee without Shift");
            System.out.println("[3] Empty All Shifts");
            System.out.println("[4] Exit Shift Management");
            int userChoice = Utilities.getUserInput("Select an option: ");

            switch (userChoice) {
                case 1 -> Utilities.removeEmployeeFromShift(connection);
                case 2 -> Utilities.addShiftToEmployee(connection);
                case 3 -> Utilities.emptyAllShifts(connection);
                case 4 -> {
                    System.out.println("Exiting Shift Management...");
                    programRun = false;
                }
                default -> throw new InputMismatchException();
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please try again.");
        }
    }
}


}
