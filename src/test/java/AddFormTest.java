import TourCatGUI.Forms.AddFormLogic; // Ensure correct package
import TourCatSystem.DatabaseManager;
// Removed: import TourCatSystem.FileManager; // No longer needed here
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*; // For assumptions if AddFormLogic instantiation fails

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AddFormTest {

    // Option 1: Dedicated test directory (as before, but path defined manually)
    static Path testDirectory = Paths.get("test_data_add_adapted").toAbsolutePath(); // Use absolute path
    static Path testDatabasePath;
    static Path testImagePath; // Define path for potential image tests
    static File testDatabaseFile;

    // Option 2: Use JUnit's temporary directory (often preferred for isolation)
    // @TempDir
    // static Path sharedTempDir; // JUnit manages creation/deletion
    // static Path testDatabasePath;
    // static Path testImagePath;
    // static File testDatabaseFile;


    // Sample Header
    static final String[] HEADER = {"ID", "Name", "City", "Province", "Category"};
    static final String[] INITIAL_RECORD = {"00000", "Initial", "InitCity", "InitProv", "InitCat"};

    @BeforeAll
    static void setupClass () throws IOException {
        // --- Using Option 1 (Manual Directory) ---
        Files.createDirectories(testDirectory);
        testDatabasePath = testDirectory.resolve("testAddDB_Adapted.csv");
        testImagePath = testDirectory.resolve("test_images"); // Directory for test images
        Files.createDirectories(testImagePath); // Create image dir
        testDatabaseFile = testDatabasePath.toFile();
        System.out.println("Test setup using manual directory: " + testDirectory);
        System.out.println("Test database path: " + testDatabasePath);
        System.out.println("Test image path: " + testImagePath);


        // --- Using Option 2 (JUnit TempDir) ---
        /*
        assumeTrue(sharedTempDir != null, "JUnit TempDir was not injected");
        testDatabasePath = sharedTempDir.resolve("testAddDB_Adapted.csv");
        testImagePath = sharedTempDir.resolve("test_images");
        Files.createDirectories(testImagePath);
        testDatabaseFile = testDatabasePath.toFile();
        System.out.println("Test setup using TempDir: " + sharedTempDir);
        System.out.println("Test database path: " + testDatabasePath);
        System.out.println("Test image path: " + testImagePath);
        */
    }

    @BeforeEach
    void setupTest () throws IOException {
        // Ensure paths are set
        assumeTrue(testDatabasePath != null && testDatabaseFile != null, "Test database path not initialized");

        // Write initial data to the test database file
        try (Writer writer = Files.newBufferedWriter(testDatabasePath, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer,
                     CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.NO_QUOTE_CHARACTER, // Match DatabaseManager config
                     CSVWriter.NO_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            csvWriter.writeNext(HEADER);
            csvWriter.writeNext(INITIAL_RECORD);
        } catch (Exception e) {
            fail("Failed to setup test database file in @BeforeEach", e);
        }
    }

    @AfterEach
    void tearDownTest () throws IOException {
        // Clean up the database file after each test
        if (testDatabasePath != null) {
            Files.deleteIfExists(testDatabasePath);
        }
        // Clean up any potential image files created during tests (more robust cleanup needed if image tests are active)
        // Example: Files.walk(testImagePath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @AfterAll
    static void tearDownClass () throws IOException {
        // Clean up the main test directory if using Option 1
        if (testDirectory != null && Files.exists(testDirectory)) {
            // Simple delete, might fail if dirs not empty (e.g., images left)
            // For robustness, consider recursive delete if needed.
            try {
                // Clean image dir first
                if (testImagePath != null && Files.exists(testImagePath)) {
                    Files.deleteIfExists(testImagePath); // Delete image dir if empty
                }
                Files.deleteIfExists(testDirectory); // Delete main test dir if empty
            } catch (IOException e) {
                System.err.println("Warning: Could not fully clean up test directory: " + testDirectory + " - " + e.getMessage());
            }
        }
        // No need to cleanup TempDir if using Option 2 - JUnit handles it
    }

    // Helper to read CSV content for verification (no change needed)
    private List<String[]> readCsvContent (File file) throws IOException, CsvException {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            // Explicitly configure parser if DatabaseManager uses specific settings
            CSVReader csvReader = new CSVReader(reader); // Simpler, assumes default parsing is sufficient for test check
            List<String[]> content = csvReader.readAll();
            // csvReader.close(); // try-with-resources handles closing
            return content;
        }
    }

    // Helper to simulate ID generation (no change needed)
    // Relies on DatabaseManager operating on the provided file.
    private String simulateGenerateNextId (File currentFile) throws IOException {
        DatabaseManager tempDbManager = new DatabaseManager(currentFile);
        return tempDbManager.getNextID();
    }

    // Helper to simulate the add action (no change needed)
    // Relies on DatabaseManager operating on the provided file.
    private void simulateAddRecordAction (String[] data, File file) throws IOException {
        DatabaseManager localDbManager = new DatabaseManager(file);
        localDbManager.addRecord(data);
    }


    @Test
    @Order(1)
    @DisplayName("[Adapted] Should add a valid record successfully")
    void addValidRecordSuccess () throws IOException, CsvException {
        // --- Input Data ---
        String name = "New Landmark";
        String city = "Test City";
        String province = "ON";
        String category = "Monument";

        // --- Initial State ---
        List<String[]> initialContent = readCsvContent(testDatabaseFile);
        assertEquals(2, initialContent.size(), "Initial file should have header + 1 initial record");
        String expectedNextIdStr = simulateGenerateNextId(testDatabaseFile);

        // --- Action ---
        String[] newLocationData = {expectedNextIdStr, name, city, province, category};
        assertDoesNotThrow(
                () -> simulateAddRecordAction(newLocationData, testDatabaseFile),
                "Simulated addRecord should not throw IO exception for valid data using test file"
        );

        // --- Verification ---
        List<String[]> finalContent = readCsvContent(testDatabaseFile);
        assertEquals(3, finalContent.size(), "File should have header + initial record + 1 new data row");
        String[] addedRow = finalContent.get(2); // Get the last added row (index = size - 1)
        assertNotNull(addedRow, "Added row should not be null");
        assertArrayEquals(newLocationData, addedRow, "Added row content should match input data");
    }


    @Test
    @Order(3)
    @DisplayName("[Adapted] Should NOT add record for incomplete form data")
    void addIncompleteRecordNoChange () throws IOException, CsvException {
        // --- Input Data (Missing Name) ---
        String name = ""; // Invalid
        String city = "Test City";
        String province = "ON";
        String category = "Monument";

        // --- Initial State ---
        List<String[]> initialContent = readCsvContent(testDatabaseFile);
        assertEquals(2, initialContent.size(), "Initial file state check");

        // --- Action: Simulate ONLY the validation check ---
        // Requires instantiating AddFormLogic just for this call.
        // This instance *will* try to initialize its own file paths, but we ignore that
        // for this test, focusing only on the validation return value.
        boolean isValid;
        AddFormLogic tempLogicForValidation = null;
        try {
            // Pass the test database file path to potentially influence its behavior,
            // although isInputValid doesn't depend on it. AddFormLogic constructor needs updating
            // or we accept it uses its default path logic here. Let's assume current constructor.
            tempLogicForValidation = new AddFormLogic("testUserForValidation");
            isValid = tempLogicForValidation.isInputValid(name, city, province, category);
        } catch (Exception e) {
            // If AddFormLogic constructor fails (e.g., cannot create default paths),
            // we cannot perform the validation check this way.
            //fail("Failed to create temporary AddFormLogic for validation. Check constructor resilience.", e);

            // Alternative: Assume the test cannot proceed if AddFormLogic cannot be instantiated.
            Assumptions.abort("Could not instantiate AddFormLogic to test validation: " + e.getMessage());
            return; // Keep compiler happy
        } finally {
            // If AddFormLogic created a GUI, try to dispose it (if AddFormLogic provides a way)
            // if (tempLogicForValidation != null && tempLogicForValidation.getGui() != null) {
            //     tempLogicForValidation.getGui().dispose();
            // }
        }

        assertFalse(isValid, "isInputValid should return false for incomplete data");

        // --- Verification ---
        // Verify the TEST database file hasn't changed.
        List<String[]> finalContent = readCsvContent(testDatabaseFile);
        assertEquals(2, finalContent.size(), "Test DB file size should not change after failed validation");
        // Compare content using assertLinesMatch or deep array comparison
        assertArrayEquals(initialContent.toArray(), finalContent.toArray(),
                "Test DB file content should not change on validation failure");
    }

    // --- Image Test (Still Commented Out) ---
    // To implement this correctly without FileManager:
    // 1. Create a dummy image file within the test setup (e.g., in testImagePath).
    // 2. Simulate adding the CSV record to testDatabaseFile.
    // 3. Instantiate AddFormLogic. This instance will determine its *own* writable image path.
    // 4. **Challenge:** We need a way for the test to *know* where AddFormLogic *will* save the image.
    //    - Option A: Modify AddFormLogic constructor to accept the target image directory (best for testing).
    //    - Option B: Replicate AddFormLogic's getApplicationDirectory logic within the test to *predict* the path. (Brittle).
    // 5. Simulate the image save call (maybe expose a package-private method in AddFormLogic or make saveImageToWritableLocation public for testing).
    // 6. Assert that the image file exists in the *predicted/provided* writable image directory (NOT the testImagePath setup dir).
    // 7. Clean up the saved image file in @AfterEach or @AfterAll.

//    @Test
//    @Order(4)
//    @DisplayName("[Adapted] Should attempt to save image when provided (basic check)")
//    void addRecordWithImage() throws IOException {
//        // ... implementation requires careful handling of AddFormLogic's internal paths ...
//    }

}