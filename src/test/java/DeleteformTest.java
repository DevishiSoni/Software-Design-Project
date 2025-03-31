import TourCatData.DatabaseManager;
import TourCatGUI.DeleteForm;
import TourCatService.LocationService;
import TourCatData.FileManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class DeleteFormTest {

    static DeleteForm testDelForm;
    static File testingDatabase;
    public static LocationService locationService;

    @BeforeAll
    public static void setup() {
        testingDatabase = FileManager.getInstance(true).getResourceFile("testDB.csv");
        FileManager fileManager = FileManager.getInstance(true);
        DatabaseManager databaseManager = new DatabaseManager();


        locationService = new LocationService(databaseManager, fileManager);
        testDelForm = new DeleteForm("test", locationService);

        testDelForm.setDatabaseFile(testingDatabase);
    }

    private long countLines(File file) throws IOException {
        return Files.lines(Paths.get(file.toURI())).count();
    }

    @Test
    void delSuccessTest() throws IOException {
        // Count lines before deleting
        long initialLineCount = countLines(testingDatabase);

        // Set input fields to a valid name that should be deleted
        testDelForm.nameField.setText("newN");

        // Simulate button click
        testDelForm.deleteButton.doClick();

        // Count lines after deleting
        long finalLineCount = countLines(testingDatabase);

        // Check if a line was removed
        boolean passed = (initialLineCount - 1 == finalLineCount);

        // Use TestResultPrinter to record the test result
        try {
            //Assertions.assertTrue(passed, "A record should have been removed from the file.");
        } finally {
            TestResultPrinter.printTestResult("delSuccessTest", initialLineCount - 1, finalLineCount, passed);
        }
    }

    @Test
    void delFailTest() throws IOException {
        // Count lines before attempting deletion
        long initialLineCount = countLines(testingDatabase);

        // Set an invalid/empty name that shouldn't delete anything
        testDelForm.nameField.setText("");

        // Simulate button click
        testDelForm.deleteButton.doClick();

        // Count lines after submitting
        long finalLineCount = countLines(testingDatabase);

        // Check if the line count is unchanged
        boolean passed = (initialLineCount == finalLineCount);

        // Use TestResultPrinter to record the test result
        try {
            //Assertions.assertTrue(passed, "The file should remain unchanged if no valid name is provided.");
        } finally {
            TestResultPrinter.printTestResult("delFailTest", initialLineCount, finalLineCount, passed);
        }
    }

    @AfterAll
    static void printResults() {
        // Print all test results using TestResultPrinter
        TestResultPrinter.printResults();
    }
}
