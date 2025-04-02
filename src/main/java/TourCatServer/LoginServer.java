package TourCatServer;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LoginServer {
    private static final int PORT = 12345;
    private static final String CREDENTIALS_FILE = "src/main/resources/credentials.txt";
    private static HashMap<String, String> credentials = new HashMap<>();
    private static Set<String> loggedInUsers = new HashSet<>(); // Track logged-in users

    public static void main(String[] args) {
        // Load credentials from file at startup
        loadCredentialsFromFile();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                // Handle client in a separate thread
                new ClientHandler(socket).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Load credentials from file
    private static void loadCredentialsFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(CREDENTIALS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    credentials.put(parts[0], parts[1]);
                }
            }
            System.out.println("Credentials loaded from file.");
        } catch (IOException e) {
            System.out.println("No credentials file found. Starting with an empty database.");
        }
    }

    // Save credentials to file
    private static void saveCredentialsToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CREDENTIALS_FILE))) {
            for (String username : credentials.keySet()) {
                bw.write(username + ":" + credentials.get(username));
                bw.newLine();
            }
            System.out.println("Credentials saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private String username; // Track the username for this client

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (InputStream input = socket.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                 OutputStream output = socket.getOutputStream();
                 PrintWriter writer = new PrintWriter(output, true)) {

                // Read the request type (LOGIN, REGISTER, or LOGOUT)
                String requestType = reader.readLine();

                if ("LOGIN".equals(requestType)) {
                    // Handle login request
                    handleLogin(reader, writer);
                } else if ("REGISTER".equals(requestType)) {
                    // Handle registration request
                    handleRegistration(reader, writer);
                } else if ("LOGOUT".equals(requestType)) {
                    // Handle logout request
                    handleLogout(reader,writer);
                } else {
                    writer.println("INVALID_REQUEST");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (username != null && loggedInUsers.contains(username)) {
                    System.out.println("Client disconnected: " + username);
                }
            }
        }

        private void handleLogin(BufferedReader reader, PrintWriter writer) throws IOException {
            // Read username and password from client
            username = reader.readLine();
            String password = reader.readLine();

            // Authenticate
            if (credentials.containsKey(username) && credentials.get(username).equals(password)) {
                if (!loggedInUsers.contains(username)) {
                    loggedInUsers.add(username); // Mark user as logged in
                    writer.println("LOGIN_SUCCESS");
                    System.out.println("User logged in: " + username);
                } else {
                    writer.println("LOGIN_FAILED: User already logged in");
                }
            } else {
                writer.println("LOGIN_FAILED: Invalid credentials");
            }
        }

        private void handleRegistration(BufferedReader reader, PrintWriter writer) throws IOException {
            // Read username and password from client
            String newUsername = reader.readLine();
            String newPassword = reader.readLine();

            // Check if username already exists
            if (credentials.containsKey(newUsername)) {
                writer.println("REGISTRATION_FAILED: Username already exists");
            } else {
                // Add new user to credentials
                credentials.put(newUsername, newPassword);
                saveCredentialsToFile(); // Save updated credentials to file
                writer.println("REGISTRATION_SUCCESS");
                System.out.println("New user registered: " + newUsername);
            }
        }

        private void handleLogout(BufferedReader reader, PrintWriter writer) throws IOException {
            String usernameToLogout = reader.readLine(); // Read username from client

            System.out.println("Received logout request for user: " + usernameToLogout);
            System.out.println("Logged-in users before logout: " + loggedInUsers);

            if (usernameToLogout != null && loggedInUsers.contains(usernameToLogout)) {
                loggedInUsers.remove(usernameToLogout);
                writer.println("LOGOUT_SUCCESS");
                System.out.println("User logged out: " + usernameToLogout);
            } else {
                writer.println("LOGOUT_FAILED: User not logged in");
            }
        }

    }
}