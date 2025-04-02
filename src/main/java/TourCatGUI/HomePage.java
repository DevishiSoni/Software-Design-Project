package TourCatGUI;

import TourCatGUI.Catalog.CatalogLogic;
import TourCatGUI.Forms.AddFormLogic;
// Removed: import TourCatSystem.FileManager; // No longer using FileManager here

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// Removed: java.io.* and java.net.Socket related imports
import java.net.URL; // Need URL for resource loading

public class HomePage extends JFrame { // Should probably extend JFrame directly
   private String currentUsername; // Renamed for clarity, can be null

   // Buttons (Consider removing login/logout if not needed)
   JButton homeButton = new JButton("Home");
   // Removed: JButton login = new JButton("Login");
   JButton catalogueButton = new JButton("Catalogue");
   JButton addButton = new JButton("Add Location"); // Renamed for clarity
   // Removed: JButton logout = new JButton("Logout");
   JButton exitButton = new JButton("Exit"); // Added Exit button

   JLabel welcomeLabel; // Make it a member variable to update it

   // Constructor now just takes username (can be null/default)
   public HomePage(String username) {
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
      welcomeLabel.setForeground(Color.WHITE); // Make text visible on potentially dark background
      // Add welcome label to the background panel using constraints
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0; // Position it nicely
      gbc.weightx = 1.0;
      gbc.weighty = 1.0;
      gbc.anchor = GridBagConstraints.CENTER; // Center it
      gbc.insets = new Insets(10, 10, 10, 10); // Add some padding
      bgPanel.add(welcomeLabel, gbc);


      // --- Top Panel for Buttons and Search ---
      JPanel topPanel = new JPanel(new BorderLayout());
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Button alignment

      // Configure buttons
      Dimension buttonSize = new Dimension(130, 40); // Adjusted size slightly
      homeButton.setPreferredSize(new Dimension(100, 40));
      catalogueButton.setPreferredSize(buttonSize);
      addButton.setPreferredSize(new Dimension(140, 40));
      exitButton.setPreferredSize(buttonSize);

      // Add buttons to panel
      buttonPanel.add(homeButton);
      buttonPanel.add(catalogueButton);
      buttonPanel.add(addButton);
      buttonPanel.add(exitButton);

      // Search components (assuming FuzzyFinder handles search within Catalog view)
      // For simplicity, let's remove the search bar from the HomePage for now.
      // Search functionality is better placed within the Catalog view itself.
      // JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      // JTextField searchBar = new JTextField(15);
      // JButton searchButton = new JButton("Search");
      // searchPanel.add(searchBar);
      // searchPanel.add(searchButton);

      topPanel.add(buttonPanel, BorderLayout.WEST);
      // topPanel.add(searchPanel, BorderLayout.EAST); // Removed search panel


      // --- Add components to the main frame ('this') ---
      add(topPanel, BorderLayout.NORTH);
      add(bgPanel, BorderLayout.CENTER); // Add background panel to the center

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

      catalogueButton.addActionListener( e -> {
         // Pass the current username (can be null)
         new CatalogLogic(this.currentUsername);
         this.dispose(); // Close the current home page
      });

      exitButton.addActionListener(e -> {
         // Confirm exit
         int choice = JOptionPane.showConfirmDialog(this,
                 "Are you sure you want to exit TourCat?",
                 "Confirm Exit",
                 JOptionPane.YES_NO_OPTION);
         if (choice == JOptionPane.YES_OPTION) {
            System.exit(0); // Exit the application
         }
      });

      // --- Finalize Frame ---
      setLocationRelativeTo(null); // Center on screen
      setVisible(true); // Make the frame visible
   }

   // Removed updateLoginLogoutUI() as buttons are fixed now

   // Updated welcome message
   private String getWelcomeMessage() {
      if (currentUsername == null || currentUsername.isEmpty() || currentUsername.equalsIgnoreCase("DefaultUser")) {
         return "Welcome to TourCat!";
      } else {
         // Basic sanitation (avoid potential injection if username was user-input)
         String safeUsername = currentUsername.replaceAll("[^a-zA-Z0-9_ .-]", "");
         return "Welcome to TourCat, " + safeUsername + "!";
      }
   }

   public void updateLoginLogoutUI() {

   }

   // Removed the internal main method, MainApplication is the entry point
}