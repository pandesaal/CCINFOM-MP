import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/s17_group8",
                    "root", //username of db
                    "password"); //password of db

            Statement statement = connection.createStatement();

            String query = "SELECT * FROM inventory"; //write sql statements as normal
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()){
                System.out.println(resultSet.getString("product_id"));
            }
        } catch (SQLException e) {
            System.out.println("Check if your setup for root and password are the same on your pc, change accordingly and run again.");
            // TODO: change to user inputs
        }
    }
}
