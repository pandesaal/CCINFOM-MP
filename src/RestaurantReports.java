import java.sql.*;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.InputMismatchException;
import java.util.List;

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
    boolean programRun = true;
    while (programRun) {
         String query = "SELECT product_id, product_name, category FROM Inventory;";

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
                int productID = resultSet.getInt("product_id");
                String name = resultSet.getString("product_name");
                String category = resultSet.getString("category");

                /* Adds orderID to the rows list. This keeps track of all
                   orderIDs retrieved, possibly for future operations.*/
                rows.add(productID);
                System.out.printf("[%d] %s (%s)\n", productID, name, category);
            }

            boolean inputRun = true;
            while (inputRun) {
                try {
                    int productID = Utilities.getUserInput("Product ID to view: ");
                    if (!rows.contains(productID)) throw new InputMismatchException("Invalid Product ID.");
                    
                    String date = "";

                    boolean validInputs = false;
                    while (!validInputs){
                        int day = Utilities.getUserInput("Enter day (0 if skip): ");
                        int month = Utilities.getUserInput("Enter month (0 if skip): ");
                        int year = Utilities.getUserInput("Enter year (0 if skip): ");

                        if (day < 0 || month < 0 || year < 0) throw new InputMismatchException();
                        if (day != 0 && month != 0 && year != 0) {  // valid date
                            date = Date.valueOf(String.format("%d-%02d-%02d", year, month, day)).toString();
                        } else {
                            String yearStr, monthStr, dayStr;

                            if (year == 0) 
                                yearStr = "%";  // placeholder for "any year" if the year is not specified
                            else yearStr = String.format("%04d", year); // // formats year as a 4-digit number

                            if (month == 0) 
                                monthStr = "%"; // any month
                            else monthStr = String.format("%02d", month);

                            if (day == 0) 
                                dayStr = "%";   // any day
                            else dayStr = String.format("%02d", day);
                            
                            // construct date string
                            date = String.format("%s-%s-%s", yearStr, monthStr, dayStr);
                        }
                        validInputs = true;
                    }
                    
                    /*
                        Main query pulls data related to daily sales for a given year and month.
                        It groups data by DATE(o.order_datetime) which allows us to group by each day of the month.
                    
                        Subquery determines the Top-Selling Product for each day 
                    */
                    query = """
                            SELECT 
                                DATE(o.order_datetime) AS sales_date,
                                SUM(oi.quantity * i.sell_price) AS total_sales,
                                AVG(oi.quantity * i.sell_price) AS average_sales,
                                sub_query.product_name AS top_selling_product,
                                MAX(sub_query.product_sales) AS top_selling_product_sales
                            FROM Orders o
                            JOIN Order_Item oi ON o.order_id = oi.order_id
                            JOIN Inventory i ON oi.product_id = i.product_id
                            
                            JOIN (
                                SELECT DATE(o.order_datetime) AS sub_sales_date,
                                       oi.product_id,
                                       i.product_name,
                                       SUM(oi.quantity * i.sell_price) AS product_sales
                                FROM Orders o
                                JOIN Order_Item oi ON o.order_id = oi.order_id
                                JOIN Inventory i ON oi.product_id = i.product_id
                                WHERE YEAR(o.order_datetime) = ? AND MONTH(o.order_datetime) = ?
                                GROUP BY DATE(o.order_datetime), oi.product_id
                            )
                            AS sub_query ON DATE(o.order_datetime) = sub_query.sub_sales_date
                            WHERE YEAR(o.order_datetime) = ? AND MONTH(o.order_datetime) = ?
                            GROUP BY DATE(o.order_datetime);
                            """;

                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setInt(1, productID);
                        statement.setString(2, date);

                        try (ResultSet result = statement.executeQuery()) {
                            System.out.println("\nSales Report for Product ID: " + productID);
                            System.out.println("-".repeat(120));
                            System.out.printf("%-15s %-15s %-15 %-15s %-15s\n",
                                    "Sales Date", "Total Sales", "Average Sales", "Top Selling Product (TSP)", "TSP Sales");
                            System.out.println("-".repeat(120));

                            boolean hasRecords = false;

                            while (result.next()) {
                                hasRecords = true;

                                String salesDate = resultSet.getString("sales_date");
                                double totalSales = result.getDouble("total_sales");
                                double averageSales = result.getDouble("average_sales");
                                String topSellingProduct = result.getString("top_selling_product");
                                double TSPsales = result.getDouble("top_selling_product_sales");


                                System.out.printf("%-15d %-10.2f %-10.2f %-25d %-10.2f\n",
                                        salesDate, totalSales, averageSales, topSellingProduct, TSPsales);
                            }
                            if (!hasRecords) 
                                System.out.println("No order records found for this product.");

                            System.out.println("-".repeat(120));
                        }
                    }
                    boolean validChoice = false;
                    while (!validChoice) {
                        int choice = Utilities.getUserInput("Continue viewing sales report? (1 - yes, 2 - no): ");
                        switch (choice) {
                            case 1 -> validChoice = true;
                            case 2 -> {
                                System.out.println("Exiting sales report menu...");
                                programRun = false;
                                inputRun = false;
                                validChoice = true;
                            }
                            default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                        }
                    }
                } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please try again.");}
                  catch (IllegalFormatException e) {
                        System.out.println("Date entered not valid, try again.");}
            }
        } catch (SQLException e) {
                System.out.println("Query error, edit MySQL database and try again.");
            }
    } 
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
        while (programRun) {
            String query = "SELECT * FROM Inventory;";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

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
                        if (!rows.contains(id)) throw new InputMismatchException("Invalid Product ID.");

                        String date = "";

                        boolean validInputs = false;
                        while (!validInputs){
                            int day = Utilities.getUserInput("Enter day (0 if skip): ");
                            int month = Utilities.getUserInput("Enter month (0 if skip): ");
                            int year = Utilities.getUserInput("Enter year (0 if skip): ");


                            if (day < 0 || month < 0 || year < 0) throw new InputMismatchException();

                            if (day != 0 && month != 0 && year != 0){
                                date = Date.valueOf(String.format("%d-%02d-%02d", year, month, day)).toString();

                            } else {
                                date = String.format("%s-%s-%s",
                                        year == 0 ? "%" : String.format("%04d", year),
                                        month == 0 ? "%" : String.format("%02d", month),
                                        day == 0 ? "%" : String.format("%02d%%", day));
                            }
                            validInputs = true;
                        }
                            query = "SELECT " +
                                "    o.order_id, " +
                                "    COUNT(*) AS total_orders_with_item, " +
                                "    SUM(oi.quantity) AS total_amount_ordered, " +
                                "    SUM(oi.quantity) * i.sell_price AS total_revenue, " +
                                "    SUM(oi.quantity) * i.make_price AS total_cost, " +
                                "    (SUM(oi.quantity) * i.sell_price) - (SUM(oi.quantity) * i.make_price) AS total_profit, " +
                                "    o.order_datetime " +
                                "FROM Inventory i " +
                                "   JOIN Order_Item oi ON i.product_id = oi.product_id " +
                                "   JOIN Orders o ON o.order_id = oi.order_id " +
                                "WHERE i.product_id = ? AND o.order_datetime LIKE ? " +
                                "GROUP BY i.product_id, o.order_id, o.order_datetime;";

                        try (PreparedStatement statement = connection.prepareStatement(query)) {
                            statement.setInt(1, id);
                            statement.setString(2, date);

                            try (ResultSet result = statement.executeQuery()) {
                                System.out.println("\nProfit Margin Report for Product ID: " + id);
                                System.out.println("-".repeat(120));
                                System.out.printf("%-10s %-20s %-15s %-15s %-10s %-10s %-10s\n",
                                        "Order ID", "Order Date", "Total Orders", "Total Quantity", "Revenue", "Cost", "Profit");
                                System.out.println("-".repeat(120));

                                boolean hasRecords = false;

                                while (result.next()) {
                                    hasRecords = true;

                                    int orderId = result.getInt("order_id");
                                    int totalOrdersWithItem = result.getInt("total_orders_with_item");
                                    int totalAmountOrdered = result.getInt("total_amount_ordered");
                                    double totalRevenue = result.getDouble("total_revenue");
                                    double totalCost = result.getDouble("total_cost");
                                    double totalProfit = result.getDouble("total_profit");
                                    String orderDate = result.getString("order_datetime");

                                    System.out.printf("%-10d %-20s %-15d %-15d %-10.2f %-10.2f %-10.2f\n",
                                            orderId, orderDate, totalOrdersWithItem, totalAmountOrdered, totalRevenue, totalCost, totalProfit);
                                }

                                if (!hasRecords) {
                                    System.out.println("No order records found for this product.");
                                }
                                System.out.println("-".repeat(120));
                            }
                        }

                        boolean validChoice = false;
                        while (!validChoice) {
                            int choice = Utilities.getUserInput("Continue viewing profit margin report? (1 - yes, 2 - no): ");
                            switch (choice) {
                                case 1 -> validChoice = true;
                                case 2 -> {
                                    System.out.println("Exiting profit margin report menu...");
                                    programRun = false;
                                    inputRun = false;
                                    validChoice = true;
                                }
                                default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                            }
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please try again.");
                    } catch (IllegalFormatException e) {
                        System.out.println("Date entered not valid, try again.");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Query error, edit MySQL database and try again.");
            }
        }
    }
}
