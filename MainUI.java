package dbmsminipro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class MainUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String URL = "jdbc:mysql://localhost:3306/dbmsminipro?useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "MySQL@123";
    private Connection connection;

    private JTextField nameField, contactField, emailField, budgetField;
    private JComboBox<String> transportBox, hotelBox, destinationBox, monthBox;
    private JButton submitButton;

    public MainUI() {
        // Setting up frame
        setTitle("Travel Preferences");
        setSize(500, 600);
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(245, 245, 245)); // Light background color

        // Set up constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title Label
        JLabel titleLabel = new JLabel("Enter Your Travel Preferences");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(34, 45, 65));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(titleLabel, gbc);

        // Adding Fields and Labels
        createLabelAndField("Name:", nameField = new JTextField(), 1, gbc);
        createLabelAndField("Contact:", contactField = new JTextField(), 2, gbc);
        createLabelAndField("Email:", emailField = new JTextField(), 3, gbc);
        createLabelAndField("Budget:", budgetField = new JTextField(), 4, gbc);

        // Dropdowns for preferences
        transportBox = new JComboBox<>();
        hotelBox = new JComboBox<>();
        destinationBox = new JComboBox<>();
        monthBox = new JComboBox<>();

        // Populate combo boxes with data from DB
        try {
            connectDatabase();
            populateComboBox("SELECT DISTINCT type FROM transportation", transportBox);
            populateComboBox("SELECT DISTINCT hotel_type FROM hotels", hotelBox);
            populateComboBox("SELECT DISTINCT type FROM destinations", destinationBox);
            populateMonthDropdown(); // Populate months as individual options
        } catch (Exception e) {
            e.printStackTrace();
        }

        createLabelAndField("Transport Preference:", transportBox, 5, gbc);
        createLabelAndField("Hotel Preference:", hotelBox, 6, gbc);
        createLabelAndField("Destination Type:", destinationBox, 7, gbc);
        createLabelAndField("Travel Month:", monthBox, 8, gbc);

        // Submit button with style
        submitButton = new JButton("Submit");
        submitButton.setBackground(new Color(70, 130, 180));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setFocusPainted(false);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 9;
        add(submitButton, gbc);

        // Add action listener to the submit button
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertData();
                recommendDestination();
            }
        });

        setVisible(true);
    }

    private void createLabelAndField(String labelText, Component field, int yPos, GridBagConstraints gbc) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = yPos;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = yPos;
        add(field, gbc);
    }

    private void connectDatabase() throws Exception {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void populateComboBox(String query, JComboBox<String> comboBox) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            comboBox.addItem(resultSet.getString(1));
        }
        resultSet.close();
        statement.close();
    }

    private void populateMonthDropdown() {
        String[] months = {"January", "February", "March", "April", "May", "June",
                           "July", "August", "September", "October", "November", "December"};
        for (String month : months) {
            monthBox.addItem(month);
        }
    }

    private void insertData() {
        try {
            // Insert into user table
            String userInsertQuery = "INSERT INTO user (name, contact, email) VALUES (?, ?, ?)";
            PreparedStatement userPreparedStatement = connection.prepareStatement(userInsertQuery, Statement.RETURN_GENERATED_KEYS);
            userPreparedStatement.setString(1, nameField.getText());
            userPreparedStatement.setString(2, contactField.getText());
            userPreparedStatement.setString(3, emailField.getText());
            userPreparedStatement.executeUpdate();

            ResultSet generatedKeys = userPreparedStatement.getGeneratedKeys();
            int userId = 0;
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            }
            userPreparedStatement.close();

            // Insert into preferences table
            String preferencesInsertQuery = "INSERT INTO preferences (user_id, budget, transport_preference, hotel_preference, destination_type, travel_month) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preferencesPreparedStatement = connection.prepareStatement(preferencesInsertQuery);
            preferencesPreparedStatement.setInt(1, userId);
            preferencesPreparedStatement.setDouble(2, Double.parseDouble(budgetField.getText()));
            preferencesPreparedStatement.setString(3, (String) transportBox.getSelectedItem());
            preferencesPreparedStatement.setString(4, (String) hotelBox.getSelectedItem());
            preferencesPreparedStatement.setString(5, (String) destinationBox.getSelectedItem());
            preferencesPreparedStatement.setString(6, (String) monthBox.getSelectedItem());
            preferencesPreparedStatement.executeUpdate();
            preferencesPreparedStatement.close();

            JOptionPane.showMessageDialog(this, "Data inserted successfully!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error inserting data: " + ex.getMessage());
        }
    }

    private void recommendDestination() {
        try {
            String query = "SELECT d.name, d.description, d.min_cost, t.cost_per_km, h.price_per_night " +
                           "FROM destinations d " +
                           "JOIN transportation t ON t.type = ? " +
                           "JOIN hotels h ON h.hotel_type = ? AND h.destination_id = d.destination_id " +
                           "WHERE d.type = ? AND d.suitable_months LIKE ? " +
                           "AND d.min_cost <= ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, (String) transportBox.getSelectedItem());
            stmt.setString(2, (String) hotelBox.getSelectedItem());
            stmt.setString(3, (String) destinationBox.getSelectedItem());
            
            // Use wildcard pattern to match month selection with range in database
            String selectedMonth = (String) monthBox.getSelectedItem();
            stmt.setString(4, "%" + selectedMonth + "%");
            
            stmt.setDouble(5, Double.parseDouble(budgetField.getText()));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String destinationName = rs.getString("name");
                String description = rs.getString("description");
                double minCost = rs.getDouble("min_cost");
                double transportCost = rs.getDouble("cost_per_km");
                double hotelCost = rs.getDouble("price_per_night");

                String message = String.format("Recommended Destination: %s\nDescription: %s\nMin Cost: %.2f\n" +
                                               "Transport Cost: %.2f per km\nHotel Cost: %.2f per night",
                                               destinationName, description, minCost, transportCost, hotelCost);
                JOptionPane.showMessageDialog(this, message);
            } else {
                JOptionPane.showMessageDialog(this, "No matching destination found for the given preferences.");
            }

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching recommendations: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI());
    }
}
