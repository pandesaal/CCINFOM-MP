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
        try {
            String query;
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
                Main query pulls data related to daily sales for a given date.
                It groups data by DATE(o.order_datetime) which allows us to group by each day of the month.
            
                Subquery determines the Top-Selling Product for each day 
            */
            query = """
                    SELECT\s
                        DATE(o.order_datetime) AS sales_date,
                        SUM(oi.quantity * i.sell_price) AS total_sales,
                        AVG(oi.quantity * i.sell_price) AS average_sales,
                        sub_query.product_name AS top_selling_product,
                        sub_query.product_sales AS top_selling_product_sales
                    FROM Orders o
                    JOIN Order_Item oi ON o.order_id = oi.order_id
                    JOIN Inventory i ON oi.product_id = i.product_id
                    JOIN (
                        SELECT\s
                            DATE(o.order_datetime) AS sub_sales_date,
                            i.product_name,
                            MAX(product_sales) AS product_sales
                        FROM (
                            SELECT\s
                                DATE(o.order_datetime) AS sub_sales_date,
                                oi.product_id,
                                i.product_name,
                                SUM(oi.quantity * i.sell_price) AS product_sales
                            FROM Orders o
                            JOIN Order_Item oi ON o.order_id = oi.order_id
                            JOIN Inventory i ON oi.product_id = i.product_id
                            WHERE o.order_datetime LIKE ?
                            GROUP BY DATE(o.order_datetime), oi.product_id, i.product_name
                        ) AS daily_products
                        GROUP BY sub_sales_date, i.product_name
                    ) AS sub_query ON DATE(o.order_datetime) = sub_query.sub_sales_date
                    WHERE o.order_datetime LIKE ?
                    GROUP BY DATE(o.order_datetime), sub_query.product_name, sub_query.product_sales;
                   \s""";
            
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, date + "%");
                    statement.setString(2, date + "%");

                try (ResultSet result = statement.executeQuery()) {
                    System.out.println("\nSales Report");
                    System.out.println("-".repeat(120));
                    System.out.printf("%-15s %-15s %-15s %-15s %-15s\n",
                            "Sales Date", "Total Sales", "Average Sales", "Top Selling Product (TSP)", "TSP Sales");
                    System.out.println("-".repeat(120));

                    boolean hasRecords = false;

                    while (result.next()) {
                        hasRecords = true;

                        String salesDate = result.getString("sales_date");
                        double totalSales = result.getDouble("total_sales");
                        double averageSales = result.getDouble("average_sales");
                        String topSellingProduct = result.getString("top_selling_product");
                        double TSPsales = result.getDouble("top_selling_product_sales");


                        System.out.printf("%-15s %-10.2f %-10.2f %-25s %-10.2f\n",
                                salesDate, totalSales, averageSales, topSellingProduct, TSPsales);
                    }
                    if (!hasRecords) 
                        System.out.println("No order records found for this product.");

                    System.out.println("-".repeat(120));
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Query error, edit MySQL database and try again."); }
          catch (InputMismatchException e) {
                System.out.println("Invalid input. Please try again.");}
          catch (IllegalFormatException e) {
                System.out.println("Date entered not valid, try again.");}

        boolean validChoice = false;
        while (!validChoice) {
            int choice = Utilities.getUserInput("Continue viewing sales report? (1 - yes, 2 - no): ");
            switch (choice) {
                case 1 -> validChoice = true;
                case 2 -> {
                    System.out.println("Exiting sales report menu...");
                    programRun = false;
                    validChoice = true;
                }
                default -> System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }
} 

    private void showCustomerOrderReport() {
    boolean programRun = true;
    while (programRun) {
        try {
            String date = "";

            boolean validInputs = false;
            while (!validInputs) {
                int day = Utilities.getUserInput("Enter day (0 to skip): ");
                int month = Utilities.getUserInput("Enter month (0 to skip): ");
                int year = Utilities.getUserInput("Enter year (0 to skip): ");

                if (day < 0 || month < 0 || year < 0) throw new InputMismatchException();
                if (day != 0 && month != 0 && year != 0) { // valid date
                    date = Date.valueOf(String.format("%d-%02d-%02d", year, month, day)).toString();
                } else { // partial date filter
                    String yearStr = (year == 0) ? "%" : String.format("%04d", year);
                    String monthStr = (month == 0) ? "%" : String.format("%02d", month);
                    String dayStr = (day == 0) ? "%" : String.format("%02d", day);

                    date = String.format("%s-%s-%s", yearStr, monthStr, dayStr);
                }
                validInputs = true;
            }

            String query = """
                SELECT 
                    COUNT(DISTINCT o.order_id) AS total_orders,
                    SUM(oi.quantity * i.sell_price) AS total_amount_spent,
                    i.product_name AS most_bought_product,
                    SUM(oi.quantity) AS most_bought_quantity
                FROM Orders o
                JOIN Order_Item oi ON o.order_id = oi.order_id
                JOIN Inventory i ON oi.product_id = i.product_id
                WHERE o.order_datetime LIKE ?
                GROUP BY i.product_name
                ORDER BY most_bought_quantity DESC
                LIMIT 1;
            """;

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, date + "%");

                try (ResultSet resultSet = statement.executeQuery()) {
                    System.out.println("\nCustomer Order Report");
                    System.out.println("-".repeat(80));
                    System.out.printf("%-20s %-30s %-20s %-20s\n", "Total Orders", "Total Amount Spent", "Most Bought Product", "Most Bought Quantity");
                    System.out.println("-".repeat(80));

                    boolean hasRecords = false;

                    if (resultSet.next()) {
                        hasRecords = true;

                        int totalOrders = resultSet.getInt("total_orders");
                        double totalAmountSpent = resultSet.getDouble("total_amount_spent");
                        String mostBoughtProduct = resultSet.getString("most_bought_product");
                        int mostBoughtQuantity = resultSet.getInt("most_bought_quantity");

                        System.out.printf("%-20d %-30.2f %-20s %-20d\n", totalOrders, totalAmountSpent, mostBoughtProduct, mostBoughtQuantity);
                    }

                    if (!hasRecords) {
                        System.out.println("No orders found for the specified date.");
                    }

                    System.out.println("-".repeat(80));
                }
            }

        } catch (SQLException e) {
            System.out.println("Query error: " + e.getMessage());
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter valid numeric values.");
        } catch (IllegalArgumentException e) {
            System.out.println("Date entered is not valid. Please try again.");
        }

        boolean validChoice = false;
        while (!validChoice) {
            int choice = Utilities.getUserInput("Continue viewing customer order report? (1 - Yes, 2 - No): ");
            switch (choice) {
                case 1 -> validChoice = true;
                case 2 -> {
                    System.out.println("Exiting customer order report menu...");
                    programRun = false;
                    validChoice = true;
                }
                default -> System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }
}

    private void showProfitMarginReport() {
        // TODO: Profit margins for each menu item within a given day, month, or year

        boolean programRun = true;
        while (programRun) {

            try {
                String date = "";
                String displayDate = "";

                boolean validInputs = false;
                while (!validInputs) {
                    int day = Utilities.getUserInput("Enter day (0 if skip): ");
                    int month = Utilities.getUserInput("Enter month (0 if skip): ");
                    int year = Utilities.getUserInput("Enter year (0 if skip): ");


                    if (day < 0 || month < 0 || year < 0) throw new InputMismatchException();

                    if (day != 0 && month != 0 && year != 0) {
                        date = Date.valueOf(String.format("%d-%02d-%02d", year, month, day)).toString();
                        displayDate = date;

                    } else {
                        date = String.format("%s-%s-%s",
                                year == 0 ? "%" : String.format("%04d", year),
                                month == 0 ? "%" : String.format("%02d", month),
                                day == 0 ? "%" : String.format("%02d%%", day));
                        displayDate = String.format("%s-%s-%s",
                                year == 0 ? "XXXX" : String.format("%04d", year),
                                month == 0 ? "XX" : String.format("%02d", month),
                                day == 0 ? "XX" : String.format("%02d%%", day));
                    }
                    validInputs = true;
                }

                String query = "SELECT " +
                        "    i.product_id, " +
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
                        "WHERE o.order_datetime LIKE ? " +
                        "GROUP BY i.product_id, o.order_id, o.order_datetime;";

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, date);

                    try (ResultSet result = statement.executeQuery()) {
                        System.out.println("\nProfit Margin Report for " + displayDate + ":");
                        System.out.println("-".repeat(120));
                        System.out.printf("%-10s %-10s %-20s %-15s %-15s %-10s %-10s %-10s\n",
                                "Product ID", "Order ID", "Order Date", "Total Orders", "Total Quantity", "Revenue", "Cost", "Profit");
                        System.out.println("-".repeat(120));

                        boolean hasRecords = false;

                        while (result.next()) {
                            hasRecords = true;

                            int productID = result.getInt("product_id");
                            int orderId = result.getInt("order_id");
                            int totalOrdersWithItem = result.getInt("total_orders_with_item");
                            int totalAmountOrdered = result.getInt("total_amount_ordered");
                            double totalRevenue = result.getDouble("total_revenue");
                            double totalCost = result.getDouble("total_cost");
                            double totalProfit = result.getDouble("total_profit");
                            String orderDate = result.getString("order_datetime");

                            System.out.printf("%-10d %-10d %-20s %-15d %-15d %-10.2f %-10.2f %-10.2f\n",
                                    productID, orderId, orderDate, totalOrdersWithItem, totalAmountOrdered, totalRevenue, totalCost, totalProfit);
                        }

                        if (!hasRecords) {
                            System.out.println("No order records found for this product.");
                        }
                        System.out.println("-".repeat(120));
                    }
                }

            } catch (SQLException e) {
                System.out.println("Query error, edit MySQL database and try again.");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please try again.");
            } catch (IllegalFormatException e) {
                System.out.println("Date entered not valid, try again.");
            }

            boolean validChoice = false;
            while (!validChoice) {
                int choice = Utilities.getUserInput("Continue viewing profit margin report? (1 - yes, 2 - no): ");
                switch (choice) {
                    case 1 -> validChoice = true;
                    case 2 -> {
                        System.out.println("Exiting profit margin report menu...");
                        programRun = false;
                        validChoice = true;
                    }
                    default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            }
        }
    }
}
