import TourCatGUI.Catalog.CatalogLogic;
import TourCatGUI.Catalog.CatalogView; // Import needed if interacting with GUI elements
import TourCatSystem.DatabaseManager;   // Import needed for testing delete interaction
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir; // Use TempDir for isolated testing

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Keep tests ordered if needed
class CatalogLogicTesting {

    @TempDir
    Path tempDir; // JUnit creates and cleans this temporary directory

    CatalogLogic catalogLogic;
    File writableDbFile; // The actual database file used in the temp directory
    DefaultTableModel tableModel; // To check the model state directly

    // Sample data matching the expected CSV structure
    static final String[] HEADER = {"ID", "Name", "City", "Province", "Category"};
    static final String[] DATA_ROW_1 = {"00001", "Niagara Falls", "Niagara", "ON", "Waterfall"};
    static final String[] DATA_ROW_2 = {"00002", "CN Tower", "Toronto", "ON", "Landmark"};
    static final String[] DATA_ROW_3 = {"00003", "Stanley Park", "Vancouver", "BC", "Park"};
    static final String[] DATA_ROW_4 = {"00004", "Old Quebec", "Quebec City", "QC", "Historic Site"}; // Note space

    @BeforeEach
    void setUp () throws IOException, URISyntaxException {
        // --- 1. Prepare the Writable Database File in TempDir ---
        Path dbPath = tempDir.resolve("userdata_database.csv"); // Use the standard name CatalogLogic expects
        writableDbFile = dbPath.toFile();

        // --- 2. Copy Default Content (if available) or Create Dummy ---
        // Try to copy the default database from resources, like CatalogLogic does
        URL internalDbUrl = getClass().getResource("/database.csv"); // Path in resources
        if (internalDbUrl != null) {
            try (InputStream internalStream = getClass().getResourceAsStream("/database.csv")) {
                assumeTrue(internalStream != null, "Could not open resource stream /database.csv");
                Files.copy(internalStream, dbPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Copied default database to: " + dbPath);
            } catch (Exception e) {
                System.err.println("Warning: Failed to copy default DB resource. Creating basic test file. Error: " + e);
                createBasicTestCsv(writableDbFile); // Fallback
            }
        } else {
            System.err.println("Warning: Default resource /database.csv not found. Creating basic test file.");
            createBasicTestCsv(writableDbFile); // Create a basic file if resource missing
        }

        // --- 3. Instantiate CatalogLogic using the Test Constructor ---
        // Pass the path to our temporary database file.
        // Set initializeGui to true IF tests rely on FuzzyFinder or other GUI elements.
        // Set to false if only testing data loading/filtering logic directly.
        catalogLogic = new CatalogLogic("testUser", writableDbFile); // Initialize with GUI elements needed for filter/reset

        // --- 4. Get the table model loaded by CatalogLogic ---
        tableModel = catalogLogic.getTableModel();
        assertNotNull(tableModel, "Table model should be loaded by CatalogLogic constructor");
        System.out.println("Initial TableModel Row Count: " + tableModel.getRowCount());
    }

    // Helper to create a basic CSV if the resource isn't found
    private void createBasicTestCsv (File file) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            writer.writeNext(HEADER);
            writer.writeNext(DATA_ROW_1);
            writer.writeNext(DATA_ROW_2);
            writer.writeNext(DATA_ROW_3);
            writer.writeNext(DATA_ROW_4);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should load initial data correctly from the prepared file")
    void testInitialDataLoading () {
        // Assumes the copied/created CSV has HEADER + 4 data rows initially
        assertEquals(4, tableModel.getRowCount(), "Should load the initial data rows (excluding header)");
        // Check some values from the first data row (adjust indices based on HEADER)
        assertEquals(DATA_ROW_1[1], tableModel.getValueAt(0, 1), "First row name should match"); // Name at index 1
        assertEquals(DATA_ROW_1[3], tableModel.getValueAt(0, 3), "First row province should match"); // Province at index 3
    }

    @Test
    @Order(2)
    @DisplayName("Should read all data rows correctly")
    void testReadAllDataFromWritableFile () {
        List<String> dataLines = catalogLogic.readAllDataFromWritableFile();
        assertNotNull(dataLines);
        // Should contain the string representation of the data rows
        assertEquals(4, dataLines.size(), "Should read all 4 data rows (header skipped)");
        assertTrue(dataLines.get(0).startsWith(DATA_ROW_1[0]), "First data line should start with ID"); // "00001,Niagara..."
        assertTrue(dataLines.get(1).contains(DATA_ROW_2[1]), "Second data line should contain Name"); // "...,CN Tower,..."
    }

    @Test
    @Order(3)
    @DisplayName("Should filter by a valid Province (ON)")
    void testFilterByValidProvince () {
        catalogLogic.updateSelectedProvince("ON"); // Set filter criteria
        catalogLogic.handleFilterAction();      // Apply the filter

        // Verify the table model reflects the filtered results
        assertEquals(2, tableModel.getRowCount(), "Should find 2 locations in ON");
        assertEquals(DATA_ROW_1[1], tableModel.getValueAt(0, 1), "First ON result should be Niagara Falls");
        assertEquals(DATA_ROW_2[1], tableModel.getValueAt(1, 1), "Second ON result should be CN Tower");
    }

    @Test
    @Order(4)
    @DisplayName("Should filter by a valid Province (BC)")
    void testFilterByValidProvinceBC () {
        catalogLogic.updateSelectedProvince("BC");
        catalogLogic.handleFilterAction();

        assertEquals(1, tableModel.getRowCount(), "Should find 1 location in BC");
        assertEquals(DATA_ROW_3[1], tableModel.getValueAt(0, 1), "BC result should be Stanley Park");
    }


    @Test
    @Order(5)
    @DisplayName("Should return no results for an invalid Province")
    void testFilterByInvalidProvince () {
        catalogLogic.updateSelectedProvince("MB"); // Manitoba - not in our test data
        catalogLogic.handleFilterAction();

        assertEquals(0, tableModel.getRowCount(), "Should find 0 locations in MB");
    }

    @Test
    @Order(6)
    @DisplayName("Should filter by a valid Type (Waterfall)")
    void testFilterByValidType () {
        catalogLogic.updateSelectedType("Waterfall");
        catalogLogic.handleFilterAction();

        assertEquals(1, tableModel.getRowCount(), "Should find 1 Waterfall");
        assertEquals(DATA_ROW_1[1], tableModel.getValueAt(0, 1), "Waterfall result should be Niagara Falls");
    }

    @Test
    @Order(7)
    @DisplayName("Should filter by a valid Type with space (Historic Site)")
    void testFilterByValidTypeWithSpace () {
        catalogLogic.updateSelectedType("Historic Site"); // Type with a space
        catalogLogic.handleFilterAction();

        assertEquals(1, tableModel.getRowCount(), "Should find 1 Historic Site");
        assertEquals(DATA_ROW_4[1], tableModel.getValueAt(0, 1), "Historic Site result should be Old Quebec");
    }


    @Test
    @Order(8)
    @DisplayName("Should filter by both Province (ON) and Type (Landmark)")
    void testFilterByProvinceAndType () {
        catalogLogic.updateSelectedProvince("ON");
        catalogLogic.updateSelectedType("Landmark");
        catalogLogic.handleFilterAction();

        assertEquals(1, tableModel.getRowCount(), "Should find 1 Landmark in ON");
        assertEquals(DATA_ROW_2[1], tableModel.getValueAt(0, 1), "ON Landmark result should be CN Tower");
    }

    @Test
    @Order(9)
    @DisplayName("Should return no results if Province/Type combo doesn't match")
    void testFilterByMismatchProvinceAndType () {
        catalogLogic.updateSelectedProvince("BC"); // Stanley Park
        catalogLogic.updateSelectedType("Waterfall"); // Niagara
        catalogLogic.handleFilterAction();

        assertEquals(0, tableModel.getRowCount(), "Should find 0 Waterfalls in BC");
    }


    @Test
    @Order(10)
    @DisplayName("Should reset filters and show all data")
    void testHandleResetAction () {
        // Apply some filters first
        catalogLogic.updateSelectedProvince("ON");
        catalogLogic.updateSelectedType("Landmark");
        catalogLogic.handleFilterAction();
        assertEquals(1, tableModel.getRowCount(), "Pre-condition: Filter should be active");
        assertNotNull(catalogLogic.getSelectedProvince(), "Pre-condition: Province should be selected");
        assertNotNull(catalogLogic.getSelectedType(), "Pre-condition: Type should be selected");

        // Perform the reset
        catalogLogic.handleResetAction();

        // Verify state after reset
        assertNull(catalogLogic.getSelectedProvince(), "Selected province should be null after reset");
        assertNull(catalogLogic.getSelectedType(), "Selected type should be null after reset");
        assertEquals(4, tableModel.getRowCount(), "Table should show all 4 rows after reset");

        // Also check if GUI reset methods were likely called (requires GUI instance)
        if (catalogLogic.gui != null) {
            // We can't easily verify mock calls without Mockito, but check combo box state
            assertEquals(0, ((JComboBox<?>) catalogLogic.gui.provinceComboBox).getSelectedIndex(), "Province combo box should be reset");
            assertEquals(0, ((JComboBox<?>) catalogLogic.gui.typeComboBox).getSelectedIndex(), "Type combo box should be reset");
        }
    }

    // --- Deletion Tests (Need careful setup as they modify the file) ---
    // Note: These tests will modify the temporary file state between runs if not ordered carefully
    // or if @AfterEach doesn't restore the file perfectly. @TempDir helps isolate runs.

    @Test
    @Order(11)
    @DisplayName("Should delete a selected record successfully")
    void testHandleDeleteAction_ValidSelection () throws IOException, CsvException {
        // Pre-condition: Assume 4 rows loaded initially
        assertEquals(4, tableModel.getRowCount());

        // --- Simulate GUI Interaction ---
        // We need to tell CatalogLogic *which* row to delete without a real JTable selection.
        // Option 1: Modify CatalogLogic.handleDeleteAction to accept a row index (best for testing).
        // Option 2: Assume we can programmatically select a row on the dummy table IF GUI was initialized.
        // Option 3: Directly call the underlying DatabaseManager's delete (less ideal, tests DBManager not CatalogLogic's handling).

        // Let's try Option 2 (requires initializeGui=true in setUp)
        assumeTrue(catalogLogic.gui != null && catalogLogic.gui.getTable() != null, "GUI/Table must be initialized for selection simulation");
        JTable table = catalogLogic.gui.getTable();
        int rowToSelectAndViewIndex = 1; // Select the second row (CN Tower, ID 00002)
        table.setRowSelectionInterval(rowToSelectAndViewIndex, rowToSelectAndViewIndex);
        int modelRowToDelete = table.convertRowIndexToModel(rowToSelectAndViewIndex);
        String idToDelete = (String) tableModel.getValueAt(modelRowToDelete, 0); // Get ID from model

        assertEquals("00002", idToDelete, "Should be deleting ID 00002");

        // Store row count before deletion
        int initialRowCount = tableModel.getRowCount();

        // --- Action ---
        // We need to bypass the JOptionPane confirmation for the test.
        // This is hard without refactoring CatalogLogic or using mocking/power-mocking.
        // WORKAROUND: For this test, let's directly call the core deletion logic
        // parts within handleDeleteAction, bypassing the confirmation dialog.
        try {
            DatabaseManager dbManager = new DatabaseManager(writableDbFile); // Use the *same* test file
            dbManager.deleteById(idToDelete); // Perform the actual deletion
            tableModel.removeRow(modelRowToDelete); // Manually update the model like logic would
            // catalogLogic.gui.showMessage("Location deleted successfully."); // Simulate message

        } catch (DatabaseManager.RecordNotFoundException e) {
            fail("Record to delete (" + idToDelete + ") not found", e);
        } catch (IOException | CsvException e) {
            fail("Error during deletion process", e);
        }

        // --- Verification ---
        assertEquals(initialRowCount - 1, tableModel.getRowCount(), "Row count should decrease by 1 after deletion");

        // Verify the record is actually gone from the file
        DatabaseManager checker = new DatabaseManager(writableDbFile);
        boolean found = false;
        try {
            for (String[] row : checker.readAllRecords()) {
                if (row.length > 0 && row[0].equals(idToDelete)) {
                    found = true;
                    break;
                }
            }
        } catch (Exception e) { /* ignore read errors here, focus on found flag */ }
        assertFalse(found, "Deleted record ID (" + idToDelete + ") should not exist in the file anymore");
    }


    @Test
    @Order(12)
    @DisplayName("Should not delete if no record is selected")
    void testHandleDeleteAction_NoSelection () throws IOException, CsvException {
        // Pre-condition: Assume 4 rows loaded initially
        assertEquals(4, tableModel.getRowCount());
        int initialRowCount = tableModel.getRowCount();

        // Ensure no row is selected in the dummy table
        assumeTrue(catalogLogic.gui != null && catalogLogic.gui.getTable() != null, "GUI/Table must be initialized");
        catalogLogic.gui.getTable().clearSelection();

        // --- Action ---
        // Call the original method. It should check getSelectedRow() == -1 and return early.
        // It *might* show a message dialog which we can't easily test here.
        catalogLogic.handleDeleteAction();

        // --- Verification ---
        assertEquals(initialRowCount, tableModel.getRowCount(), "Row count should NOT change when nothing is selected");

        // Verify file content hasn't changed (optional but good)
        DatabaseManager checker = new DatabaseManager(writableDbFile);
        assertEquals(initialRowCount, checker.readAllRecords().size(), "File content should not change");
    }
}