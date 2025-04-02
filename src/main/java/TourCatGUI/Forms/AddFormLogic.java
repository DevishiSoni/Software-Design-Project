package TourCatGUI.Forms;

import TourCatGUI.HomePage;
import TourCatSystem.DatabaseManager;
import TourCatSystem.FileManager;
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
    private final File databaseFile;
    private File selectedImage = null; // Holds the currently selected image file

    private DatabaseManager databaseManager;

    public AddFormLogic(String username) {
        this.username = username;
        this.databaseFile = FileManager.getInstance().getDatabaseFile();

        try {
            this.databaseManager = new DatabaseManager(databaseFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        // Add other fields if your CSV structure requires them


        // 4. Attempt to add data to the file
        try {
            DatabaseManager databaseManager = new DatabaseManager(databaseFile);
            databaseManager.addRecord(newLocationData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // 5. Attempt to save the image (if selected)
        boolean imageSaveSuccess = true; // Assume success if no image selected
        if (selectedImage != null) {
            imageSaveSuccess = saveImageResource(selectedImage, nextIdStr);
            if (!imageSaveSuccess) {
                // Warn user, but data might be saved already. Consider rollback?
                gui.setSubmissionReply("Warning: Location data saved, but failed to save image file.", true);
                // Don't clear the form yet, maybe user wants to retry image? Or clear partially?
            }
        }

        // 6. Final success handling (if data and image saved)
        if (imageSaveSuccess) {
            gui.setSubmissionReply("Success: Location added to the database!", false);
            gui.clearForm(); // Clear the form on full success
            this.selectedImage = null; // Reset selected image state
        }
    }

    /** Handles the action when the 'Cancel' button is clicked. */
    public void handleCancelAction() {
        // Assuming HomePage takes username and shows itself
        new HomePage(username);
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


    /**
     * Copies the selected image file to the application's image resource folder,
     * renaming it based on the location's ID.
     *
     * @param sourceImageFile The image file selected by the user.
     * @param locationId The ID assigned to the new location (used for filename).
     * @return true if the image was copied successfully, false otherwise.
     */
    private boolean saveImageResource(File sourceImageFile, String locationId) {
        try {
            // Get the designated image resource folder
            File destinationFolder = FileManager.getInstance().getImageResourceFolder(); // Use specific method

            if (!destinationFolder.exists()) {
                if (!destinationFolder.mkdirs()) {
                    System.err.println("Error: Could not create image resource directory: " + destinationFolder.getAbsolutePath());
                    return false;
                }
            }

            // Determine the file extension
            String extension = FilenameUtils.getExtension(sourceImageFile.getName());
            if (extension == null || extension.isEmpty()) {
                // Handle case with no extension? Maybe default to .jpg or skip?
                System.err.println("Warning: Selected image has no extension. Cannot determine file type.");
                extension = "jpg"; // Or return false / throw exception
            }

            // Create the destination filename (e.g., "00015.png")
            String destinationFilename = locationId + "." + extension.toLowerCase();
            File destinationFile = new File(destinationFolder, destinationFilename);

            // Copy the file, replacing if it somehow already exists
            Files.copy(sourceImageFile.toPath(), destinationFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Image successfully saved to: " + destinationFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("Error saving image file: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return false;
        } catch (Exception e) { // Catch unexpected errors
            System.err.println("Unexpected error saving image: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Main method for testing this specific form (optional)
    // Usually, the application starts from a central point (like MainApplication)
    public static void main(String[] args) {

        // Run the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new AddFormLogic("TestUser"));
    }
}