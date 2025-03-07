import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class LoginGUI extends JFrame {
    public JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
//    private JButton logoutButton;
    private String loggedInUser = null; // Track the logged-in user

    public LoginGUI() {
        // Set up the GUI
        setTitle("TourCat - Login");
        setSize(500, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.CYAN);


        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
//        logoutButton = new JButton("Logout");

        JLabel welcome = new JLabel("Welcome to TourCat! Please login to start touring :)");
        welcome.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
        add(welcome);
        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);
        add(registerButton);
//        add(logoutButton);

        // Disable logout button initially
//        logoutButton.setEnabled(false);

        // Add action listener for the login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                try (Socket socket = new Socket("localhost", 12345);
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Send login request
                    writer.println("LOGIN");
                    writer.println(username);
                    writer.println(password);

                    // Receive response from the server
                    String response = reader.readLine();
                    if ("LOGIN_SUCCESS".equals(response)) {
                        loggedInUser = username; // Track the logged-in user
                        JOptionPane.showMessageDialog(LoginGUI.this, "Login Successful!");
                        loginButton.setEnabled(false);
                        registerButton.setEnabled(false);
//                        logoutButton.setEnabled(true);

                        SwingUtilities.invokeLater(() -> {
                            new HomePage(username); // Open home screen
                            dispose(); // Close login window
                        });

                    } else {
                        JOptionPane.showMessageDialog(LoginGUI.this, response, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add action listener for the register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                try (Socket socket = new Socket("localhost", 12345);
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Send registration request
                    writer.println("REGISTER");
                    writer.println(username);
                    writer.println(password);

                    // Receive response from the server
                    String response = reader.readLine();
                    if ("REGISTRATION_SUCCESS".equals(response)) {
                        JOptionPane.showMessageDialog(LoginGUI.this, "Registration Successful!");

                        SwingUtilities.invokeLater(() -> {
                            new HomePage(username); // Open home screen
                            dispose(); // Close login window
                        });

                    } else {
                        JOptionPane.showMessageDialog(LoginGUI.this, response, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add action listener for the logout button
//        logoutButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (loggedInUser == null) {
//                    JOptionPane.showMessageDialog(LoginGUI.this, "No user is currently logged in.", "Error", JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//                try (Socket socket = new Socket("localhost", 12345);
//                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
//                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//
//                    // Debug: Print the username being sent
//                    System.out.println("Attempting to log out user: " + loggedInUser);
//
//                    // Send logout request
//                    writer.println("LOGOUT");
//                    writer.println(loggedInUser); // Send the logged-in username
//
//                    // Receive response from the server
//                    String response = reader.readLine();
//                    if ("LOGOUT_SUCCESS".equals(response)) {
//                        JOptionPane.showMessageDialog(LoginGUI.this, "Logout Successful!");
//                        loggedInUser = null; // Clear the logged-in user
//                        loginButton.setEnabled(true);
//                        registerButton.setEnabled(true);
//                        logoutButton.setEnabled(false);
//                    } else {
//                        JOptionPane.showMessageDialog(LoginGUI.this, response, "Error", JOptionPane.ERROR_MESSAGE);
//                    }
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginGUI loginGUI = new LoginGUI();
            loginGUI.setVisible(true);
        });
    }
}