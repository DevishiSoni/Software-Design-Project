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
    private String loggedInUser;
    public HomePage(String username) {
        loggedInUser = username;

       JFrame frame = new JFrame("TourCat");
       frame.setLayout(new BorderLayout());
       frame.setBackground(Color.CYAN);
       frame.getContentPane().setBackground(Color.WHITE);

       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.setSize(1000, 500);

       BackgroundPanel bgPanel = new BackgroundPanel("torontoSkyline.jpg", 0.75f);
       bgPanel.setLayout(new GridBagLayout());
       setContentPane(bgPanel);

       JLabel welcomeLabel = new JLabel("Welcome to TourCat " + username + "!", SwingConstants.CENTER);
       welcomeLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 36));
       bgPanel.add(welcomeLabel);

       GridBagConstraints gbc = new GridBagConstraints();
       gbc.gridx = 0;
       gbc.gridy = 50;
       gbc.weightx = 1.0;
       gbc.weighty = 1.0;
       gbc.anchor = GridBagConstraints.FIRST_LINE_START;
       gbc.insets = new Insets(75, 50, 0, 0);

       bgPanel.add(welcomeLabel, gbc);

       JTextField searchBar = new JTextField(15);

       JPanel topPanel = new JPanel(new BorderLayout());
       JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));


       JButton homeButton = new JButton("Home");
       JButton catalogue = new JButton("Catalogue");
       JButton add = new JButton("Add to Catalogue");
       JButton delete = new JButton("Delete from Catalogue");
       JButton logout = new JButton("Logout");

       Dimension buttonSize = new Dimension(120, 40);
       homeButton.setPreferredSize(new Dimension(100, 40));
       catalogue.setPreferredSize(buttonSize);
       add.setPreferredSize(new Dimension(140, 40));
       delete.setPreferredSize(new Dimension(175, 40));
       logout.setPreferredSize(buttonSize);

       buttonPanel.add(homeButton);
       buttonPanel.add(catalogue);
       buttonPanel.add(add);
       buttonPanel.add(delete);
       buttonPanel.add(logout);

       JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
       JButton searchButton = new JButton("Search");
       searchPanel.add(searchBar);
       searchPanel.add(searchButton);


       topPanel.add(buttonPanel, BorderLayout.WEST);
       topPanel.add(searchPanel, BorderLayout.EAST);


       frame.add(topPanel, BorderLayout.NORTH);
       frame.add(bgPanel, BorderLayout.CENTER);
       frame.setVisible(true);

       logout.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
             if (loggedInUser == null) {
                 JOptionPane.showMessageDialog(HomePage.this, "No user is currently logged in.", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
             }

             try (Socket socket = new Socket("localhost", 12345);
                  PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                  BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                 // Debug: Print the username being sent
                 System.out.println("Attempting to log out user: " + loggedInUser);

                 // Send logout request
                 writer.println("LOGOUT");
                 writer.println(loggedInUser); // Send the logged-in username

                 // Receive response from the server
                 String response = reader.readLine();
                 if ("LOGOUT_SUCCESS".equals(response)) {
                     JOptionPane.showMessageDialog(HomePage.this, "Logout Successful!");
                     loggedInUser = null; // Clear the logged-in user
                 } else {
                     JOptionPane.showMessageDialog(HomePage.this, response, "Error", JOptionPane.ERROR_MESSAGE);
                 }
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
             frame.setVisible(false);// Close home screen
             dispose();
             new LoginGUI().setVisible(true); // Open login screen
         }
       });

       add.addActionListener(e -> {
          frame.setVisible(false);
          dispose();
          new AddForm(username).setVisible(true);
       });

       delete.addActionListener(e -> {
          frame.setVisible(false);
          dispose();
          new DeleteForm(username).setVisible(true);
       });

       catalogue.addActionListener( e -> {
          frame.setVisible(false);
          dispose();
          new CatalogView(username);

       });
   }
}

class BackgroundPanel extends JPanel {
   private BufferedImage image;
   private float alpha; // Transparency level (0.0 - 1.0)

   public BackgroundPanel(String imagePath, float alpha) {
      this.alpha = alpha;
      try {
         image = ImageIO.read(new File(imagePath));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (image != null) {
         Graphics2D g2d = (Graphics2D) g;
         g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
         g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);
         g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
      }
   }
}
