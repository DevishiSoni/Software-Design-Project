package TourCatGUI.Forms;

import TourCatGUI.HomePage;
import TourCatSystem.DatabaseManager;
// Assuming FileManager might be replaced or adapted for writable paths
// import TourCatSystem.FileManager;
import org.apache.commons.io.FilenameUtils; // Ensure this dependency is present

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*; // Use NIO paths
import java.util.OptionalInt;

public class AddFormLogic {

    private final AddFormGUI gui; // Reference to the GUI
    private final String username;
    private final File writableDatabaseFile; // Path to the writable database
    private final Path writableImageDirectory; // Path to the writable image folder
    private File selectedImage = null; // Holds the currently selected image file (from file chooser)

    private final DatabaseManager databaseManager; // Instance initialized once

    // Constants (should match CatalogLogic if shared)
    private static final String INTERNAL_DB_PATH = "/database.csv";
    private static final String WRITABLE_DB_FILENAME = "userdata_database.csv";
    private static final String WRITABLE_IMAGE_DIRNAME = "images"; // Subdirectory for images


    public AddFormLogic (String username) {
        this.username = username;

        try {
            // 1. Determine and prepare the writable database file location
            this.writableDatabaseFile = initializeWritableDatabase();

            // 2. Determine the writable image directory path
            this.writableImageDirectory = writableDatabaseFile.getParentFile().toPath().resolve(WRITABLE_IMAGE_DIRNAME);
            // Ensure the writable image directory exists
            Files.createDirectories(this.writableImageDirectory);
            System.out.println("Using writable image directory: " + this.writableImageDirectory);

            // 3. Initialize DatabaseManager *once* with the writable file path
            this.databaseManager = new DatabaseManager(writableDatabaseFile);

            // 4. Create the GUI, passing this logic instance
            this.gui = new AddFormGUI(username, this);

            // 5. Make the GUI visible
            this.gui.setVisible(true);

        } catch (IOException | URISyntaxException e) {
            // Handle critical initialization errors
            System.err.println("FATAL: Could not initialize Add Form logic. " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error initializing form resources.\n" + e.getMessage() + "\nPlease check file permissions or contact support.",
                    "Initialization Error", JOptionPane.ERROR_MESSAGE);
            // Cannot proceed without the database, throw runtime exception or handle gracefully
            throw new RuntimeException("Failed to initialize AddFormLogic", e);
        } catch (IllegalArgumentException e) {
            System.err.println("FATAL: Configuration error during Add Form initialization. " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Configuration error.\n" + e.getMessage(),
                    "Initialization Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Failed to initialize AddFormLogic", e);
        }
    }

    // --- Helper methods for initializing writable paths (potentially move to a shared utility) ---

    /**
     * Ensures the writable database file exists, copying the default from resources if needed.
     *
     * @return The File object pointing to the writable database.
     * @throws IOException        If file operations fail.
     * @throws URISyntaxException If finding the app's running location fails.
     */
    private File initializeWritableDatabase () throws IOException, URISyntaxException {
        Path applicationDirectory = getApplicationDirectory();
        Path externalDbPath = applicationDirectory.resolve(WRITABLE_DB_FILENAME);
        File externalDbFile = externalDbPath.toFile();

        if (!externalDbFile.exists()) {
            System.out.println("Writable database not found at " + externalDbPath + ". Copying default...");
            URL internalDbUrl = getClass().getResource(INTERNAL_DB_PATH);
            if (internalDbUrl == null) {
                throw new IOException("Could not find internal resource: " + INTERNAL_DB_PATH);
            }

            try (InputStream internalStream = getClass().getResourceAsStream(INTERNAL_DB_PATH)) {
                if (internalStream == null) {
                    throw new IOException("Could not open internal resource stream: " + INTERNAL_DB_PATH);
                }
                Files.createDirectories(externalDbPath.getParent());
                Files.copy(internalStream, externalDbPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Default database copied to: " + externalDbPath);
            } catch (IOException e) {
                throw new IOException("Failed to copy internal database to " + externalDbPath, e);
            }
        } else {
            System.out.println("Using existing writable database at: " + externalDbPath);
        }
        return externalDbFile;
    }

    /**
     * Helper to get the directory where the JAR/application is running.
     * Falls back to user home directory if running location is problematic.
     */
    private Path getApplicationDirectory () throws URISyntaxException {
        try {
            Path jarPath = Paths.get(AddFormLogic.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isDirectory(jarPath)) {
                return jarPath; // IDE
            }
            return jarPath.getParent(); // JAR
        } catch (Exception e) {
            System.err.println("Warning: Could not determine application directory. Falling back to user home. Error: " + e.getMessage());
            String userHome = System.getProperty("user.home");
            Path userHomePath = Paths.get(userHome, "TourCatData"); // Subfolder
            try {
                Files.createDirectories(userHomePath);
            } catch (IOException ioException) {
                System.err.println("Error creating fallback directory: " + ioException.getMessage());
                return Paths.get("").toAbsolutePath(); // Last resort CWD
            }
            return userHomePath;
        }
    }

    // --- Action Handlers (Called by GUI listeners) ---

    /**
     * Handles the action when the 'Choose Image' button is clicked.
     */
    public void handleUploadImageAction () {
        File file = gui.showImageFileChooser();
        if (file != null) {
            // Check file existence and readability before proceeding
            if (!file.exists() || !file.canRead()) {
                gui.showError("Cannot read selected image file: " + file.getName());
                this.selectedImage = null;
                gui.setImagePreview(null);
                return;
            }

            this.selectedImage = file;
            try {
                // Create a scaled ImageIcon for the preview (using File path is OK here)
                ImageIcon originalIcon = new ImageIcon(selectedImage.getAbsolutePath());
                if (originalIcon.getIconWidth() <= 0) { // Basic check if image loaded
                    throw new Exception("ImageIcon could not load image data.");
                }
                Image scaledImage = originalIcon.getImage().getScaledInstance(
                        150, 120, Image.SCALE_SMOOTH); // Adjust preview size if needed
                ImageIcon previewIcon = new ImageIcon(scaledImage);
                gui.setImagePreview(previewIcon);
                gui.setSubmissionReply("Image selected: " + file.getName(), false);
            } catch (Exception e) {
                System.err.println("Error creating image preview: " + e.getMessage());
                gui.setImagePreview(null);
                gui.setSubmissionReply("Error loading image preview.", true);
                this.selectedImage = null; // Invalidate on error
            }
        } else {
            gui.setSubmissionReply("Image selection cancelled.", false);
        }
    }

    /**
     * Handles the action when the 'Submit' button is clicked.
     */
    public void handleSubmitAction () {
        // 1. Get data from GUI
        String name = gui.getNameText().trim();
        String city = gui.getCityText().trim();
        String province = gui.getProvinceText().trim();
        String category = gui.getCategoryText().trim();

        // 2. Validate input
        if (!isInputValid(name, city, province, category)) {
            gui.setSubmissionReply("Validation Error: Please fill in Name, Province, and Category.", true);
            return;
        }

        // 3. Prepare data for storage
        String nextIdStr;
        try {
            nextIdStr = generateNextId(); // Use the correctly initialized dbManager
        } catch (RuntimeException e) { // Catch potential errors from getMaxId/formatting
            gui.showError("Error generating next ID: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        String[] newLocationData = new String[5]; // Adjust size if more columns
        newLocationData[0] = nextIdStr;
        newLocationData[1] = name;
        newLocationData[2] = city;
        newLocationData[3] = province;
        newLocationData[4] = category;

        // 4. Attempt to add data to the CSV file (using the member dbManager)
        try {
            this.databaseManager.addRecord(newLocationData);
            // If addRecord succeeds, proceed to image saving
        } catch (IOException e) {
            gui.showError("Error saving location data: " + e.getMessage());
            e.printStackTrace();
            // Don't proceed to image saving if data saving failed
            return; // Stop the submission process
        } catch (RuntimeException e) { // Catch other potential errors from addRecord
            gui.showError("An unexpected error occurred saving data: " + e.getMessage());
            e.printStackTrace();
            return;
        }


        // 5. Attempt to save the image (if selected) to the *writable* image directory
        boolean imageSaveSuccess = true; // Assume success if no image selected
        if (selectedImage != null) {
            imageSaveSuccess = saveImageToWritableLocation(selectedImage, nextIdStr);
            if (!imageSaveSuccess) {
                // Warn user, data is already saved. Cannot easily roll back CSV add.
                gui.setSubmissionReply("Warning: Location data saved, but failed to save image file.", true);
                // Don't clear form, allow user to retry or cancel maybe?
            }
        }

        // 6. Final success handling (if data saved and image save was successful or not needed)
        if (imageSaveSuccess) {
            gui.setSubmissionReply("Success: Location added!", false);
            gui.clearForm(); // Clear the form on full success
            this.selectedImage = null; // Reset selected image state
        }
    }

    /**
     * Handles the action when the 'Cancel' button is clicked.
     */
    public void handleCancelAction () {
        new HomePage(username); // Navigate back
        gui.dispose(); // Close the AddForm window
    }


    // --- Helper Methods ---

    /**
     * Generates the next sequential ID based on the current max ID in the database.
     *
     * @return The formatted ID string (e.g., "00015").
     * @throws RuntimeException if max ID cannot be determined.
     */
    private String generateNextId () {
        // Use the instance variable databaseManager initialized with the correct path
        return this.databaseManager.getNextID();
    }

    /**
     * Validates the core required input fields.
     */
    public boolean isInputValid (String name, String city, String province, String category) {
        return name != null && !name.isBlank() &&
                province != null && !province.isBlank() &&
                category != null && !category.isBlank();
    }


    /**
     * Copies the selected image file to the application's writable image folder,
     * renaming it based on the location's ID.
     *
     * @param sourceImageFile The image file selected by the user.
     * @param locationId      The ID assigned to the new location (used for filename).
     * @return true if the image was copied successfully, false otherwise.
     */
    private boolean saveImageToWritableLocation (File sourceImageFile, String locationId) { // Renamed for clarity
        try {
            // Destination is the writableImageDirectory determined in constructor
            if (!Files.exists(writableImageDirectory)) {
                Files.createDirectories(writableImageDirectory); // Ensure it exists
                System.out.println("Re-created missing writable image directory: " + writableImageDirectory);
            }

            // Determine the file extension
            String extension = FilenameUtils.getExtension(sourceImageFile.getName());
            if (extension == null || extension.isEmpty()) {
                System.err.println("Warning: Selected image has no extension. Defaulting to .jpg");
                extension = "jpg"; // Default extension or handle differently
            }

            // Create the destination filename (e.g., "00015.png")
            String destinationFilename = locationId + "." + extension.toLowerCase();
            Path destinationPath = writableImageDirectory.resolve(destinationFilename);

            // Copy the file, replacing if it somehow already exists
            Files.copy(sourceImageFile.toPath(), destinationPath,
                    StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Image successfully saved to writable location: " + destinationPath);
            return true;

        } catch (IOException e) {
            System.err.println("Error saving image file to " + writableImageDirectory + ": " + e.getMessage());
            e.printStackTrace();
            gui.showError("Could not save image: " + e.getMessage()); // Show error to user
            return false;
        } catch (Exception e) { // Catch unexpected errors
            System.err.println("Unexpected error saving image: " + e.getMessage());
            e.printStackTrace();
            gui.showError("Unexpected error saving image.");
            return false;
        }
    }

    // Main method for testing (optional)
    public static void main (String[] args) {
        // Ensure Look and Feel is set before creating GUI components
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Couldn't set system look and feel.");
        }

        // Run the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                new AddFormLogic("TestUser");
            } catch (Exception e) {
                // Catch runtime exceptions from constructor if initialization fails severely
                System.err.println("Failed to launch AddFormLogic: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Application failed to start.\nCould not initialize form resources.",
                        "Fatal Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}