package TourCatGUI.Forms;

import TourCatData.DatabaseManager;
import TourCatData.FileManager;
import TourCatData.LocationData;
import TourCatGUI.HomePage;
import TourCatService.LocationService;
import org.apache.commons.io.FilenameUtils; // Ensure this dependency is present

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class AddFormLogic {

    private final AddFormGUI gui; // Reference to the GUI
    private final String username;
    private final LocationService locationService;
    private File selectedImage = null; // Holds the currently selected image file

    private DatabaseManager databaseManager;

    public AddFormLogic(String username, LocationService locationService) {
        this.username = username;
        this.locationService = locationService;

        // Create the GUI, passing this logic instance
        this.gui = new AddFormGUI(username, this);

        // Make the GUI visible
        this.gui.setVisible(true);
    }

    // --- Action Handlers (Called by GUI listeners) ---

    /** Handles the action when the 'Choose Image' button is clicked. */
    public void handleUploadImageAction() {
        File file = gui.showImageFileChooser();
        if (file != null) {
            this.selectedImage = file;
            try {
                // Create a scaled ImageIcon for the preview
                ImageIcon originalIcon = new ImageIcon(selectedImage.getAbsolutePath());
                Image scaledImage = originalIcon.getImage().getScaledInstance(
                        150, 120, Image.SCALE_SMOOTH);
                ImageIcon previewIcon = new ImageIcon(scaledImage);
                gui.setImagePreview(previewIcon);
                gui.setSubmissionReply("Image selected: " + file.getName(), false);
            } catch (Exception e) {
                // Handle potential errors loading the image
                System.err.println("Error creating image preview: " + e.getMessage());
                gui.setImagePreview(null); // Clear preview on error
                gui.setSubmissionReply("Error loading image preview.", true);
                this.selectedImage = null; // Invalidate selected image if preview failed
            }
        } else {
            // User cancelled selection, optionally clear preview if needed
            // gui.setImagePreview(null);
            // this.selectedImage = null;
            gui.setSubmissionReply("Image selection cancelled.", false);
        }
    }

    /** Handles the action when the 'Submit' button is clicked. */
    public void handleSubmitAction() {
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
        String nextIdStr = generateNextId();
        String[] newLocationData = new String[5];
        newLocationData[0] = (nextIdStr);
        newLocationData[1] = (name);
        newLocationData[2] = (city); // Add city even if blank, handle in DB/display if needed
        newLocationData[3] = (province);
        newLocationData[4] = (category);


        LocationData locationData = locationService.addLocation(name, city, province, category, selectedImage);




//        // 6. Final success handling (if data and image saved)
//        if (imageSaveSuccess) {
//            gui.setSubmissionReply("Success: Location added to the database!", false);
//            gui.clearForm(); // Clear the form on full success
//            this.selectedImage = null; // Reset selected image state
//        }
    }

    /** Handles the action when the 'Cancel' button is clicked. */
    public void handleCancelAction() {
        // Assuming HomePage takes username and shows itself
        new HomePage(username, locationService);
        gui.dispose(); // Close the AddForm window
    }


    // --- Helper Methods ---

    /**
     * Generates the next sequential ID based on the current max ID in the database.
     * @return The formatted ID string (e.g., "00015").
     */
    private String generateNextId() {
        int maxId = this.databaseManager.getMaxId().getAsInt();
        int nextId = maxId + 1;
        return String.format("%05d", nextId); // Formats with leading zeros up to 5 digits
    }

    /**
     * Validates the core required input fields.
     * Basic check: Name, Province, Category must not be blank.
     * @param name Name input
     * @param city City input (optional in this validation)
     * @param province Province input
     * @param category Category input
     * @return true if the essential input is valid, false otherwise.
     */
    public boolean isInputValid(String name, String city, String province, String category) {
        // City is often optional, main requirement here is Name, Province, Category
        return name != null && !name.isBlank() &&
                province != null && !province.isBlank() &&
                category != null && !category.isBlank();
    }
    // Main method for testing this specific form (optional)
    // Usually, the application starts from a central point (like MainApplication)
    public static void main(String[] args) {

        FileManager fileManager = FileManager.getInstance(true);
        try {
            DatabaseManager databaseManager = new DatabaseManager(fileManager.getDatabaseFile());

            LocationService service = new LocationService(databaseManager, fileManager);

            // Run the GUI on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> new AddFormLogic("TestUser", service));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}