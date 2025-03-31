package TourCatGUI;

import TourCatData.DatabaseManager;
import TourCatService.LocationService;
import TourCatData.FileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class HomePage extends JFrame {

   //Keep track of the user..
   private String loggedInUser;

   //Function Buttons.
   JButton homeButton = new JButton("Home");
   JButton loginButton = new JButton("Login");
   JButton catalogueButton = new JButton("Catalogue");
   JButton addButton = new JButton("Add to Catalogue");
   JButton logoutButton = new JButton("Logout");

   //Locaiton Service for altering locations in database and viewing.
   public LocationService locationService;

   //Set up the home page.

   public HomePage(String username, LocationService service) {
      super("TourCat");
      this.loggedInUser = username;
      this.locationService = service;

      this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      Dimension initialSize = new Dimension(1000, 500);

      this.setSize(initialSize);
      this.setMinimumSize(initialSize);
      // NO Need to set layout on 'this' JFrame directly when using setContentPane

      // --- Background Panel Setup ---
      File skylineImg = FileManager.getInstance().getImageFile("torontoSkyline.jpg");
      BackgroundPanel bgPanel = new BackgroundPanel(skylineImg, 0.75f);
      // Set GridBagLayout on the panel that WILL BE the content pane
      bgPanel.setLayout(new GridBagLayout());
      setContentPane(bgPanel); // Make bgPanel the content pane

      // --- Welcome Label ---
      JLabel welcomeLabel = new JLabel(getWelcomeMessage(), SwingConstants.CENTER);
      welcomeLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 36));
      welcomeLabel.setForeground(Color.WHITE);
      welcomeLabel.setOpaque(false);

      GridBagConstraints gbcWelcome = new GridBagConstraints(); // Use separate GBC for clarity
      gbcWelcome.gridx = 0;
      gbcWelcome.gridy = 1; // Place welcome label below top panel (gridy=1)
      gbcWelcome.weightx = 1.0;
      gbcWelcome.weighty = 1.0; // Let welcome label area take remaining vertical space
      gbcWelcome.anchor = GridBagConstraints.NORTHWEST; // Center it within its space
      gbcWelcome.insets = new Insets(40, 40, 40, 40);
      // Add welcomeLabel to bgPanel (the content pane)
      bgPanel.add(welcomeLabel, gbcWelcome);

      // --- Top Panel (Buttons & Search) ---
      homeButton = new JButton("Home");
      loginButton = new JButton("Login");
      catalogueButton = new JButton("Catalogue");
      addButton = new JButton("Add to Catalogue");
      logoutButton = new JButton("Logout");
      // ... configure buttons ...
      updateLoginLogoutUI();

      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      buttonPanel.setOpaque(false);
      buttonPanel.add(homeButton);
      buttonPanel.add(loginButton);
      buttonPanel.add(catalogueButton);
      buttonPanel.add(addButton);


      JTextField searchBar = new JTextField(15);
      JButton searchButton = new JButton("Search");
      JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      searchPanel.setOpaque(false);
      searchPanel.add(searchBar);
      searchPanel.add(searchButton);

      // Create the topPanel itself
      JPanel topPanel = new JPanel(new BorderLayout());
      topPanel.setOpaque(false);
      topPanel.add(buttonPanel, BorderLayout.WEST);
      topPanel.add(searchPanel, BorderLayout.EAST);

      // --- FIX: Add topPanel to bgPanel using GridBagConstraints ---
      GridBagConstraints gbcTopPanel = new GridBagConstraints();
      gbcTopPanel.gridx = 0;      // First column
      gbcTopPanel.gridy = 0;      // First row (top)
      gbcTopPanel.weightx = 1.0;  // Take full width
      gbcTopPanel.weighty = 0;    // Don't take vertical space beyond preferred size
      gbcTopPanel.anchor = GridBagConstraints.NORTH; // Stick to the top
      gbcTopPanel.fill = GridBagConstraints.HORIZONTAL; // Ensure it fills horizontally
      gbcTopPanel.insets = new Insets(0, 0, 0, 0); // No padding needed usually

      // Add topPanel to bgPanel (the content pane) with these constraints
      bgPanel.add(topPanel, gbcTopPanel);

      // --- Action Listeners ---
      addHomePageActionListeners(welcomeLabel);

      // --- Finalize ---
      this.setLocationRelativeTo(null);
      this.setVisible(true);
   }

   // Helper method for adding action listeners
   private void addHomePageActionListeners(JLabel welcomeLabel) {
      loginButton.addActionListener(e -> {
         // this.setVisible(false); // Don't hide, dispose
         SwingUtilities.invokeLater(() -> {
            LoginGUI loginGUI = new LoginGUI();
            loginGUI.setVisible(true);
         });
         dispose(); // Close this HomePage window
         // No need to update welcomeLabel here, the window is closing
      });

      logoutButton.addActionListener(e -> performLogout(welcomeLabel)); // Call helper

      addButton.addActionListener(e -> {
         // this.setVisible(false); // Don't hide, dispose
         new AddForm(loggedInUser, locationService).setVisible(true);
         dispose();
      });

      catalogueButton.addActionListener(e -> {
         // this.setVisible(false); // Don't hide, dispose
         new CatalogView(loggedInUser, locationService); // CatalogView handles its visibility
         dispose();
      });

      homeButton.addActionListener(e -> {
         // Already home, do nothing or refresh if needed
         System.out.println("Home button clicked.");
      });
   }

   // Extracted logout logic (still needs SwingWorker ideally)
   private void performLogout(JLabel welcomeLabel) {
      if (loggedInUser == null) {
         JOptionPane.showMessageDialog(this, "No user is currently logged in.", "Error", JOptionPane.ERROR_MESSAGE);
         return;
      }

      // TODO: Wrap this network call in a SwingWorker!
      try (Socket socket = new Socket("localhost", 12345);
           PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
           BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

         System.out.println("Attempting to log out user: " + loggedInUser);
         writer.println("LOGOUT");
         writer.println(loggedInUser);

         String response = reader.readLine();
         if ("LOGOUT_SUCCESS".equals(response)) {
            JOptionPane.showMessageDialog(this, "Logout Successful!");
            loggedInUser = null; // Clear the logged-in user
         } else {
            JOptionPane.showMessageDialog(this, "Logout Failed: " + response, "Error", JOptionPane.ERROR_MESSAGE);
         }
      } catch (IOException ex) {
         JOptionPane.showMessageDialog(this, "Logout Error: Could not connect to server.", "Network Error", JOptionPane.ERROR_MESSAGE);
         ex.printStackTrace(); // Log for debugging
      } finally {
         // Update UI regardless of success/failure if user *attempted* logout
         updateLoginLogoutUI();
         welcomeLabel.setText(getWelcomeMessage());
      }
   }


   // Call this method after login/logout to update button visibility
   public void updateLoginLogoutUI() {
//      boolean isLoggedIn = (loggedInUser != null && !loggedInUser.isBlank());
//      loginButton.setVisible(!isLoggedIn);
//      logoutButton.setVisible(isLoggedIn);
//      // Only enable Add/Logout if logged in
//      addButton.setEnabled(isLoggedIn);
//      logoutButton.setEnabled(isLoggedIn); // Should only be visible if enabled anyway
//      // Decide if Catalogue is always enabled or requires login
//      // catalogueButton.setEnabled(isLoggedIn);
      homeButton.setEnabled(true);
      loginButton.setEnabled(loggedInUser == null);
      logoutButton.setEnabled(loggedInUser != null);
      addButton.setEnabled(true);
      catalogueButton.setEnabled(true);


   }

   private String getWelcomeMessage() {
      return (loggedInUser == null || loggedInUser.isBlank())
              ? "Welcome to TourCat!"
              : "Welcome to TourCat, " + loggedInUser + "!";
   }
}



class BackgroundPanel extends JPanel {
   private BufferedImage image;
   private float alpha; // Transparency level (0.0 - 1.0)

   public BackgroundPanel(File imageFile, float alpha) { // Constructor takes File
      this.alpha = Math.max(0.0f, Math.min(1.0f, alpha)); // Clamp alpha 0.0-1.0
      this.setOpaque(false); // Important for transparency to work well

      if (imageFile != null && imageFile.exists()) {
         try {
            System.out.println("Reading image: " + imageFile.getPath());
            image = ImageIO.read(imageFile);
            if (image == null) {
               System.err.println("ImageIO.read returned null for: " + imageFile.getPath());
            }
         } catch (IOException e) { // Catch specific IO exception
            System.err.println("Error reading background image: " + imageFile.getPath());
            e.printStackTrace();
            image = null; // Ensure image is null on error
         }
      } else {
         System.err.println("Background image file not found or is null: " + (imageFile != null ? imageFile.getPath() : "null"));
         image = null;
      }
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g); // Important for JPanel painting cycle
      if (image != null) {
         Graphics2D g2d = (Graphics2D) g.create(); // Create a copy for safe modification
         try {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            // Draw image scaled to panel size
            g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);
         } finally {
            g2d.dispose(); // Dispose of the graphics copy
         }
      } else {
         // Optional: Draw a fallback background color if image failed to load
         g.setColor(Color.DARK_GRAY); // Example fallback
         g.fillRect(0, 0, getWidth(), getHeight());
         System.out.println("Drawing fallback background color.");
      }
   }

   public static void main(String[] args) {


      FileManager fileManager = FileManager.getInstance(true);
      DatabaseManager databaseManager = new DatabaseManager();

      LocationService service = new LocationService(databaseManager, fileManager);
      HomePage homePage = new HomePage("Username", service);
   }
}
