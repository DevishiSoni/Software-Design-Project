package TourCatService;

import TourCatData.DatabaseManager;
import TourCatData.FileManager;
import TourCatData.LocationData;
import TourCatSystem.LocationReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocationService {

    private final DatabaseManager databaseManager;
    private final FileManager fileManager;

    public LocationService(DatabaseManager databaseManager, FileManager fileManager)
    {
        this.databaseManager = databaseManager;
        this.fileManager = fileManager;
    }



    /**
     * Adds a new location and optionally saves its associated image.
     * @param name Name of the location.
     * @param city City of the location.
     * @param province Province of the location.
     * @param category Category of the location.
     * @return The newly created LocationData object with its assigned ID.
     */
    public LocationData addLocation(String name, String city, String province, String category, File imageFile) {

        // 2. Generate a robust ID (UUID is better than sequential)
        // String id = String.format("%05d", databaseManager.getMaxId(fileManager.getDatabaseFile()) + 1); // Fragile
        String id = UUID.randomUUID().toString(); // More robust

        // 3. Create the Data Transfer Object
        LocationData newLocation = new LocationData(id, name, city, province, category);

        // 4. Prepare data for persistence (Convert DTO to format needed by current DatabaseManager)
        ArrayList<String> dataRow = new ArrayList<>();
        dataRow.add(newLocation.getId());
        dataRow.add(newLocation.getName());
        dataRow.add(newLocation.getCity());
        dataRow.add(newLocation.getProvince());
        dataRow.add(newLocation.getCategory());

        // 5. Persist the location data (using the injected dependency)
        File dbFile = fileManager.getDatabaseFile();


        try {
            databaseManager.addRecord(dataRow.toArray(String[]::new)); // Use static method for now
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (imageFile != null) {
            try {
                saveImageForLocation(newLocation.getId(), imageFile);
            } catch (IOException e) {
                System.err.println("Location data saved, but failed to save image: " + e.getMessage());
            }
        }

        return newLocation; // Return the created object
    }

    /**
     * Retrieves all locations.
     * @return A list of all LocationData objects.
     */
    public List<LocationData> getAllLocations(){
        // This needs improvement - DatabaseManager doesn't provide this directly.
        // The LocationReader logic should be moved to a DAO or within this service/DAO layer.
        // For now, simulate by using LocationReader temporarily (BAD PRACTICE)

        File dbFile = fileManager.getDatabaseFile();

        System.out.println("Reading CSV file from: "+  dbFile.getAbsolutePath());

        // This logic belongs in a DAO:
        LocationReader reader = new LocationReader(dbFile); // Re-reading file every time! Inefficient.
        javax.swing.table.DefaultTableModel model = reader.getTableModel();
        List<LocationData> locations = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            locations.add(new LocationData(
                    (String) model.getValueAt(i, 0), // ID
                    (String) model.getValueAt(i, 1), // Name
                    (String) model.getValueAt(i, 2), // City
                    (String) model.getValueAt(i, 3), // Province
                    (String) model.getValueAt(i, 4)  // Category
            ));
        }
        System.out.println(locations.get(0));
        System.out.println("Found " + locations.size() + " locations in the LocationService Database!");
        return locations;
    }

    /**
     * Deletes a location by its ID.
     * @param locationId The ID of the location to delete.
     */
    public void deleteLocation(String locationId)  {
        File dbFile = fileManager.getDatabaseFile();
        // DatabaseManager deletes by name currently - this needs to be changed to ID!
        // For now, we'd need to first find the name by ID (inefficient) or modify DatabaseManager.
        // Assuming DatabaseManager.deleteFromFile is modified to take ID (index 0):

        try {
            databaseManager.deleteById(locationId);
        } catch (IOException | DatabaseManager.RecordNotFoundException | CsvException e) {
            throw new RuntimeException(e);
        }


        // Attempt to delete associated images (png/jpg)
        try {
            deleteImagesForLocation(locationId);
        } catch (IOException e) {
            System.err.println("Warning: Location data deleted, but failed to delete associated image(s) for ID " + locationId + ": " + e.getMessage());
            // Don't throw exception here, primary data is gone. Just log.
        }
    }

    /**
     * Finds locations based on search criteria. (Simplified version)
     * This should ideally query the DAO layer.
     * @param query Search term (searches name, city, etc.)
     * @param province Filter by province (null or empty to ignore)
     * @param category Filter by category (null or empty to ignore)
     * @return A list of matching LocationData objects.
     */
    public List<LocationData> findLocations(String query, String province, String category) {
        // This is highly inefficient with CSV. A real DAO/database is needed.
        // Re-implementing search/filter logic here based on getAllLocations:
        List<LocationData> allLocations = getAllLocations();
        List<LocationData> filteredLocations = new ArrayList<>();

        for (LocationData loc : allLocations) {
            boolean match = true;

            // Filter by province
            if (province != null && !province.isBlank() && !province.equalsIgnoreCase(loc.getProvince())) {
                match = false;
            }
            // Filter by category
            if (match && category != null && !category.isBlank() && !category.equalsIgnoreCase(loc.getCategory())) {
                match = false;
            }
            // Filter by query (simple contains check across fields)
            if (match && query != null && !query.isBlank()) {
                String lowerQuery = query.toLowerCase();
                if (!(loc.getName().toLowerCase().contains(lowerQuery) ||
                        (loc.getCity() != null && loc.getCity().toLowerCase().contains(lowerQuery)) || // Handle potential null city
                        loc.getProvince().toLowerCase().contains(lowerQuery) ||
                        loc.getCategory().toLowerCase().contains(lowerQuery))) {
                    match = false;
                }
            }

            if (match) {
                filteredLocations.add(loc);
            }
        }
        return filteredLocations;
    }

    /**
     * Retrieves a single location by its ID.
     * @param locationId The ID of the location.
     * @return The LocationData object.
     */
    public LocationData getLocationById(String locationId) {
        // Inefficient: Iterate through all locations to find by ID
        List<LocationData> allLocations = getAllLocations();
        for (LocationData loc : allLocations) {
            if (loc.getId().equals(locationId)) {
                return loc;
            }
        }

        return null;
    }


    // --- Helper methods for image handling ---

    private void saveImageForLocation(String locationId, File sourceImageFile) throws IOException {
        File destinationFolder = fileManager.getResourceFile("image"); // Use FileManager
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs(); // Ensure image directory exists
        }

        String extension = FilenameUtils.getExtension(sourceImageFile.getName());
        if (extension == null || extension.isEmpty()) {
            extension = "jpg"; // Default extension if none found
            System.err.println("Warning: Image file has no extension, assuming ." + extension);
        }

        // Use location ID as base filename
        String destinationFilename = locationId + "." + extension;
        File destinationFile = new File(destinationFolder, destinationFilename);

        // Copy the file
        Files.copy(sourceImageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Image saved to: " + destinationFile.getAbsolutePath());
    }

    private void deleteImagesForLocation(String locationId) throws IOException {
        File imageFolder = fileManager.getResourceFile("image");
        if (!imageFolder.exists() || !imageFolder.isDirectory()) {
            return; // No image folder, nothing to delete
        }

        // Attempt to delete common image types for this ID
        String[] extensions = {"png", "jpg", "jpeg", "gif"};
        boolean deleted = false;
        for (String ext : extensions) {
            File imageFile = new File(imageFolder, locationId + "." + ext);
            if (imageFile.exists()) {
                if (Files.deleteIfExists(imageFile.toPath())) {
                    System.out.println("Deleted image: " + imageFile.getName());
                    deleted = true;
                } else {
                    System.err.println("Failed to delete image: " + imageFile.getName());
                }
            }
        }
        //if (!deleted) { System.out.println("No image files found to delete for ID: " + locationId); }
    }
}