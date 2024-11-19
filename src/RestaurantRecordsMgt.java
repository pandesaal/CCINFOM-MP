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
                            // fetches all orders that contain a product belonging to the inventory
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
                                detailStmt.setInt(1, id);     // 1 refers to the first ? in the query
                                                              //  id is the value that will replace the placeholder (?) in the query
                                try (ResultSet detailResult = detailStmt.executeQuery()) {

                                    System.out.println("\nOrder Details for Product ID: " + id);
                                    System.out.println("-".repeat(100));
                                    System.out.printf("%-10s %-15s %-15s %-15s\n", "Order ID", "Quantity", "Total Amount", "Order Date");
                                    System.out.println("-".repeat(100));

                                    //  flag to check if any records exist for the given product ID
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

                        } else {    // throws an exception if the id is not in the rows list
                            throw new InputMismatchException("Product ID not found.");
                        }
                    } catch (InputMismatchException e) {     // catches invalid input exceptions and prompts the user to try again
                        System.out.println("Invalid input. Please try again.");
                    }
                }

            } catch (SQLException e) {   // handles SQL-related errors, such as invalid queries or database issues
                System.out.println("Query error, edit MySQL database and try again.");
            }
        }
    }

    private void viewCustomer() {
    }

    private void viewEmployee() {
        boolean programRun = true;
        while (programRun) {
            String query = "SELECT employee_id, first_name, last_name FROM Employee;";

            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet resultSet = pstmt.executeQuery()) {

                List<Integer> employeeIds = new ArrayList<>();

                System.out.println("List of all employees: ");
                boolean hasEmployeesCount;
                hasEmployeesCount = false;

                while (resultSet.next()) {
                    hasEmployeesCount = true;
                    int id = resultSet.getInt("employee_id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");

                    employeeIds.add(id);

                    System.out.printf("[%d] %s %s\n", id, firstName, lastName);
                }

                if (!hasEmployeesCount) {
                    System.out.println("No employees found.");
                    break;
                }

                boolean inputRun = true;
                while (inputRun) {
                    try {
                        int id = Utilities.getUserInput("Employee ID of employee to view: ");

                        if (employeeIds.contains(id)) {

                            query = """
                                    SELECT e.first_name, e.last_name, r.role_name, t.shift_type, t.time_start, t.time_end
                                    FROM Employee e
                                    JOIN Roles r ON e.role_id = r.role_id
                                    JOIN TimeShift t ON e.time_shiftid = t.time_shiftid
                                    WHERE e.employee_id = ?
                                    ORDER BY e.employee_id;
                                    """;

                            try (PreparedStatement detailStmt = connection.prepareStatement(query)) {
                                detailStmt.setInt(1, id);
                                try (ResultSet detailResult = detailStmt.executeQuery()) {

                                    System.out.println("\nEmployee Details for Employee ID: " + id);
                                    System.out.println("-".repeat(100));
                                    System.out.printf("%-10s %-15s %-15s %-20s %-20s %-20s %-20s\n", "Employee ID", "First Name", "Last Name", "Role", "Shift Type", "Start Time", "End Time");
                                    System.out.println("-".repeat(100));

                                    boolean hasEmployeeDetails = false;

                                    while (detailResult.next()) {
                                        hasEmployeeDetails = true;
                                        String firstName = detailResult.getString("first_name");
                                        String lastName = detailResult.getString("last_name");
                                        String roleName = detailResult.getString("role_name");
                                        String shiftType = detailResult.getString("shift_type");
                                        Time startTime = detailResult.getTime("time_start");
                                        Time endTime = detailResult.getTime("time_end");

                                        System.out.printf("%-10d %-15s %-15s %-20s %-20s %-20s %-20s\n", id, firstName, lastName, roleName, shiftType, startTime, endTime);
                                    }

                                    if (!hasEmployeeDetails) {
                                        System.out.println("No details found for this employee.");
                                    }

                                    System.out.println("-".repeat(100));
                                }
                            }

                            boolean validChoice = false;
                            while (!validChoice) {
                                int choice = Utilities.getUserInput("Continue viewing employee records? (1 - yes, 2 - no): ");

                                switch (choice) {
                                    case 1 -> validChoice = true;
                                    case 2 -> {
                                        System.out.println("Exiting view employees menu...");
                                        programRun = false;
                                        inputRun = false;
                                        validChoice = true;
                                    }
                                    default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                                }
                            }
                        } else {
                            throw new InputMismatchException("Employee ID not found.");
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please try again.");
                    } catch (SQLException e) {
                        System.out.println("Database error: " + e.getMessage());
                    }
                }

            } catch (SQLException e) {
                System.out.println("Query error: " + e.getMessage());
            }

        }
    }

    private void viewOrder() {
        boolean programRun = true;
        while (programRun) {
             String query = "SELECT order_id, order_type, order_status FROM Orders;";
             
            try (PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet resultSet = pstmt.executeQuery()) {
    
                // Creates an ArrayList to store orderID values from the query results
                List<Integer> rows = new ArrayList<>();
    
                System.out.println("List of all orders: ");
                /*
                 ResultSet represents the result of a database query, 
                 specifically the rows returned by an SQL SELECT statement.
                 The cursor starts before the first row, call .next() to move
                 it to the first row.*/
                while (resultSet.next()) { // while may next row pa
                    int orderID = resultSet.getInt("order_id");
                    String orderType = resultSet.getString("order_type");
                    String orderStatus = resultSet.getString("order_status");
    
                    /* Adds orderID to the rows list. This keeps track of all
                       orderIDs retrieved, possibly for future operations.*/
                    rows.add(orderID);
    
                    System.out.printf("Order ID: %d\n", orderID);
                    System.out.printf("Order Type: %s\n", orderType);
                    System.out.printf("Order Status: %s\n\n", orderStatus);
                }
           
                boolean inputRun = true;
                while (inputRun) {
                    try {
                        int orderID = Utilities.getUserInput("Order ID to view: ");
    
                        /*
                        Checks if the user-provided id exists in the rows list
                        throws an exception is ID is invalid */
                        if (rows.contains(orderID)) {
                            // View an order and the inventory it affected
                            query = """
                                        SELECT o.order_id, i.product_name, oi.quantity AS "quantity_ordered", 
                                        i.quantity AS "current_stock"\s
                                        FROM Orders o\s
                                        JOIN Order_Item oi ON o.order_id = oi.order_id\s
                                        JOIN Inventory i ON i.product_id = oi.product_id\s
                                        WHERE o.order_id = ?
                                        ORDER BY o.order_id, i.product_name;
                                       \s""";  
    
                            try (PreparedStatement detailStmt = connection.prepareStatement(query)) {
                                detailStmt.setInt(1, orderID);  // 1 refers to the first ? in the query
                                                                //  id is the value that will replace the placeholder (?) in the query
                                try (ResultSet detailResult = detailStmt.executeQuery()) {
    
                                    System.out.println("\nInventory affected by Order ID: " + orderID);
                                    System.out.println("-".repeat(100));               
                                    System.out.printf("%-10s %-25s %-10s %-10s\n",
                                                      // left-aligned string (s) with a minimum width of 10 characters
                                                      "Order ID", "Product Name", "Quantity Ordered", "Current Stock");
                                                      // column headers for the table
                                    System.out.println("-".repeat(100));
    
                                    //  flag to check if any records exist for the given order ID
                                    boolean hasRecords = false;
                                   /*
                                    detailResult represents the rows returned by an SQL query.
                                    while loop iterates through the rows */
                                    while (detailResult.next()) {   // while may next row pa
                                        hasRecords = true;
    
                                        int orderId = detailResult.getInt("order_id");
                                        String productName = detailResult.getString("product_name");
                                        int quantityOrdered = detailResult.getInt("quantity_ordered");
                                        int currentStock = detailResult.getInt("current_stock");
                                        
                                        System.out.printf("%-10s %-25s %-10s %-10s\n",
                                           orderId, productName, quantityOrdered, currentStock);
                                    }
    
                                    if (!hasRecords)
                                        System.out.println("No order records found for this order.");
    
                                    System.out.println("-".repeat(100));  
                                }
                           }
    
                            boolean validChoice = false;
                            while (!validChoice) {
                                int choice = Utilities.getUserInput("Continue viewing order records? (1 - yes, 2 - no): ");
    
                                switch (choice) {
                                    case 1 -> validChoice = true;
                                    case 2 -> {
                                        System.out.println("Exiting view order menu...");
                                        programRun = false;
                                        inputRun = false;
                                        validChoice = true;
                                    }
                                    default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                                }
                            }
                        } else    // throws an exception if the id is not in the rows list
                            throw new InputMismatchException("Order ID not found.");
                    } catch (InputMismatchException e)  {    // catches invalid input exceptions and prompts the user to try again
                        System.out.println("Invalid input. Please try again.");
                      } 
                }
            }  catch (SQLException e) {   // handles SQL-related errors, such as invalid queries or database issues
                        System.out.println("Query error, edit MySQL database and try again.");
                }
        }
    }
}
