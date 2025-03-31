package TourCatGUI.Catalog;

import TourCatData.LocationData;        // Use the DTO
import TourCatData.FileManager;         // Needed for view action image lookup
import TourCatGUI.HomePage;
import TourCatService.LocationService;
// Removed LocationReader, Filter, DatabaseManager, CsvException, File, FileReader, BufferedReader imports

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File; // Keep for image file handling in view action
import java.util.ArrayList; // Keep for empty list creation potentially
import java.util.List;     // Use List interface

public class CatalogModel {

    private final CatalogView gui; // Reference to the GUI
    private final String username;
    // private File dataBaseFile; // REMOVED - Use service
    private DefaultTableModel tableModel;
    private FuzzyFinder fuzzyFinder; // Keep for UI-level text filtering
    // private Filter filter; // REMOVED - Use service directly

    private final LocationService locationService; // Make final

    // Filter state - Keep track of selected dropdown values
    private String selectedProvince = null;
    private String selectedType = null;

    public CatalogModel(String username, LocationService locationService) {
        if (locationService == null) {
            throw new IllegalArgumentException("LocationService cannot be null.");
        }
        this.username = username;
        this.locationService = locationService;
        // this.filter = new Filter(locationService); // REMOVED

        // 1. Prepare the table model structure (columns)
        prepareTableModel(); // NEW Helper method

        // 2. Load initial data using the service
        loadInitialTableData(); // Will now use service and update the prepared model

        // 3. Create the GUI, passing the *populated* model and this logic instance
        this.gui = new CatalogView(username, this, tableModel);

        // 4. Initialize components requiring GUI elements (like FuzzyFinder)
        // FuzzyFinder needs the JTable from the GUI
        this.fuzzyFinder = new FuzzyFinder(gui.getTable());

        // 5. Tell the GUI to hide the ID column (GUI's responsibility)
        this.gui.hideIdColumn(); // Assuming CataView has this method now

        // 6. Make the GUI visible
        this.gui.setVisible(true);
    }

    // --- Data Loading and Management ---

    /** Prepares the structure (columns) of the table model. */
    private void prepareTableModel() {
        // Define column names explicitly - safer than relying on CSV header order always
        String[] columnNames = {"ID", "Name", "City", "Province", "Category"};
        // Create an empty model with 0 rows but defined columns
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            // Optional: Make cells non-editable if desired
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }


    /** Loads initial data from the LocationService and updates the table model. */
    private void loadInitialTableData() {
        try {
            // Fetch all locations from the service
            List<LocationData> allLocations = locationService.getAllLocations();
            // Update the table model with this data
            updateTableModel(allLocations);
        } catch (Exception e) {
            // Handle exceptions during data loading (e.g., service throws error)
            System.err.println("Error loading initial location data: " + e.getMessage());
            e.printStackTrace();
            // Show error to the user via the GUI (if GUI is ready)
            if (gui != null) {
                gui.showError("Failed to load initial location data: " + e.getMessage());
            } else {
                // If GUI not ready, maybe throw runtime exception or log prominently
                JOptionPane.showMessageDialog(null, "Critical Error: Failed to load initial data.", "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
            // Ensure table model is empty on error
            if (tableModel != null) {
                tableModel.setRowCount(0);
            } else {
                prepareTableModel(); // Ensure model exists even if loading failed
            }
        }
    }


    // Called by GUI after JTable is created - REMOVED from Logic
    // public void hideIdColumn(TableColumnModel columnModel) {
    //     // Logic moved to CataView
    // }

    /**
     * Updates the table model with the provided list of LocationData.
     * Clears existing rows before adding new ones.
     * @param locations The list of LocationData objects to display.
     */
    private void updateTableModel(List<LocationData> locations) {
        // Clear existing data
        tableModel.setRowCount(0);

        if (locations != null) {
            for (LocationData loc : locations) {
                // Add data from the LocationData object
                tableModel.addRow(new Object[]{
                        loc.getId(),
                        loc.getName() != null ? loc.getName() : "", // Handle potential nulls
                        loc.getCity() != null ? loc.getCity() : "",
                        loc.getProvince() != null ? loc.getProvince() : "",
                        loc.getCategory() != null ? loc.getCategory() : ""
                });
            }
        }
        // Table model updates automatically notify the JTable/View via listeners
        // Re-apply text filter after model update
        applyTextFilter();
    }

    // Removed readAllDataFromFile - use service instead


    // --- Action Handlers (Called by GUI listeners) ---

    public void handleSearch(String searchText) {
        applyTextFilter(); // Use helper to apply fuzzy filter
    }

    /** Helper method to apply the text filter using FuzzyFinder */
    private void applyTextFilter() {
        if (fuzzyFinder != null) {
            String searchText = gui.getSearchText(); // Get current search text from GUI
            // Avoid filtering on placeholder text
            if (searchText != null && !searchText.equals("Search here:") && !searchText.isBlank()) {
                fuzzyFinder.performFuzzySearch(searchText);
            } else {
                fuzzyFinder.clearFilter(); // Clear text filter if search box is empty/placeholder
            }
        }
    }


    public void handleReturnAction() {
        // Assuming HomePage takes username and service, and shows itself
        new HomePage(username, locationService).setVisible(true); // Ensure HomePage becomes visible
        gui.dispose(); // Close the catalog window
    }

    public void handleViewAction() {
        int selectedRow = gui.getSelectedRow(); // Get VIEW row index
        if (selectedRow != -1) {
            // Convert view index to model index in case of sorting/filtering
            int modelRow = gui.getTable().convertRowIndexToModel(selectedRow);

            // Get data using model index from the tableModel
            // Safely get values, converting to String
            String id = String.valueOf(tableModel.getValueAt(modelRow, 0)); // ID
            String name = String.valueOf(tableModel.getValueAt(modelRow, 1)); // Name
            String city = String.valueOf(tableModel.getValueAt(modelRow, 2)); // City
            String province = String.valueOf(tableModel.getValueAt(modelRow, 3)); // Province
            String category = String.valueOf(tableModel.getValueAt(modelRow, 4)); // Category

            // Find the corresponding image file using FileManager
            // This logic is okay here as it's about finding a view resource
            File imageFile = FileManager.getInstance().getImageFile(id + ".png");
            if (!imageFile.exists()) {
                imageFile = FileManager.getInstance().getImageFile(id + ".jpg"); // Check for .jpg
            }
            // If neither exists, imageFile will point to the non-existent .jpg path

            // Ask GUI to display the details window
            gui.displayDetailsWindow(id, name, city, province, category, imageFile);

        } else {
            gui.showMessage("Please select a location from the table to view details.");
        }
    }

    public void handleDeleteAction() {
        int selectedViewRow = gui.getSelectedRow();
        if (selectedViewRow != -1) {
            int modelRow = gui.getTable().convertRowIndexToModel(selectedViewRow);
            String locationId = String.valueOf(tableModel.getValueAt(modelRow, 0)); // Get ID
            String locationName = String.valueOf(tableModel.getValueAt(modelRow, 1)); // Get Name for message

            // Show confirmation dialog
            int confirmation = JOptionPane.showConfirmDialog(
                    gui.frame, // Parent component
                    "Are you sure you want to delete '" + locationName + "'?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirmation == JOptionPane.YES_OPTION) {
                try {
                    // Use the LocationService to delete
                    locationService.deleteLocation(locationId);

                    // If successful, remove the row from the *model* for immediate UI update
                    tableModel.removeRow(modelRow);
                    gui.showMessage("Location '" + locationName + "' deleted successfully.");

                } catch (Exception e) { // Catch potential exceptions from service/db layer
                    System.err.println("Error deleting location with ID " + locationId + ": " + e.getMessage());
                    e.printStackTrace();
                    gui.showError("Failed to delete location '" + locationName + "'.\nReason: " + e.getMessage());
                }
            }
        } else {
            gui.showMessage("Please select a location from the table to delete.");
        }
    }

    /** Handles the filtering when dropdown selections change or filter button is pressed */
    public void handleFilterAction() {
        // filter.reset(); // REMOVED Filter object usage

        // Fetch data from the service based on current dropdown selections
        try {
            List<LocationData> filteredResults = locationService.findLocations(
                    null, // Pass null for text query - FuzzyFinder handles text separately
                    selectedProvince,
                    selectedType
            );

            // Update the table model with the filtered results
            updateTableModel(filteredResults); // This will also re-apply text filter

            // Show message if no results match dropdown filters
            if (filteredResults.isEmpty()) {
                gui.showMessage("No locations match the selected filters.");
            }
        } catch (Exception e) {
            System.err.println("Error applying filters: " + e.getMessage());
            e.printStackTrace();
            gui.showError("Failed to apply filters: " + e.getMessage());
            // Optionally load all data on filter error? Or just show empty table?
            updateTableModel(new ArrayList<>()); // Show empty table on error
        }
    }


    public void handleResetAction() {
        // 1. Clear filter state in logic
        selectedProvince = null;
        selectedType = null;

        // 2. Tell GUI to reset combo boxes and search field
        gui.resetFilters(); // Resets dropdowns
        gui.setSearchText(""); // Clears search field text visually

        // 3. Reload all data using the service
        loadInitialTableData(); // This fetches all and calls updateTableModel

        // 4. updateTableModel already called by loadInitialTableData,
        //    which includes clearing the text filter via applyTextFilter().

        gui.showMessage("Filters reset. Showing all locations.");
    }


    // --- State Update Methods (Called by GUI listeners for dropdowns) ---

    public void updateSelectedProvince(String province) {
        // Store null if the "Select/All" option is chosen
        this.selectedProvince = (province == null || province.equals("Select Province") || province.equals("All Provinces")) ? null : province;
        // Automatically apply filters when a dropdown changes
        handleFilterAction();
    }

    public void updateSelectedType(String type) {
        // Store null if the "Select/All" option is chosen
        this.selectedType = (type == null || type.equals("Select Type") || type.equals("All Types")) ? null : type;
        // Automatically apply filters when a dropdown changes
        handleFilterAction();
    }
}