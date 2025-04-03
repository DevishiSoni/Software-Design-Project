package TourCatGUI;

import TourCatGUI.Catalog.CatalogLogic;
import TourCatGUI.Forms.AddFormLogic;
import TourCatSystem.DatabaseManager;
// Removed: import TourCatSystem.FileManager; // No longer using FileManager here

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// Removed: java.io.* and java.net.Socket related imports
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URL; // Need URL for resource loading

public class HomePage extends JFrame { // Should probably extend JFrame directly
    private String currentUsername; // Renamed for clarity, can be null

    // Buttons (Consider removing login/logout if not needed)
    JButton homeButton = new JButton("Home");
    JButton login = new JButton("Login");
    JButton catalogueButton = new JButton("Catalogue");
    JButton addButton = new JButton("Add Location"); // Renamed for clarity
    JButton logout = new JButton("Logout");
    JLabel welcomeLabel; // Make it a member variable to update it

    // Constructor now just takes username (can be null/default)
    public HomePage (String username) {
        this.currentUsername = username;

        // Use 'this' JFrame directly instead of creating a separate 'frame' variable
        setTitle("TourCat"); // Set title on 'this' frame
        setLayout(new BorderLayout());
        // setBackground(Color.CYAN); // Background usually set by panel
        getContentPane().setBackground(Color.WHITE); // Set default background

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 500);

        // --- Load Background Image using Classpath Resource ---
        URL skylineUrl = getClass().getResource("/image/torontoSkyline.jpg"); // Path relative to resources root
        System.out.println(skylineUrl.toString());
        BackgroundPanel bgPanel = null;
        if (skylineUrl != null) {
            bgPanel = new BackgroundPanel(skylineUrl, 0.75f); // Use 0.75f for float
        } else {
            System.err.println("Error: Could not find background image resource /image/torontoSkyline.jpg");
            // Create a fallback panel if image loading fails
            bgPanel = new BackgroundPanel(null, 0.75f); // Pass null URL
        }
        // ----------------------------------------------------

        bgPanel.setLayout(new GridBagLayout()); // Layout for the content *on top* of the background
        // setContentPane(bgPanel); // Don't set as content pane, add it to the frame's CENTER

        welcomeLabel = new JLabel(getWelcomeMessage(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 36));
        // Add welcome label to the background panel using constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 50; // Position it nicely
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START; // Center it
        gbc.insets = new Insets(75, 50, 0, 0); // Add some padding
        bgPanel.add(welcomeLabel, gbc);


        // --- Top Panel for Buttons and Search ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Button alignment

        // Configure buttons
        Dimension buttonSize = new Dimension(120, 40); // Adjusted size slightly
        homeButton.setPreferredSize(new Dimension(100, 40));
        login.setPreferredSize(buttonSize);
        catalogueButton.setPreferredSize(buttonSize);
        addButton.setPreferredSize(new Dimension(140, 40));
        logout.setPreferredSize(buttonSize);
        login.setVisible(true);
        logout.setVisible(false);


        // Add buttons to panel
        buttonPanel.add(homeButton);
        buttonPanel.add(login);
        buttonPanel.add(catalogueButton);
        buttonPanel.add(addButton);
        buttonPanel.add(logout);


        topPanel.add(buttonPanel, BorderLayout.WEST);
        // topPanel.add(searchPanel, BorderLayout.EAST); // Removed search panel


        // --- Add components to the main frame ('this') ---
        add(topPanel, BorderLayout.NORTH);
        add(bgPanel, BorderLayout.CENTER); // Add background panel to the center

        login.addActionListener(e -> {
            this.setVisible(false);
            SwingUtilities.invokeLater(() -> {
                LoginGUI loginGUI = new LoginGUI();
                loginGUI.setVisible(true);
            });
            dispose();
            welcomeLabel.setText(getWelcomeMessage());
        });

        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                if (currentUsername == null) {
                    JOptionPane.showMessageDialog(HomePage.this, "No user is currently logged in.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Socket socket = new Socket("localhost", 12345);
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Debug: Print the username being sent
                    System.out.println("Attempting to log out user: " + currentUsername);

                    // Send logout request
                    writer.println("LOGOUT");
                    writer.println(currentUsername); // Send the logged-in username

                    // Receive response from the server
                    String response = reader.readLine();
                    if ("LOGOUT_SUCCESS".equals(response)) {
                        JOptionPane.showMessageDialog(HomePage.this, "Logout Successful!");
                        currentUsername = null; // Clear the logged-in user
                    } else {
                        JOptionPane.showMessageDialog(HomePage.this, response, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                updateLoginLogoutUI();
                welcomeLabel.setText(getWelcomeMessage());
            }
        });


        // --- Add Action Listeners ---
        homeButton.addActionListener(e -> {
            // Already on home, maybe refresh or do nothing?
            JOptionPane.showMessageDialog(this, "Already on Home Page!");
        });

        // Removed login listener

        // Removed logout listener (and associated socket/server code)

        addButton.addActionListener(e -> {
            // Pass the current username (can be null)
            new AddFormLogic(this.currentUsername);
            this.dispose(); // Close the current home page
        });

        catalogueButton.addActionListener(e -> {
            // Pass the current username (can be null)

            new CatalogLogic(this.currentUsername, File);
            this.dispose(); // Close the current home page
        });


        // --- Finalize Frame ---
        setLocationRelativeTo(null); // Center on screen
        setVisible(true); // Make the frame visible
    }

    // Removed updateLoginLogoutUI() as buttons are fixed now

    // Updated welcome message
    private String getWelcomeMessage () {
        if (currentUsername == null || currentUsername.isEmpty() || currentUsername.equalsIgnoreCase("DefaultUser")) {
            return "Welcome to TourCat!";
        } else {
            // Basic sanitation (avoid potential injection if username was user-input)
            String safeUsername = currentUsername.replaceAll("[^a-zA-Z0-9_ .-]", "");
            return "Welcome to TourCat, " + safeUsername + "!";
        }
    }

    public void updateLoginLogoutUI () {
        if (currentUsername == null) {
            login.setVisible(true);
            logout.setVisible(false);
        } else {
            login.setVisible(false);
            logout.setVisible(true);
        }
    }
}