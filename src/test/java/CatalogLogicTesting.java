import TourCatGUI.Catalog.CatalogLogic;
import TourCatSystem.DatabaseManager; // For checking file state post-delete
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CatalogLogicTesting {

    @TempDir
    Path tempDir; // JUnit manages this

    CatalogLogic catalogLogic; // Instance under test
    File writableDbFile;     // Path to the test DB file
    DefaultTableModel tableModel; // Direct access to the model state

    // Test Data Constants
    static final String[] HEADER = {"ID", "Name", "City", "Province", "Category"};
    static final String[] DATA_ROW_1 = {"00001", "Niagara Falls", "Niagara", "ON", "Waterfall"};
    static final String[] DATA_ROW_2 = {"00002", "CN Tower", "Toronto", "ON", "Landmark"};
    static final String[] DATA_ROW_3 = {"00003", "Stanley Park", "Vancouver", "BC", "Park"};
    static final String[] DATA_ROW_4 = {"00004", "Old Quebec", "Quebec City", "QC", "Historic Site"};

    @BeforeEach
    void setUp () throws IOException {
        // 1. Define the path for the database within the temp directory
        Path dbPath = tempDir.resolve("test_catalog_db.csv"); // Use a unique name
        writableDbFile = dbPath.toFile();
        System.out.println("Test DB path: " + writableDbFile.getAbsolutePath());

        // 2. Create a fresh dummy CSV file for each test
        //    This ensures tests are isolated and start from a known state.
        createBasicTestCsv(writableDbFile);

        // 3. Instantiate CatalogLogic WITHOUT the GUI
        //    Pass the path to our temporary database file.
        catalogLogic = new CatalogLogic("testUser", writableDbFile, false); // *** false = no GUI ***

        // 4. Get the table model loaded by CatalogLogic
        tableModel = catalogLogic.getTableModel();
        assertNotNull(tableModel, "Table model should be loaded by CatalogLogic constructor");

        // 5. Verify initial state (optional but good)
        assertEquals(4, tableModel.getRowCount(), "Initial model should have 4 data rows from test file");
        System.out.println("Setup complete. Initial TableModel Row Count: " + tableModel.getRowCount());
    }

    // Helper to create the CSV file with test data
    private void createBasicTestCsv (File file) throws IOException {
        // Ensure parent directory exists (JUnit's @TempDir usually handles this, but belt-and-suspenders)
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
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

        Assertions.assertTrue(writableDbFile.exists());

    }

    @Test
    @Order(1)
    @DisplayName("Should load initial data correctly")
    void testInitialDataLoading () {
        // Assertions are mostly covered by setUp verification, but can add more specific checks
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

        System.out.println(dataLines.get(0));
        System.out.println(DATA_ROW_1[0]);

        assertNotNull(dataLines);
        assertEquals(4, dataLines.size(), "Should read all 4 data rows (header skipped)");
        assertTrue(dataLines.get(0).contains(DATA_ROW_1[0]), "First data line check"); // Starts with "00001"
        assertTrue(dataLines.get(0).contains(DATA_ROW_1[1]), "First data line check"); // Starts with "00001"
    }

    @Test
    @Order(3)
    @DisplayName("Should filter by valid Province (ON)")
    void testFilterByValidProvinceON () {
        catalogLogic.updateSelectedProvince("ON");
        catalogLogic.updateSelectedType(null);
        catalogLogic.handleFilterAction();


        List<String> strings = catalogLogic.getFilter().getResults();


        assertEquals(2, strings.size(), "Should find 2 locations in ON");
    }

    @Test
    @Order(5)
    @DisplayName("Should return no results for non-existent Province (MB)")
    void testFilterByInvalidProvince () {
        catalogLogic.updateSelectedProvince("MB");
        catalogLogic.updateSelectedType(null);
        catalogLogic.handleFilterAction();

        assertEquals(0, tableModel.getRowCount(), "Should find 0 locations in MB");
    }

    @Test
    @Order(6)
    @DisplayName("Should filter by valid Type (Park)")
    void testFilterByValidType () {
        catalogLogic.updateSelectedProvince(null);
        catalogLogic.updateSelectedType("Park");
        catalogLogic.handleFilterAction();

        assertEquals(1, tableModel.getRowCount(), "Should find 1 Park");
        assertEquals(DATA_ROW_3[1], tableModel.getValueAt(0, 1), "Park result should be Stanley Park");
    }


    @Test
    @Order(7)
    @DisplayName("Should filter by valid Type with space (Historic Site)")
    void testFilterByValidTypeWithSpace () {
        catalogLogic.updateSelectedProvince(null);
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
        catalogLogic.updateSelectedProvince("BC"); // Stanley Park is BC/Park
        catalogLogic.updateSelectedType("Waterfall"); // Niagara is ON/Waterfall
        catalogLogic.handleFilterAction();

        assertEquals(0, tableModel.getRowCount(), "Should find 0 Waterfalls in BC");
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
        catalogLogic.handleResetAction();

        // Verification: Should show all original rows
        assertEquals(4, tableModel.getRowCount(), "Should show all 4 rows after reset");
        assertEquals(DATA_ROW_1[1], tableModel.getValueAt(0, 1), "Row 1 Name check after reset");
        assertEquals(DATA_ROW_4[1], tableModel.getValueAt(3, 1), "Row 4 Name check after reset");
    }


    @Test
    @Order(11)
    @DisplayName("Should NOT delete if handleDeleteAction called without GUI/Selection")
    void testHandleDeleteAction_NoGuiOrSelection () throws IOException, CsvException {
        // Pre-condition: 4 rows exist in model and file
        assertEquals(4, tableModel.getRowCount());
        long initialFileLength = writableDbFile.length(); // Check file size as proxy for content change

        // Action: Call delete. Since we initialized without GUI, it should ideally do nothing
        // or log an error, but not modify the data.
        catalogLogic.handleDeleteAction();

        // Verification
        assertEquals(4, tableModel.getRowCount(), "Model row count should NOT change");

        // Verify file content hasn't changed
        assertEquals(initialFileLength, writableDbFile.length(), "File size should not change");
        // Optionally, read the file again and verify content fully
        DatabaseManager checker = new DatabaseManager(writableDbFile);
        List<String[]> records = checker.readAllRecords(); // Reads data rows only
        assertEquals(4, records.size(), "File should still contain 4 data records");
    }

    // --- Deletion Success Test (Commented Out - Requires Mocking/Refactoring) ---
    /*
    @Test
    @Order(12)
    @DisplayName("Should delete selected record (Requires Mocking/Refactoring)")
    void testHandleDeleteAction_WithSelection() {
        // PROBLEM: Cannot easily test this path without:
        // 1. Initializing the GUI (difficult in headless tests, introduces Swing dependency).
        // 2. Simulating row selection reliably.
        // 3. Bypassing or Mocking the JOptionPane confirmation dialog.

        // --- Potential Setup (if GUI were initialized and mocking possible) ---
        // CatalogLogic testLogicWithGui = new CatalogLogic("testUser", writableDbFile, true); // Need GUI
        // DefaultTableModel testModel = testLogicWithGui.getTableModel();
        // assumeTrue(testLogicWithGui.gui != null && testLogicWithGui.gui.getTable() != null, "GUI Required");
        // JTable table = testLogicWithGui.gui.getTable();
        // int viewRowToSelect = 1; // e.g., CN Tower (ID 00002)
        // int modelRowToDelete = table.convertRowIndexToModel(viewRowToSelect);
        // String idToDelete = (String) testModel.getValueAt(modelRowToDelete, 0);
        // table.setRowSelectionInterval(viewRowToSelect, viewRowToSelect); // Simulate selection

        // --- Mocking JOptionPane (Conceptual using Mockito/PowerMock) ---
        // PowerMockito.mockStatic(JOptionPane.class);
        // Mockito.when(JOptionPane.showConfirmDialog(any(), any(), any(), anyInt(), anyInt())).thenReturn(JOptionPane.YES_OPTION);

        // --- Action ---
        // testLogicWithGui.handleDeleteAction();

        // --- Assertions ---
        // assertEquals(3, testModel.getRowCount(), "Model should have one less row");
        // // Verify file using DatabaseManager...
        // assertFalse(checkIfIdExistsInFile(idToDelete, writableDbFile), "ID should be removed from file");

        fail("Test testHandleDeleteAction_WithSelection is not implemented due to JOptionPane and GUI selection complexity. Requires mocking or refactoring CatalogLogic.");
    }
    */

    // Helper function for commented-out test
    private boolean checkIfIdExistsInFile (String id, File file) {
        try {
            DatabaseManager checker = new DatabaseManager(file);
            for (String[] row : checker.readAllRecords()) {
                if (row.length > 0 && row[0].equals(id)) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking file for ID existence: " + e.getMessage());
        }
        return false;
    }
}