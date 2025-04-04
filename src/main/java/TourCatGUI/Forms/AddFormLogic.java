package TourCatGUI.Forms;

import TourCatGUI.HomePage;
import TourCatSystem.AppDataManager;
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

import static TourCatSystem.AppDataManager.*;

public class AddFormLogic {

    private final AddFormGUI gui; // Reference to the GUI
    private final String username;
    private File selectedImage = null; // Holds the currently selected image file (from file chooser)

    private final DatabaseManager databaseManager; // Instance initialized once

    public AddFormLogic (String username) {
        this.username = username;

        try {


            // Ensure the writable image directory exists
            Files.createDirectories(writableImageDirectory);
            System.out.println("Using writable image directory: " + writableImageDirectory);

            // 3. Initialize DatabaseManager *once* with the writable file path
            this.databaseManager = new DatabaseManager(writableDatabaseFile);

            // 4. Create the GUI, passing this logic instance
            this.gui = new AddFormGUI(username, this);

            // 5. Make the GUI visible
            this.gui.setVisible(true);

        } catch (IOException e) {
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
            try {
                imageSaveSuccess = AppDataManager.saveImageToWritableLocation(selectedImage, nextIdStr);
            } catch (Exception e) {
                System.err.println("Saving error: " + e.getMessage());
                throw new RuntimeException(e);
            }
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