import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class HomePage extends JFrame {
    private String loggedInUser;
    public HomePage(String username) {

       JFrame frame = new JFrame("TourCat");
       frame.setLayout(new BorderLayout());
       frame.setBackground(Color.CYAN);
       frame.getContentPane().setBackground(Color.WHITE);

       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.setSize(1000, 500);

       JLabel welcomeLabel = new JLabel("Welcome to TourCat " + username + "!", SwingConstants.CENTER);
       welcomeLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 36));
       frame.add(welcomeLabel);
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
       frame.add(welcomeLabel, BorderLayout.CENTER);
       frame.setVisible(true);

       logout.addActionListener(e -> {
           frame.setVisible(false);// Close home screen
           dispose();
           new LoginGUI().setVisible(true); // Open login screen
       });

   }
}
