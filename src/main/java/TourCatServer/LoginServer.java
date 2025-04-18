package TourCatServer;

import java.io.*;
import java.net.*;
import java.nio.file.*; // Import NIO Paths
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LoginServer {
    private static final int PORT = 12345;

    // --- Paths ---
    // Internal path (within JAR) for the default/template credentials file
    private static final String INTERNAL_CREDENTIALS_RESOURCE_PATH = "/default_credentials.txt";
    // Filename for the writable credentials file (outside JAR)
    private static final String WRITABLE_CREDENTIALS_FILENAME = "user_credentials.txt";

    // Path to the actual writable credentials file (determined at runtime)
    private Path writableCredentialsPath;

    // --- Data Structures (Keep static if server runs as singleton within app) ---
    private static HashMap<String, String> credentials = new HashMap<>();
    private static Set<String> loggedInUsers = new HashSet<>(); // Track logged-in users

    // --- Instance Variables ---
    private ServerSocket serverSocket;


    /**
     * Private constructor to control instantiation.
     * Call initialize() after creating instance.
     */
    private LoginServer() {}

    /**
     * Initializes the server: determines paths, ensures writable file exists, loads credentials.
     *
     * @throws IOException if initializing paths or files fails.
     * @throws URISyntaxException if finding application directory fails.
     */
    private void initialize() throws IOException, URISyntaxException {
        Path appDataDirectory = getApplicationDirectory(); // Find where writable data should go
        this.writableCredentialsPath = appDataDirectory.resolve(WRITABLE_CREDENTIALS_FILENAME);

        System.out.println("LoginServer: Using writable credentials file: " + this.writableCredentialsPath);

        // Ensure the writable file exists, copy from internal resource if needed
        ensureWritableCredentialsFileExists();

        // Load credentials from the now guaranteed-to-exist writable file
        loadCredentialsFromWritableFile();
    }

    /**
     * Ensures the writable credentials file exists. If not, copies the default
     * credentials from the JAR's internal resources.
     *
     * @throws IOException If file operations fail.
     */
    private void ensureWritableCredentialsFileExists() throws IOException {
        if (!Files.exists(writableCredentialsPath)) {
            System.out.println("LoginServer: Writable credentials file not found. Copying default...");

            URL internalResourceUrl = getClass().getResource(INTERNAL_CREDENTIALS_RESOURCE_PATH);
            if (internalResourceUrl == null) {
                // Option 1: Throw an error - cannot function without a default
                // throw new IOException("Critical: Could not find internal resource: " + INTERNAL_CREDENTIALS_RESOURCE_PATH);

                // Option 2: Create an empty file - allows server to start but with no initial users
                System.err.println("LoginServer: Warning - Could not find internal resource: " + INTERNAL_CREDENTIALS_RESOURCE_PATH + ". Creating empty credentials file.");
                Files.createDirectories(writableCredentialsPath.getParent()); // Ensure parent dir exists
                Files.createFile(writableCredentialsPath); // Create empty file
                return; // Skip copy attempt
            }

            try (InputStream internalStream = getClass().getResourceAsStream(INTERNAL_CREDENTIALS_RESOURCE_PATH)) {
                if (internalStream == null) {
                    throw new IOException("Critical: Could not open internal resource stream: " + INTERNAL_CREDENTIALS_RESOURCE_PATH);
                }
                // Ensure parent directory exists
                Files.createDirectories(writableCredentialsPath.getParent());
                // Copy the file
                Files.copy(internalStream, writableCredentialsPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("LoginServer: Default credentials copied to: " + writableCredentialsPath);
            } catch (IOException e) {
                throw new IOException("LoginServer: Failed to copy internal credentials to " + writableCredentialsPath, e);
            }
        } else {
            System.out.println("LoginServer: Using existing writable credentials file.");
        }
    }


    /**
     * Helper to get the directory for writable application data.
     * Uses JAR location's parent or falls back to user home.
     * (Adapted from CatalogLogic/AddFormLogic)
     * @return Path to the application's writable data directory.
     * @throws URISyntaxException
     */
    private Path getApplicationDirectory() throws URISyntaxException {
        try {
            Path jarPath = Paths.get(LoginServer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path parentDir = Files.isDirectory(jarPath) ? jarPath : jarPath.getParent(); // Handle running from classes vs JAR
            if (parentDir == null) { // Parent might be null if running from root? Unlikely but possible.
                throw new URISyntaxException("JAR path parent is null", jarPath.toString());
            }
            // Let's create a dedicated subdir for TourCat data within the parent dir or user home
            Path dataDir = parentDir.resolve("TourCatData");
            Files.createDirectories(dataDir); // Ensure it exists
            return dataDir;

        } catch (URISyntaxException | NullPointerException | IOException e ) { // Catch broader exceptions during path finding/creation
            System.err.println("LoginServer: Warning - Could not determine application directory reliably. Falling back to user home. Error: " + e.getMessage());
            String userHome = System.getProperty("user.home");
            Path userHomePath = Paths.get(userHome, "TourCatData"); // Subfolder in user home
            try {
                Files.createDirectories(userHomePath); // Ensure the fallback directory exists
            } catch (IOException ioException) {
                System.err.println("LoginServer: Error creating fallback directory in user home: "+ ioException.getMessage());
                // As a last resort, use current working directory, though less reliable
                Path cwdDataPath = Paths.get("").toAbsolutePath().resolve("TourCatData");
                try {
                    Files.createDirectories(cwdDataPath);
                    return cwdDataPath;
                } catch(IOException finalE) {
                    System.err.println("LoginServer: FATAL - Cannot create any data directory.");
                    throw new RuntimeException("Cannot determine or create application data directory", finalE);
                }
            }
            return userHomePath;
        }
    }


    /**
     * Loads credentials from the writable file.
     * Called *after* initialize() ensures the file path is set and file exists.
     */
    private void loadCredentialsFromWritableFile() {
        credentials.clear(); // Clear existing before loading
        try (BufferedReader br = Files.newBufferedReader(writableCredentialsPath)) { // Use NIO Files for reading
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":", 2); // Split only on the first colon
                if (parts.length == 2) {
                    credentials.put(parts[0], parts[1]);
                } else if (!line.trim().isEmpty()){
                    System.err.println("LoginServer: Skipping malformed line in credentials file: " + line);
                }
            }
            System.out.println("LoginServer: Credentials loaded from writable file: " + writableCredentialsPath);
        } catch (IOException e) {
            System.err.println("LoginServer: Error loading credentials from " + writableCredentialsPath + ". Starting with empty credentials. Error: " + e.getMessage());
            // Keep credentials empty, server might still function for registration
        }
    }

    /**
     * Saves credentials to the writable file.
     * Should only be called when credentials change (e.g., registration).
     */
    private void saveCredentialsToWritableFile() {
        // Ensure path is initialized
        if (writableCredentialsPath == null) {
            System.err.println("LoginServer: FATAL - Attempted to save credentials before path was initialized.");
            return;
        }
        // Use try-with-resources for the writer
        try (BufferedWriter bw = Files.newBufferedWriter(writableCredentialsPath)) { // Use NIO Files for writing
            for (String username : credentials.keySet()) {
                bw.write(username + ":" + credentials.get(username));
                bw.newLine();
            }
            System.out.println("LoginServer: Credentials saved to writable file: " + writableCredentialsPath);
        } catch (IOException e) {
            System.err.println("LoginServer: Error saving credentials to " + writableCredentialsPath + ": " + e.getMessage());
            e.printStackTrace(); // Log stack trace for debugging
        }
    }

    /**
     * Starts the server listening loop.
     * Call after initialize().
     */
    public void start() {
        if (writableCredentialsPath == null) {
            System.err.println("LoginServer: Cannot start - server not initialized.");
            return;
        }
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected: " + socket.getInetAddress());
                    // Pass the instance methods for saving to the handler if needed,
                    // or keep save static if credentials map is static.
                    new ClientHandler(socket, this::saveCredentialsToWritableFile).start();
                } catch (IOException e) {
                    System.err.println("LoginServer: Error accepting client connection: " + e.getMessage());
                    // Decide if this is fatal or if the loop should continue
                }
            }
        } catch (IOException ex) {
            System.err.println("LoginServer: Could not start server on port " + PORT + ": " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // Clean up server socket if loop exits
            if(serverSocket != null && !serverSocket.isClosed()){
                try { serverSocket.close(); } catch (IOException e) {/*ignore*/}
            }
        }
    }

    /**
     * Stops the server.
     */
    public void stop() {
        try {
            if(serverSocket != null && !serverSocket.isClosed()){
                serverSocket.close();
                System.out.println("LoginServer: Server stopped.");
            }
        } catch (IOException e) {
            System.err.println("LoginServer: Error stopping server: " + e.getMessage());
        }
    }


    // --- Main Method (Entry Point) ---
    public static void main(String[] args) {
        LoginServer server = new LoginServer();
        try {
            server.initialize(); // Setup paths and load initial data
            server.start(); // Start listening loop
        } catch(Exception e) { // Catch initialization or startup errors
            System.err.println("LoginServer: Failed to start server.");
            e.printStackTrace();
        }

        // Optional: Add shutdown hook for graceful stop
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("LoginServer: Shutdown hook triggered.");
            server.stop();
        }));
    }


    // --- Client Handler (Modified to accept save callback) ---
    private static class ClientHandler extends Thread {
        private Socket socket;
        private String username; // Track the username for this client connection
        private Runnable saveCredentialsCallback; // Callback to trigger saving

        // Constructor accepts the callback
        public ClientHandler(Socket socket, Runnable saveCredentialsCallback) {
            this.socket = socket;
            this.saveCredentialsCallback = saveCredentialsCallback;
        }

        public void run() {
            // Try-with-resources for automatic closing of streams
            try (InputStream input = socket.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                 OutputStream output = socket.getOutputStream();
                 PrintWriter writer = new PrintWriter(output, true)) {

                String requestType = reader.readLine();
                if (requestType == null) {
                    System.out.println("Client disconnected before sending request.");
                    return;
                }

                switch (requestType) {
                    case "LOGIN":
                        handleLogin(reader, writer);
                        break;
                    case "REGISTER":
                        handleRegistration(reader, writer);
                        break;
                    case "LOGOUT":
                        handleLogout(reader, writer);
                        break;
                    default:
                        System.out.println("Received invalid request type: " + requestType);
                        writer.println("INVALID_REQUEST");
                        break;
                }
            } catch (IOException ex) {
                // Log error, could be client disconnecting abruptly
                System.err.println("LoginServer: IOException in ClientHandler for " + (username != null ? username : socket.getInetAddress()) + ": " + ex.getMessage());
                // ex.printStackTrace(); // More detail if needed
            } finally {
                // Clean up loggedInUsers if the client was logged in during this session
                if (username != null && loggedInUsers.contains(username)) {
                    System.out.println("Client disconnected or handler finished: " + username + ". Removing from active users.");
                    // Note: This doesn't trigger a "LOGOUT" action, just cleans up this specific connection's state
                    // Proper logout should be initiated by the client sending "LOGOUT"
                    // loggedInUsers.remove(username); // Reconsider if logout should be the *only* way to remove
                }
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) { /* ignore closing error */ }
            }
        }

        // handleLogin remains largely the same, uses static 'credentials' and 'loggedInUsers'
        private void handleLogin(BufferedReader reader, PrintWriter writer) throws IOException {
            username = reader.readLine(); // Track username for this connection
            String password = reader.readLine();
            if (username == null || password == null) {
                writer.println("LOGIN_FAILED: Incomplete request");
                return;
            }

            if (credentials.containsKey(username) && credentials.get(username).equals(password)) {
                synchronized (loggedInUsers) { // Synchronize access to shared set
                    if (!loggedInUsers.contains(username)) {
                        loggedInUsers.add(username);
                        writer.println("LOGIN_SUCCESS");
                        System.out.println("User logged in: " + username);
                    } else {
                        writer.println("LOGIN_FAILED: User already logged in elsewhere");
                        System.out.println("Login attempt failed for " + username + ": Already logged in.");
                    }
                }
            } else {
                writer.println("LOGIN_FAILED: Invalid credentials");
                System.out.println("Login attempt failed for " + username + ": Invalid credentials.");
            }
        }

        // handleRegistration modified to use the callback after successful registration
        private void handleRegistration(BufferedReader reader, PrintWriter writer) throws IOException {
            String newUsername = reader.readLine();
            String newPassword = reader.readLine();
            if (newUsername == null || newUsername.trim().isEmpty() || newPassword == null || newPassword.isEmpty()) {
                writer.println("REGISTRATION_FAILED: Username or password cannot be empty");
                return;
            }
            newUsername = newUsername.trim(); // Trim whitespace

            synchronized (credentials) {
                synchronized (loggedInUsers) { // Lock both credentials and loggedInUsers
                    if (credentials.containsKey(newUsername)) {
                        writer.println("REGISTRATION_FAILED: Username already exists");
                        System.out.println("Registration failed for " + newUsername + ": Username exists.");
                    } else {
                        credentials.put(newUsername, newPassword);
                        saveCredentialsCallback.run(); // Save the new credentials
                        writer.println("REGISTRATION_SUCCESS");
                        System.out.println("New user registered: " + newUsername);

                        // **Automatically log in the new user**
                        loggedInUsers.add(newUsername);
                        this.username = newUsername; // Track session for this handler
                        writer.println("AUTO_LOGIN_SUCCESS");
                        System.out.println("New user automatically logged in: " + newUsername);
                    }
                }
            }
        }


        // handleLogout remains largely the same, uses static 'loggedInUsers'
        private void handleLogout(BufferedReader reader, PrintWriter writer) throws IOException {
            String usernameToLogout = reader.readLine();
            if (usernameToLogout == null) {
                writer.println("LOGOUT_FAILED: Incomplete request");
                return;
            }

            System.out.println("Received logout request for user: " + usernameToLogout);

            synchronized (loggedInUsers) { // Synchronize access to shared set
                if (loggedInUsers.contains(usernameToLogout)) {
                    loggedInUsers.remove(usernameToLogout);
                    writer.println("LOGOUT_SUCCESS");
                    System.out.println("User logged out via request: " + usernameToLogout);
                    // If this handler was tracking the logged-out user, clear it
                    if (usernameToLogout.equals(this.username)) {
                        this.username = null;
                    }
                } else {
                    writer.println("LOGOUT_FAILED: User not logged in");
                    System.out.println("Logout failed for " + usernameToLogout + ": Not logged in.");
                }
            }
        }
    } // End ClientHandler
}