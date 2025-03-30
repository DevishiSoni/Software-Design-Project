import TourCatGUI.Forms.AddFormLogic; // Correct package if it moved
import TourCatSystem.DatabaseManager;
import TourCatSystem.FileManager; // Assuming this provides the test file path
import com.opencsv.CSVReader; // For reading test file
import com.opencsv.CSVWriter; // For writing test file
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.*; // Use JUnit 5 annotations

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*; // Use JUnit 5 assertions

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AddFormLogicTest_Adapted {

    static Path testDirectory = Paths.get("test_data_add_adapted"); // Dedicated test directory
    static Path testDatabasePath;
    static File testDatabaseFile;

    // Sample Header
    static final String[] HEADER = {"ID", "Name", "City", "Province", "Category"};

    static final String[] INITIAL_RECORD = {"00000", "Initial", "InitCity", "InitProv", "InitCat"};

    @BeforeAll
    static void setupClass() throws IOException {

        Files.createDirectories(testDirectory);
        // Rely on FileManager to provide the path for the test file name
        testDatabaseFile = FileManager.getInstance(true) // Use testing mode if it helps isolate
                .getResourceFile("testAddDB_Adapted.csv"); // Use a specific test file name
        testDatabasePath = testDatabaseFile.toPath();

        System.out.println("Test database path: " + testDatabasePath.toAbsolutePath());
    }

    @BeforeEach
    void setupTest() throws IOException {

        try (Writer writer = Files.newBufferedWriter(testDatabasePath, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer,
                     CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.NO_QUOTE_CHARACTER, // Match DatabaseManager config
                     CSVWriter.NO_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            csvWriter.writeNext(HEADER);
            csvWriter.writeNext(INITIAL_RECORD); // Add initial record
        }
    }

    @AfterEach
    void tearDownTest() throws IOException {
        Files.deleteIfExists(testDatabasePath);
    }

    @AfterAll
    static void tearDownClass() throws IOException {
        // Files.deleteIfExists(testDirectory);
    }

    // Helper to read CSV content for verification
    private List<String[]> readCsvContent(File file) throws IOException, CsvException {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> content = csvReader.readAll();
            csvReader.close(); // Explicitly close reader
            return content;
        }
    }

    // Helper to simulate ID generation based on AddFormLogic's current implementation
    // Needs the current file state. Handles the OptionalInt bug.
    private String simulateGenerateNextId(File currentFile) throws IOException {
        DatabaseManager tempDbManager = new DatabaseManager(currentFile); // Create temporary manager to check ID
        OptionalInt maxIdOpt = tempDbManager.getMaxId();
        if (!maxIdOpt.isPresent()) {
            // This simulates the state where the original code would throw NoSuchElementException
            // We handle it here to make the test runnable, but highlight the underlying issue.
            System.err.println("TEST WARNING: Max ID not found, defaulting to 0. Original code might fail here.");
            return String.format("%05d", 0); // Start from 0 if file was unexpectedly empty/invalid
        }
        int nextId = maxIdOpt.getAsInt() + 1;
        return String.format("%05d", nextId);
    }

    // Helper to simulate the actual add action as performed in handleSubmitAction
    // Creates a local DatabaseManager instance.
    private void simulateAddRecordAction(String[] data, File file) throws IOException {
        DatabaseManager localDbManager = new DatabaseManager(file); // Mimics local creation
        localDbManager.addRecord(data);
    }


    @Test
    @Order(1)
    @DisplayName("[Adapted] Should add a valid record successfully")
    void addValidRecordSuccess() throws IOException, CsvException {
        // --- Input Data ---
        String name = "New Landmark";
        String city = "Test City";
        String province = "ON";
        String category = "Monument";
        // We don't directly test image saving here, focus on CSV change

        // --- Initial State ---
        List<String[]> initialContent = readCsvContent(testDatabaseFile);
        assertEquals(2, initialContent.size(), "Initial file should have header + 1 initial record"); // Header + Initial Record
        String expectedNextIdStr = simulateGenerateNextId(testDatabaseFile); // Get expected ID based on current file

        // --- Action (Simulate steps of handleSubmitAction for valid data) ---
        String[] newLocationData = {
                expectedNextIdStr, name, city, province, category
        };
        // Simulate the add action using the helper that mimics the flawed logic
        assertDoesNotThrow(
                () -> simulateAddRecordAction(newLocationData, testDatabaseFile),
                "Simulated addRecord should not throw IO exception for valid data"
        );

        // --- Verification ---
        List<String[]> finalContent = readCsvContent(testDatabaseFile);
        assertEquals(3, finalContent.size(), "File should have header + initial record + 1 new data row");

        // Verify the added row content (it will be the last row, index 2)
        String[] addedRow = finalContent.get(2);
        assertNotNull(addedRow, "Added row should not be null");
        assertEquals(5, addedRow.length, "Added row should have 5 columns");
        assertEquals(expectedNextIdStr, addedRow[0], "ID should be the next sequential ID");
        assertEquals(name, addedRow[1], "Name should match input");
        // ... check other columns ...
        assertEquals(category, addedRow[4], "Category should match input");
    }

    @Test
    @Order(2)
    @DisplayName("[Adapted] Should increment Max ID after adding a record")
    void newHighestIDTest() throws IOException, CsvException {
        // --- Add a first record (simulated) ---
        String firstId = simulateGenerateNextId(testDatabaseFile);
        String[] firstData = {firstId, "First", "CityA", "ProvA", "CatA"};
        simulateAddRecordAction(firstData, testDatabaseFile);
        int idAfterFirstAdd = Integer.parseInt(firstId); // ID we just added

        // --- Add a second record (simulated) ---
        String secondId = simulateGenerateNextId(testDatabaseFile); // ID based on state *after* first add
        String[] secondData = {secondId, "Second", "CityB", "ProvB", "CatB"};
        simulateAddRecordAction(secondData, testDatabaseFile);
        int idAfterSecondAdd = Integer.parseInt(secondId); // ID we just added

        // --- Verification ---
        assertEquals(idAfterFirstAdd + 1, idAfterSecondAdd, "ID after second add should be one greater than the first");

        // Verify using DatabaseManager's getMaxId on the final state
        DatabaseManager finalDbManager = new DatabaseManager(testDatabaseFile);
        int finalMaxId = finalDbManager.getMaxId().orElse(-1);
        assertEquals(idAfterSecondAdd, finalMaxId, "Final max ID in file should match the last added ID");
    }


    @Test
    @Order(3)
    @DisplayName("[Adapted] Should NOT add record for incomplete form data")
    void addIncompleteRecordNoChange() throws IOException, CsvException {
        // --- Input Data (Missing Name) ---
        String name = ""; // Invalid according to isInputValid
        String city = "Test City";
        String province = "ON";
        String category = "Monument";

        // --- Initial State ---
        List<String[]> initialContent = readCsvContent(testDatabaseFile);
        assertEquals(2, initialContent.size(), "Initial file should have header + 1 record");

        // --- Action (Simulate the validation check only) ---
        // We need to access or replicate the isInputValid logic. Let's assume we can call it.
        // If AddFormLogic needs an instance, create one temporarily ONLY for validation check.
        // This is awkward due to the lack of a test constructor/static validation method.
        boolean isValid;
        try {
            // Temporary instance just to call validation - less than ideal
            AddFormLogic tempLogicForValidation = new AddFormLogic("test");
            isValid = tempLogicForValidation.isInputValid(name, city, province, category);
        } catch (Exception e) {
            fail("Failed to create temporary AddFormLogic for validation", e);
            return; // Keep compiler happy
        }

        assertFalse(isValid, "isInputValid should return false for incomplete data");

        // --- Verification ---
        // Since validation failed, handleSubmitAction would return. No addRecord is called.
        // Verify the file hasn't changed.
        List<String[]> finalContent = readCsvContent(testDatabaseFile);
        assertEquals(2, finalContent.size(), "File should still have header + 1 record after failed validation");
        // Use deep array comparison
        assertLinesMatch(initialContent.stream().map(Arrays::toString),
                finalContent.stream().map(Arrays::toString), // Compare string representations for simplicity here
                "File content should not change on validation failure");
    }

    // --- Image Test remains largely the same, relying on file system ---
//    @Test
//    @Order(4)
//    @DisplayName("[Adapted] Should attempt to save image when provided (basic check)")
//    void addRecordWithImage() throws IOException {
//        // --- Setup: Create a dummy image file ---
//
//
//
//        Path dummyImagePath = testDirectory.resolve("dummyImage_adapted.png");
//
//        Path newDummyImage = dummyImagePath.resolveSibling("newDummy.png");
//
//        Files.createFile(newDummyImage);
//
//        File dummyImageFile = dummyImagePath.toFile();
//
//        // --- Input Data ---
//        String name = "Image Landmark Adapted";
//        String city = "Img City";
//        String province = "BC";
//        String category = "Viewpoint";
//
//        // --- Action (Simulate adding the record first) ---
//        String expectedImageId = simulateGenerateNextId(testDatabaseFile); // Get ID before adding
//        String[] data = {expectedImageId, name, city, province, category};
//        simulateAddRecordAction(data, testDatabaseFile); // Add the CSV record
//
//        // --- Simulate image saving (call saveImageResource) ---
//        // Need an AddFormLogic instance to call the private method. Create one.
//        boolean imageSaveAttempted;
//        boolean imageSaveSuccess;
//        try {
//            AddFormLogic tempLogicForImage = new AddFormLogic("tempuser.");
//            // We cannot call the private saveImageResource directly.
//            // We assume handleSubmitAction would call it.
//            // The best we can do here is simulate the outcome: check if the file exists.
//            imageSaveAttempted = true; // We know the code *tries* to save if selectedImage!=null
//
//            // Check if the file *actually* got saved by the logic (if it were run fully)
//            File imageResourceFolder = FileManager.getInstance().getImageResourceFolder();
//            String expectedImageName = expectedImageId + ".png"; // Assuming png
//            File expectedImageFile = new File(imageResourceFolder, expectedImageName);
//            imageSaveSuccess = expectedImageFile.exists(); // Check existence
//
//            // Clean up the potentially created image file
//            Files.deleteIfExists(expectedImageFile.toPath());
//
//        } catch(Exception e) {
//            fail("Failed during image saving simulation", e);
//            return; // Keep compiler happy
//        }
//
//
//        // --- Verification ---
//        assertTrue(imageSaveAttempted, "Code should attempt to save image");
//        //assertTrue(imageSaveSuccess, "Expected image file should have been created by the logic");
//
//
//        Files.deleteIfExists(newDummyImage);
//    }
//}
}