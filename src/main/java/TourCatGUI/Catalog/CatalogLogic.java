package TourCatGUI.Catalog;

import TourCatGUI.HomePage;
import TourCatSystem.DatabaseManager;
// Assuming FileManager might still be used for *finding* the writable path, or replaced by a new manager
import TourCatSystem.Filter;
import TourCatSystem.LocationReader;
import com.opencsv.exceptions.CsvException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.TableView;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*; // Import NIO for file operations
import java.util.ArrayList;
import java.util.List; // Use List interface

public class CatalogLogic {

    public CatalogView gui; // Reference to the GUI
    private String username;
    private File writableDatabaseFile; // Path to the database file the user can modify
    private DefaultTableModel tableModel;
    private FuzzyFinder fuzzyFinder;
    private Filter filter; // Reusable filter object

    // Constants for resource paths inside the JAR
    private static final String INTERNAL_DB_PATH = "/database.csv";
    private static final String IMAGE_RESOURCE_PATH_PREFIX = "/image/"; // Leading and trailing slash

    // Name for the external database file
    private static final String WRITABLE_DB_FILENAME = "userdata_database.csv";

    // Filter state
    private String selectedProvince = null;
    private String selectedType = null;

    public CatalogLogic (String username, File writableDatabaseFile) {
        new CatalogLogic(username, writableDatabaseFile, false);
    }

    public CatalogLogic (String username, File writableDatabaseFile, boolean initGUI) {
        this.username = username;

        try {
            // 1. Determine and prepare the writable database file location
            this.writableDatabaseFile = writableDatabaseFile;

            // 2. Initialize Filter and DatabaseManager (using the writable file)
            // Assuming Filter is updated to work with the provided File path
            this.filter = new Filter(writableDatabaseFile);

            // 3. Load initial data from the *writable* database
            loadInitialTableData(); // Uses writableDatabaseFile internally

            // 4. Create the GUI, passing the model and this logic instance
            this.gui = new CatalogView(username, this, tableModel);

            // 5. Initialize components requiring GUI elements (like FuzzyFinder)
            this.fuzzyFinder = new FuzzyFinder(gui.getTable());

            // 6. Make the GUI visible
            this.gui.setVisible(true);

        } catch (IOException e) {
            // Handle critical initialization errors
            System.err.println("FATAL: Could not initialize database. " + e.getMessage());
            e.printStackTrace();
            // Show error to user and potentially exit or disable functionality
            JOptionPane.showMessageDialog(null,
                    "Error initializing database.\n" + e.getMessage() + "\nPlease check file permissions or contact support.",
                    "Database Initialization Error", JOptionPane.ERROR_MESSAGE);
            // Optionally, create an empty GUI or dispose it:
            // if (gui != null) gui.dispose();
            // Or maybe just disable features that need the DB
        }
    }

    /**
     * Ensures the writable database file exists, copying the default from resources if needed.
     *
     * @return The File object pointing to the writable database.
     * @throws IOException        If file operations fail.
     * @throws URISyntaxException If finding the app's running location fails.
     */
    private File initializeWritableDatabase () throws IOException, URISyntaxException {
        // Determine directory where the app is running (or user home dir)
        Path applicationDirectory = getApplicationDirectory(); // Use helper method
        Path externalDbPath = applicationDirectory.resolve(WRITABLE_DB_FILENAME);
        File externalDbFile = externalDbPath.toFile();

        // If the writable file doesn't exist, copy it from the JAR resources
        if (!externalDbFile.exists()) {
            System.out.println("Writable database not found at " + externalDbPath + ". Copying default...");
            URL internalDbUrl = getClass().getResource(INTERNAL_DB_PATH);
            if (internalDbUrl == null) {
                throw new IOException("Could not find internal resource: " + INTERNAL_DB_PATH);
            }

            try (InputStream internalStream = getClass().getResourceAsStream(INTERNAL_DB_PATH)) {
                if (internalStream == null) { // Double check stream could be opened
                    throw new IOException("Could not open internal resource stream: " + INTERNAL_DB_PATH);
                }
                // Ensure parent directory exists
                Files.createDirectories(externalDbPath.getParent());
                // Copy the file
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
     * Falls back to user home directory if running location is problematic (e.g., inside JAR structure).
     *
     * @return Path to the application's directory or user home.
     * @throws URISyntaxException
     */
    private Path getApplicationDirectory () throws URISyntaxException {
        try {
            // Get the path of the JAR file itself
            Path jarPath = Paths.get(CatalogLogic.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // Get the directory containing the JAR
            if (Files.isDirectory(jarPath)) {
                return jarPath; // Running from classes dir (IDE)
            }
            return jarPath.getParent(); // Running from JAR
        } catch (Exception e) { // Catch broader exceptions during path finding
            System.err.println("Warning: Could not determine application directory reliably. Falling back to user home. Error: " + e.getMessage());
            // Fallback to user home directory
            String userHome = System.getProperty("user.home");
            Path userHomePath = Paths.get(userHome, "TourCatData"); // Subfolder in user home
            try {
                Files.createDirectories(userHomePath); // Ensure the fallback directory exists
            } catch (IOException ioException) {
                System.err.println("Error creating fallback directory in user home: " + ioException.getMessage());
                // As a last resort, use current working directory, though less reliable
                return Paths.get("").toAbsolutePath();
            }
            return userHomePath;
        }
    }


    // --- Data Loading and Management ---

    /**
     * Loads data from the *writable* database file into the table model.
     */
    private void loadInitialTableData () throws IOException { // Propagate potential IO errors
        // Assuming LocationReader is updated to read from a File path correctly
        LocationReader reader = new LocationReader(writableDatabaseFile);
        this.tableModel = reader.getTableModel();
    }

    // Called by GUI after JTable is created
    public void hideIdColumn (TableColumnModel columnModel) {
        // Assuming LocationReader provides a static method for this
        LocationReader.hideColumns(columnModel, new int[]{0}); // Assuming column 0 is ID
    }

    /**
     * Updates the table model with the given list of CSV data lines.
     *
     * @param results List of strings, each representing a row from the CSV.
     */
    private void updateTableModel (List<String> results) { // Use List interface
        // Clear existing data (important!)
        tableModel.setRowCount(0);

        System.out.println(tableModel.getColumnCount());

        if (results != null) {
            for (String resultLine : results) {
                if (resultLine != null && !resultLine.trim().isEmpty()) {
                    // Use a more robust CSV parser if possible, but stick to split for now
                    String[] rowData = resultLine.split(","); // Potential issue with commas in fields
                    // Basic validation: Ensure enough columns exist
                    if (rowData.length >= tableModel.getColumnCount()) {
                        tableModel.addRow(rowData);
                    } else {
                        System.err.println("Skipping malformed row: " + resultLine);
                        // Handle potentially malformed rows (e.g., pad, log, ignore)
                    }
                }
            }
        }
        // No need to call fireTableDataChanged if using addRow/setRowCount on DefaultTableModel
    }

    /**
     * Reads all data lines (excluding header) from the *writable* database file.
     *
     * @return A List of strings, each representing a data row.
     */
    public List<String> readAllDataFromWritableFile () { // Renamed for clarity
        ArrayList<String> allResults = new ArrayList<>();
        // Use the writableDatabaseFile instance variable
        try (BufferedReader br = new BufferedReader(new FileReader(writableDatabaseFile))) {
            String line;
            boolean isFirstLine = true; // Assuming header row
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip the header line
                }
                if (!line.trim().isEmpty()) { // Avoid adding blank lines
                    allResults.addAll(List.of(line.split(",")));
                }
            }
        } catch (IOException ex) {
            // Show error to the user via the GUI if available
            if (gui != null) {
                gui.showError("Error reading database file: " + ex.getMessage());
            } else {
                System.err.println("Error reading database file: " + ex.getMessage());
            }
            ex.printStackTrace(); // Log for debugging
        }
        return allResults;
    }


    // --- Action Handlers (Called by GUI listeners) ---

    public void handleSearch (String searchText) {
        if (searchText != null && !searchText.isEmpty() && !searchText.equals("Search here:")) {
            fuzzyFinder.performFuzzySearch(searchText);
        } else {
            // If search text is empty/placeholder, reset filtering/searching
            handleFilterAction(); // Re-apply filters or show all if no filters active
            // Optionally clear the sorter filter directly:
            // fuzzyFinder.clearFilter();
        }
    }

    public void handleReturnAction () {
        // Consider passing the username back correctly
        new HomePage(username); // Assuming HomePage constructor handles username
        gui.dispose();
    }

    public void handleViewAction (String id, String name, String city, String province, String category) {

        URL imageURL = null;
        File externalImageFile = null;
        String[] extensions = {".png", ".jpg", ".jpeg", ".gif"}; // Common extensions


// --- Step 1: Check External Writable Location First ---
        try {
            // Need the path to the writable image directory (AddFormLogic has it, CatalogLogic needs it too)
            // Solution: Replicate getApplicationDirectory() logic here or pass it during initialization
            Path appDataDirectory = getApplicationDirectory(); // Assuming this method exists/is accessible
            Path writableImageDirectory = appDataDirectory.resolve("images"); // Or use WRITABLE_IMAGE_DIRNAME constant

            for (String ext : extensions) {
                Path potentialExternalPath = writableImageDirectory.resolve(id + ext);
                if (Files.exists(potentialExternalPath)) {
                    externalImageFile = potentialExternalPath.toFile();
                    System.out.println("Found external image file: " + externalImageFile.getAbsolutePath());
                    break; // Found it
                }
            }
        } catch (Exception e) { // Catch errors during external path resolution/check
            System.err.println("Error checking for external image file for ID " + id + ": " + e.getMessage());
            // Continue to check internal resources
        }


// --- Step 2: If not found externally, check Internal JAR Resources ---
        if (externalImageFile == null) {
            for (String ext : extensions) {
                String resourcePath = IMAGE_RESOURCE_PATH_PREFIX + id + ext; // e.g., "/image/00001.png"
                imageURL = getClass().getResource(resourcePath);
                if (imageURL != null) {
                    System.out.println("Found internal image resource: " + resourcePath);
                    break; // Found one, stop looking
                }
            }
        }

// --- Step 3: Pass result to the GUI ---
        if (externalImageFile != null) {
            try {
                // Convert external File to URL for ImageIcon compatibility if needed,
                // or modify displayDetailsWindow to accept File. Using toURI().toURL() is common.
                imageURL = externalImageFile.toURI().toURL();
            } catch (MalformedURLException e) {
                System.err.println("Error converting external file path to URL: " + e.getMessage());
                imageURL = null; // Fallback
            }
        }

// --- Final check and display ---
        if (imageURL == null && externalImageFile == null) { // Double check, though imageURL might be set from file
            System.err.println("Could not find image resource or file for ID: " + id);
        }

// Pass the final imageURL (which might be from internal or external source)
        gui.displayDetailsWindow(id, name, city, province, category, imageURL);
    }

    public void handleDeleteAction () {
        int selectedRow = gui.getSelectedRow();
        if (selectedRow != -1) {
            int confirmation = JOptionPane.showConfirmDialog(
                    gui.frame,
                    "Are you sure you want to delete this location?\n" + tableModel.getValueAt(gui.getTable().convertRowIndexToModel(selectedRow), 1),
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirmation == JOptionPane.YES_OPTION) {
                int modelRow = gui.getSelectedRow();
                String selectedRowID = (String) tableModel.getValueAt(modelRow, 0);

                try {
                    // DatabaseManager needs to use the writable file
                    // It should ideally be an instance variable or re-created safely
                    DatabaseManager databaseManager = new DatabaseManager(writableDatabaseFile); // Pass the correct file
                    databaseManager.deleteById(selectedRowID);

                    // If deleteById throws no exception, assume success
                    tableModel.removeRow(modelRow); // Update the view
                    gui.showMessage("Location deleted successfully.");

                } catch (DatabaseManager.RecordNotFoundException e) {
                    gui.showError("Could not delete: Record not found (ID: " + selectedRowID + ")");
                } catch (IOException | CsvException e) {
                    gui.showError("Error deleting location from database: " + e.getMessage());
                    e.printStackTrace(); // Log for debugging
                } catch (RuntimeException e) { // Catch unexpected runtime errors
                    gui.showError("An unexpected error occurred during deletion: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            gui.showMessage("Please select a location from the table to delete.");
        }
    }

    public void handleFilterAction () {
        // Assuming Filter class reads correctly from the file path provided in its constructor
        filter.reset();

        boolean provinceSelected = selectedProvince != null;
        boolean typeSelected = selectedType != null;

        // Determine which filter method to call based on selections
        if (provinceSelected && typeSelected) {
            filter.filterBoth(selectedProvince, selectedType);
        } else if (provinceSelected) {
            filter.filterProvince(selectedProvince);
        } else if (typeSelected) {
            filter.filterType(selectedType);
        } else {
            // No filters selected, show all data from the writable file
            List<String> allData = readAllDataFromWritableFile();
            updateTableModel(allData);
            return; // Exit after showing all data
        }

        // Get results from the filter object and update the table model
        ArrayList<String> results = filter.getResults(); // Filter should hold results internally
        updateTableModel(results);

        if (results.isEmpty() && (provinceSelected || typeSelected)) { // Only show if filters were active
            gui.showMessage("No locations match the selected filters.");
        }
    }


    public void handleResetAction () {
        // 1. Clear filter state in logic
        selectedProvince = null;
        selectedType = null;

        // 2. Tell GUI to reset combo boxes
        gui.resetFilters();

        // 3. Reload all data from the *writable* file
        List<String> allResults = readAllDataFromWritableFile();

        // 4. Update the table model
        updateTableModel(allResults);

        // 5. Clear any active JTable sorting/filtering via FuzzyFinder
        if (fuzzyFinder != null) {
            fuzzyFinder.clearFilter();
        } else {
            // Fallback if FuzzyFinder wasn't initialized? Unlikely here but good practice
            gui.getTable().setRowSorter(null);
            // Recreate FuzzyFinder if needed
            this.fuzzyFinder = new FuzzyFinder(gui.getTable());
        }

        gui.showMessage("Filters reset. Showing all locations.");
    }


    // --- State Update Methods (Called by GUI listeners) ---

    public void updateSelectedProvince (String province) {
        this.selectedProvince = province;
    }

    public void updateSelectedType (String type) {
        this.selectedType = type;
    }

    public DefaultTableModel getTableModel () {
        return this.tableModel;
    }

    public Filter getFilter () {
        return this.filter;
    }
}