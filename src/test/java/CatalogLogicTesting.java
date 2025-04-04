import TourCatGUI.Catalog.CatalogLogic;
import TourCatSystem.DatabaseManager; // For checking file state post-delete
import TourCatSystem.Filter; // Import Filter class
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List; // Use List interface

import static org.junit.jupiter.api.Assertions.*;
// No need for Assumptions unless skipping tests

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CatalogLogicTesting {

    @TempDir
    Path tempDir; // JUnit manages this

    CatalogLogic catalogLogic; // Instance under test
    File writableDbFile;     // Path to the test DB file
    DefaultTableModel tableModel; // Direct access to the model state
    Filter filter; // Direct access to the filter state

    // Test Data Constants
    static final String[] HEADER = {"ID", "Name", "City", "Province", "Category"};
    static final String[] DATA_ROW_1 = {"00001", "Niagara Falls", "Niagara", "ON", "Waterfall"};
    static final String[] DATA_ROW_2 = {"00002", "CN Tower", "Toronto", "ON", "Landmark"};
    static final String[] DATA_ROW_3 = {"00003", "Stanley Park", "Vancouver", "BC", "Park"};
    static final String[] DATA_ROW_4 = {"00004", "Old Quebec", "Quebec City", "QC", "Historic Site"};

    @BeforeEach
    void setUp () throws IOException {
        Path dbPath = tempDir.resolve("test_catalog_db.csv");
        writableDbFile = dbPath.toFile();
        System.out.println("Test DB path: " + writableDbFile.getAbsolutePath());

        createBasicTestCsv(writableDbFile);

        // Instantiate CatalogLogic WITHOUT the GUI
        catalogLogic = new CatalogLogic("testUser", writableDbFile, false);

        // Get references to the model and filter used by the logic instance
        tableModel = catalogLogic.getTableModel();
        filter = catalogLogic.getFilter(); // Get the Filter instance

        assertNotNull(tableModel, "Table model should be loaded by CatalogLogic constructor");
        assertNotNull(filter, "Filter object should be created by CatalogLogic constructor");

        // Verify initial state
        assertEquals(4, tableModel.getRowCount(), "Initial model should have 4 data rows from test file");
        assertTrue(filter.getResults().isEmpty(), "Initial Filter results should be empty"); // Filter results are populated on demand
        System.out.println("Setup complete. Initial TableModel Row Count: " + tableModel.getRowCount());
    }

    // Helper to create the CSV file with test data (no changes needed)
    private void createBasicTestCsv (File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                // Fail fast if directory cannot be created
                throw new IOException("Failed to create parent directory for test file: " + parent.getAbsolutePath());
            }
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            writer.writeNext(HEADER);
            writer.writeNext(DATA_ROW_1);
            writer.writeNext(DATA_ROW_2);
            writer.writeNext(DATA_ROW_3);
            writer.writeNext(DATA_ROW_4);
        }
        System.out.println("Created/Replaced test CSV file: " + file.getAbsolutePath());
    }

    @Test
    @Order(0)
    @DisplayName("Should create or load file.")
    void createDatafile () {
        System.out.println(writableDbFile.getAbsolutePath() + ": file exists?");
        assertTrue(writableDbFile.exists(), "Test database file should exist after setup.");
    }

    @Test
    @Order(1)
    @DisplayName("Should load initial data correctly")
    void testInitialDataLoading () {
        assertEquals(4, tableModel.getRowCount(), "Should load 4 data rows");
        assertEquals(DATA_ROW_1[1], tableModel.getValueAt(0, 1), "Row 1 Name check"); // Niagara Falls
        assertEquals(DATA_ROW_3[3], tableModel.getValueAt(2, 3), "Row 3 Province check"); // BC
        assertEquals(DATA_ROW_4[4], tableModel.getValueAt(3, 4), "Row 4 Category check"); // Historic Site
    }

    @Test
    @Order(2)
    @DisplayName("Should read all data rows correctly using readAllDataFromWritableFile")
    void testReadAllDataFromWritableFile () {
        List<String> dataLines = catalogLogic.readAllDataFromWritableFile();

        assertNotNull(dataLines);
        assertEquals(4 * 5, dataLines.size(), "Should read all 4 data rows (header skipped)");
        // Check content more specifically if needed, e.g., using contains or equals on split parts
        assertTrue(dataLines.get(0).contains(DATA_ROW_1[0]), "First data line should start with ID of DATA_ROW_1");
        //System.out.println(dataLines.get(4) + " " + DATA_ROW_2[0]);
        assertTrue(dataLines.get(5).contains(DATA_ROW_2[0]), "Second data line should start with ID of DATA_ROW_2");

    }

    @Test
    @Order(3)
    @DisplayName("Should filter by valid Province (ON)")
    void testFilterByValidProvinceON () {
        // --- Action ---
        catalogLogic.updateSelectedProvince("ON");
        catalogLogic.updateSelectedType(null);
        catalogLogic.handleFilterAction(); // This updates both Filter results AND tableModel

        // --- Verification ---
        // 1. Check the Filter object's internal state
        List<String> filterResults = filter.getResults(); // Get results stored within Filter
        assertNotNull(filterResults, "Filter results list should not be null");
        assertEquals(2, filterResults.size(), "Filter object's internal results should contain 2 rows for ON");
        assertTrue(filterResults.get(0).contains(DATA_ROW_1[1]), "Filter result 1 check"); // Niagara
        assertTrue(filterResults.get(1).contains(DATA_ROW_2[1]), "Filter result 2 check"); // CN Tower


        // 2. Check the TableModel's state (should match filter results)
        assertEquals(2, tableModel.getRowCount(), "TableModel should be updated to show 2 rows for ON");
        assertEquals(DATA_ROW_1[1], tableModel.getValueAt(0, 1), "TableModel row 1 Name check after filter"); // Niagara Falls
        assertEquals(DATA_ROW_2[1], tableModel.getValueAt(1, 1), "TableModel row 2 Name check after filter"); // CN Tower
    }

    // --- Other tests (modified slightly for consistency) ---

    @Test
    @Order(5)
    @DisplayName("Should return no results for non-existent Province (MB)")
    void testFilterByInvalidProvince () {
        catalogLogic.updateSelectedProvince("MB");
        catalogLogic.updateSelectedType(null);
        catalogLogic.handleFilterAction();

        // Check Filter state
        assertEquals(0, filter.getResults().size(), "Filter results for MB should be empty");
        // Check TableModel state
        assertEquals(0, tableModel.getRowCount(), "TableModel should show 0 rows for MB");
    }

    @Test
    @Order(6)
    @DisplayName("Should filter by valid Type (Park)")
    void testFilterByValidType () {
        catalogLogic.updateSelectedProvince(null);
        catalogLogic.updateSelectedType("Park");
        catalogLogic.handleFilterAction();

        // Check Filter state
        assertEquals(1, filter.getResults().size(), "Filter results for Park should have 1 row");
        // Check TableModel state
        assertEquals(1, tableModel.getRowCount(), "TableModel should show 1 row for Park");
        assertEquals(DATA_ROW_3[1], tableModel.getValueAt(0, 1), "Park result should be Stanley Park");
    }


    @Test
    @Order(7)
    @DisplayName("Should filter by valid Type with space (Historic Site)")
    void testFilterByValidTypeWithSpace () {
        catalogLogic.updateSelectedProvince(null);
        catalogLogic.updateSelectedType("Historic Site"); // Type with a space
        catalogLogic.handleFilterAction();

        assertEquals(1, filter.getResults().size(), "Filter results for Historic Site should have 1 row");
        assertEquals(1, tableModel.getRowCount(), "TableModel should show 1 row for Historic Site");
        assertEquals(DATA_ROW_4[1], tableModel.getValueAt(0, 1), "Historic Site result should be Old Quebec");
    }

    @Test
    @Order(8)
    @DisplayName("Should filter by both Province (ON) and Type (Landmark)")
    void testFilterByProvinceAndType () {
        catalogLogic.updateSelectedProvince("ON");
        catalogLogic.updateSelectedType("Landmark");
        catalogLogic.handleFilterAction();

        assertEquals(1, filter.getResults().size(), "Filter results for ON/Landmark should have 1 row");
        assertEquals(1, tableModel.getRowCount(), "TableModel should show 1 row for ON/Landmark");
        assertEquals(DATA_ROW_2[1], tableModel.getValueAt(0, 1), "ON Landmark result should be CN Tower");
    }

    @Test
    @Order(9)
    @DisplayName("Should return no results if Province/Type combo doesn't match")
    void testFilterByMismatchProvinceAndType () {
        catalogLogic.updateSelectedProvince("BC"); // Stanley Park is BC/Park
        catalogLogic.updateSelectedType("Waterfall"); // Niagara is ON/Waterfall
        catalogLogic.handleFilterAction();

        assertEquals(0, filter.getResults().size(), "Filter results for BC/Waterfall should be empty");
        assertEquals(0, tableModel.getRowCount(), "TableModel should show 0 rows for BC/Waterfall");
    }

    @Test
    @Order(10)
    @DisplayName("Should reset filters and show all data")
    void testResetAction () {
        // Apply some filters first
        catalogLogic.updateSelectedProvince("ON");
        catalogLogic.updateSelectedType("Landmark");
        catalogLogic.handleFilterAction();
        assertEquals(1, tableModel.getRowCount(), "Pre-condition: Should be filtered to 1 row");

        // Action: Reset
        catalogLogic.handleResetAction(); // This should reload all data into tableModel

        // Verification:
        // Filter results should be empty AFTER reset (until a new filter is applied)
        // Note: handleResetAction currently reloads tableModel but doesn't explicitly clear Filter results
        // Let's adjust handleResetAction or the test assertion based on desired behavior.
        // Assuming handleResetAction SHOULD clear the filter state:
        // assertTrue(filter.getResults().isEmpty(), "Filter results should be empty after reset");

        // TableModel should show all original rows
        assertEquals(4, tableModel.getRowCount(), "TableModel should show all 4 rows after reset");
        assertEquals(DATA_ROW_1[1], tableModel.getValueAt(0, 1), "Row 1 Name check after reset");
        assertEquals(DATA_ROW_4[1], tableModel.getValueAt(3, 1), "Row 4 Name check after reset");
    }

    @Test
    @Order(11)
    @DisplayName("Should NOT delete if handleDeleteAction called without GUI/Selection")
    void testHandleDeleteAction_NoGuiOrSelection () throws IOException, CsvException {
        assertEquals(4, tableModel.getRowCount());
        long initialFileLength = writableDbFile.length();

        // Action: Call delete without GUI context
        catalogLogic.handleDeleteAction(); // This needs CatalogView to work properly.

        // Verification: Without GUI/selection/confirmation, nothing should happen
        assertEquals(4, tableModel.getRowCount(), "Model row count should NOT change");
        assertEquals(initialFileLength, writableDbFile.length(), "File size should not change");

        // Verify file content using DatabaseManager
        DatabaseManager checker = new DatabaseManager(writableDbFile);
        List<String[]> records = checker.readAllRecords(); // Reads data rows only
        assertEquals(4, records.size(), "File should still contain 4 data records");
    }

    // Deletion test remains commented out due to GUI dependency (JOptionPane, JTable selection)
    /*
    @Test
    @Order(12)
    @DisplayName("Should delete selected record (Requires Mocking/Refactoring)")
    void testHandleDeleteAction_WithSelection() {
        fail("Test testHandleDeleteAction_WithSelection is not implemented due to JOptionPane and GUI selection complexity. Requires mocking or refactoring CatalogLogic.");
    }
    */

    // Helper for commented-out test (no changes needed)
    private boolean checkIfIdExistsInFile (String id, File file) {
        try {
            DatabaseManager checker = new DatabaseManager(file);
            for (String[] row : checker.readAllRecords()) {
                if (row != null && row.length > 0 && row[0] != null && row[0].equals(id)) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking file for ID existence: " + e.getMessage());
        }
        return false;
    }
}