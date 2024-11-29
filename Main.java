package dbmsminipro;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class Main{
	private static final String URL = "jdbc:mysql://localhost:3306/dbmsminipro?useSSL=false";
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = "MySQL@123"; // Replace with your MySQL password

    private static Connection connection;

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database successfully!");

            Scanner scanner = new Scanner(System.in);

            // Step 4: Collect user input for 'user' table
            System.out.println("\nEnter your details to be inserted into the 'user' table:");
            System.out.print("Enter your name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter your contact: ");
            String contact = scanner.nextLine();
            
            System.out.print("Enter your email: ");
            String email = scanner.nextLine();

            String userInsertQuery = "INSERT INTO user (name, contact, email) VALUES (?, ?, ?)";
            PreparedStatement userPreparedStatement = connection.prepareStatement(userInsertQuery, Statement.RETURN_GENERATED_KEYS);
            userPreparedStatement.setString(1, name);
            userPreparedStatement.setString(2, contact);
            userPreparedStatement.setString(3, email);
            userPreparedStatement.executeUpdate();
            
            ResultSet generatedKeys = userPreparedStatement.getGeneratedKeys();
            int userId = 0;
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            }
            userPreparedStatement.close();
            System.out.println("User added successfully with ID: " + userId);

            // Step 5: Collect user input for 'preferences' table with options shown before each input
            System.out.println("\nEnter your preferences to be inserted into the 'preferences' table:");
            
            // Show available transport options
            displayOptions("SELECT DISTINCT type FROM transportation", "Available transport preferences:");
            System.out.print("Enter your transport preference: ");
            String transportPreference = scanner.nextLine();
            
            // Show available hotel options
            displayOptions("SELECT DISTINCT hotel_type FROM hotels", "Available hotel preferences:");
            System.out.print("Enter your hotel preference: ");
            String hotelPreference = scanner.nextLine();
            
            // Show available destination types
            displayOptions("SELECT DISTINCT type FROM destinations", "Available destination types:");
            System.out.print("Enter your destination type: ");
            String destinationType = scanner.nextLine();
            
            // Show available travel months
            displayOptions("SELECT DISTINCT suitable_months FROM destinations", "Available travel months:");
            System.out.print("Enter your travel month: ");
            String travelMonth = scanner.nextLine();
            
            // Collect budget after showing all the preference options
            System.out.print("Enter your budget: ");
            double budget = scanner.nextDouble();
            scanner.nextLine(); // Consume newline

            String preferencesInsertQuery = "INSERT INTO preferences (user_id, budget, transport_preference, hotel_preference, destination_type, travel_month) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preferencesPreparedStatement = connection.prepareStatement(preferencesInsertQuery);
            preferencesPreparedStatement.setInt(1, userId);
            preferencesPreparedStatement.setDouble(2, budget);
            preferencesPreparedStatement.setString(3, transportPreference);
            preferencesPreparedStatement.setString(4, hotelPreference);
            preferencesPreparedStatement.setString(5, destinationType);
            preferencesPreparedStatement.setString(6, travelMonth);
            preferencesPreparedStatement.executeUpdate();
            preferencesPreparedStatement.close();

            System.out.println("Preferences added successfully!");

            connection.close();
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayOptions(String query, String message) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        System.out.println("\n" + message);
        while (resultSet.next()) {
            System.out.println(" - " + resultSet.getString(1));
        }
        resultSet.close();
        statement.close();
    }
}
