package TourCatGUI.Catalog;

import TourCatGUI.HomePage;
import TourCatSystem.DatabaseManager;
import TourCatSystem.FileManager;
import TourCatSystem.Filter;
import TourCatSystem.LocationReader;
import com.opencsv.exceptions.CsvException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CataLogic {

    private CataView gui; // Reference to the GUI
    private String username;
    private File dataBaseFile;
    private DefaultTableModel tableModel;
    private FuzzyFinder fuzzyFinder;
    private Filter filter; // Reusable filter object

    // Filter state
    private String selectedProvince = null;
    private String selectedType = null;

    public CataLogic(String username) {
        this.username = username;
        this.dataBaseFile = FileManager.getInstance().getDatabaseFile();
        this.filter = new Filter(dataBaseFile); // Initialize filter with the database file

        // 1. Load initial data
        loadInitialTableData();

        // 2. Create the GUI, passing the model and this logic instance
        this.gui = new CataView(username, this, tableModel);

        // 3. Initialize components requiring GUI elements (like FuzzyFinder)
        this.fuzzyFinder = new FuzzyFinder(gui.getTable());

        // 4. Make the GUI visible
        this.gui.setVisible(true);
    }

    // --- Data Loading and Management ---

    private void loadInitialTableData() {
        LocationReader reader = new LocationReader(dataBaseFile);
        this.tableModel = reader.getTableModel();
        // Column hiding logic can stay here or move to GUI, let's keep it near the reader
        // We need a way to access the TableColumnModel *after* the GUI/JTable is created.
        // Let's add a method for the GUI to call *after* table creation.
    }

    // Called by GUI after JTable is created
    public void hideIdColumn(TableColumnModel columnModel) {
        LocationReader.hideColumns(columnModel, new int[]{0}); // Assuming column 0 is ID
    }

    private void updateTableModel(ArrayList<String> results) {
        // Clear existing data (important!)
        tableModel.setRowCount(0);

        // Get column names (assuming they don't change)
        // String[] columnNames = ... ; // If needed, but DefaultTableModel handles this

        if (results != null) {
            for (String resultLine : results) {
                if (resultLine != null && !resultLine.trim().isEmpty()) {
                    String[] rowData = resultLine.split(","); // Assuming CSV
                    // Basic validation: Ensure enough columns exist
                    if (rowData.length >= tableModel.getColumnCount()) {
                        // If ID is hidden but present in data, adjust indices or ensure model matches data structure
                        tableModel.addRow(rowData);
                    } else {
                        System.err.println("Skipping malformed row: " + resultLine);
                        // Optionally, pad with empty strings if necessary:
                        // Object[] paddedRow = new Object[tableModel.getColumnCount()];
                        // System.arraycopy(rowData, 0, paddedRow, 0, rowData.length);
                        // java.util.Arrays.fill(paddedRow, rowData.length, paddedRow.length, "");
                        // tableModel.addRow(paddedRow);
                    }
                }
            }
        }
        // No need to call fireTableDataChanged if using addRow/setRowCount on DefaultTableModel
    }


    private ArrayList<String> readAllDataFromFile() {
        ArrayList<String> allResults = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(dataBaseFile))) {
            String line;
            boolean isFirstLine = true; // Assuming header row
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip the header line
                }
                if (!line.trim().isEmpty()) { // Avoid adding blank lines
                    allResults.add(line);
                }
            }
        } catch (IOException ex) {
            gui.showError("Error reading database file: " + ex.getMessage());
            ex.printStackTrace(); // Log for debugging
        }
        return allResults;
    }


    // --- Action Handlers (Called by GUI listeners) ---

    public void handleSearch(String searchText) {
        // Avoid searching on the placeholder text
        if (searchText != null && !searchText.equals("Search here:")) {
            fuzzyFinder.performFuzzySearch(searchText);
        } else {
            // Optional: If search text is empty or placeholder, reset to show all (or filtered) results
            handleFilterAction(); // Re-apply filters or show all if no filters active
        }
    }

    public void handleReturnAction() {
        new HomePage(username); // Assuming HomePage exists and works
        gui.dispose();
    }

    public void handleViewAction() {
        int selectedRow = gui.getSelectedRow();
        if (selectedRow != -1) {
            // Convert view index to model index in case of sorting/filtering
            int modelRow = gui.getTable().convertRowIndexToModel(selectedRow);

            // Get data using model index
            String id = (String) tableModel.getValueAt(modelRow, 0); // Assumes ID is column 0
            String name = (String) tableModel.getValueAt(modelRow, 1);
            String city = (String) tableModel.getValueAt(modelRow, 2);
            String province = (String) tableModel.getValueAt(modelRow, 3);
            String category = (String) tableModel.getValueAt(modelRow, 4);

            // Find the corresponding image file
            File imageFile = FileManager.getInstance().getImageFile(id + ".png");
            if (!imageFile.exists()) {
                imageFile = FileManager.getInstance().getImageFile(id + ".jpg");
            }
            // If neither exists, imageFile will point to the non-existent .jpg path

            // Ask GUI to display the details window
            gui.displayDetailsWindow(id, name, city, province, category, imageFile);

        } else {
            gui.showMessage("Please select a location from the table to view details.");
        }
    }

    public void handleDeleteAction() {
        int selectedRow = gui.getSelectedRow();
        if (selectedRow != -1) {
            // Show confirmation dialog
            int confirmation = JOptionPane.showConfirmDialog(
                    gui.frame, // Parent component
                    "Are you sure you want to delete this location?\n" + tableModel.getValueAt(gui.getTable().convertRowIndexToModel(selectedRow), 1), // Message showing name
                    "Confirm Deletion", // Title
                    JOptionPane.YES_NO_OPTION, // Options
                    JOptionPane.WARNING_MESSAGE // Icon
            );

            if (confirmation == JOptionPane.YES_OPTION) {
                int modelRow = gui.getTable().convertRowIndexToModel(selectedRow);
                String selectedRowID = (String) tableModel.getValueAt(modelRow, 0); // Assumes ID is column 0


                DatabaseManager databaseManager = null;
                try {
                    databaseManager = new DatabaseManager(dataBaseFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                boolean success = false;
                try {
                    databaseManager.deleteById(selectedRowID);
                } catch (IOException | DatabaseManager.RecordNotFoundException | CsvException e) {
                    throw new RuntimeException(e);
                }

                if (success) {
                    // Remove row from the model (this will update the JTable)
                    tableModel.removeRow(modelRow);
                    gui.showMessage("Location deleted successfully.");
                } else {
                    gui.showError("Failed to delete the location from the database file.");
                }
            }
        } else {
            gui.showMessage("Please select a location from the table to delete.");
        }
    }

    public void handleFilterAction() {
        filter.reset(); // Clear previous filter results within the Filter object

        boolean provinceSelected = selectedProvince != null && !selectedProvince.isEmpty();
        boolean typeSelected = selectedType != null && !selectedType.isEmpty();

        if (provinceSelected && typeSelected) {
            filter.filterBoth(selectedProvince, selectedType);
        } else if (provinceSelected) {
            filter.filterProvince(selectedProvince);
        } else if (typeSelected) {
            filter.filterType(selectedType);
        } else {
            // No filters selected, show all data
            ArrayList<String> allData = readAllDataFromFile();
            updateTableModel(allData);
            // Optionally show a message if you *require* a filter to be selected
            // gui.showMessage("Please select at least one filter option or reset filters.");
            return; // Exit after showing all data
        }

        // Get results from the filter and update the table model
        ArrayList<String> results = filter.getResults();
        updateTableModel(results);

        if(results.isEmpty()){
            gui.showMessage("No locations match the selected filters.");
        }
    }

    public void handleResetAction() {
        // 1. Clear filter state in logic
        selectedProvince = null;
        selectedType = null;

        // 2. Tell GUI to reset combo boxes and potentially search field
        gui.resetFilters();

        // 3. Reload all data from the file
        ArrayList<String> allResults = readAllDataFromFile();

        // 4. Update the table model
        updateTableModel(allResults);

        // 5. Clear any active JTable sorting/filtering (if FuzzyFinder or JTable itself adds it)
        gui.getTable().setRowSorter(null); // Remove sorter temporarily
        fuzzyFinder = new FuzzyFinder(gui.getTable()); // Recreate FuzzyFinder with the fresh table state

        gui.showMessage("Filters reset. Showing all locations.");
    }


    // --- State Update Methods (Called by GUI listeners) ---

    public void updateSelectedProvince(String province) {
        this.selectedProvince = province; // Store null if "Select Province" was chosen
    }

    public void updateSelectedType(String type) {
        this.selectedType = type; // Store null if "Select Type" was chosen
    }
}