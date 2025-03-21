import TourCatGUI.AddForm;
import TourCatSystem.DatabaseManager;
import TourCatSystem.FileManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class AddFormTest {

    static AddForm testAddForm;
    static File testFile;

    @BeforeAll
    public static void setup() {
        testAddForm = new AddForm("test");
        testAddForm.saveFile = FileManager.getInstance(true).getResourceFile("testDB.csv");
        testFile = testAddForm.saveFile;

        FileManager.getInstance(true);
    }

    private long countLines(File file) throws IOException {
        return Files.lines(file.toPath()).count();
    }

    @Test
    void newCsvLineTest() throws IOException {
        File testFile = testAddForm.saveFile;
        long initialLineCount = countLines(testFile);

        testAddForm.nameField.setText("newN");
        testAddForm.submitButton.doClick();

        long finalLineCount = countLines(testFile);
        boolean passed = (initialLineCount + 1 == finalLineCount);

        // Use the new class to print results
        try {
            Assertions.assertTrue(passed, "testDB.csv should be one line longer.");
        } finally {
            TestResultPrinter.printTestResult("newCsvLineTest", initialLineCount + 1, finalLineCount, passed);
        }
    }

    @Test
    void incompleteFormTest() throws IOException {
        File testFile = testAddForm.saveFile;
        long initialLineCount = countLines(testFile);

        testAddForm.nameField.setText(""); // No name provided
        testAddForm.submitButton.doClick();

        long finalLineCount = countLines(testFile);
        boolean passed = (initialLineCount == finalLineCount);

        // Use the new class to print results
        try {
            Assertions.assertTrue(passed, "testDB.csv should be the same length.");
        } finally {
            TestResultPrinter.printTestResult("incompleteFormTest", initialLineCount, finalLineCount, passed);
        }
    }

    @Test
    void newHighestIDTest() {
        File testFile = testAddForm.saveFile;
        int expectedHighestID = DatabaseManager.getMaxId(testFile) + 1;

        testAddForm.nameField.setText("newLandmark");
        testAddForm.submitButton.doClick();

        int actualHighestID = DatabaseManager.getMaxId(testFile);
        boolean passed = (expectedHighestID == actualHighestID);

        // Use the new class to print results
        try {
            Assertions.assertTrue(passed, "The highest ID should be one larger than the previous.");
        } finally {
            TestResultPrinter.printTestResult("newHighestIDTest", expectedHighestID, actualHighestID, passed);
        }
    }

    @AfterAll
    static void printResults() {
        TestResultPrinter.printResults(); // Print results using the new class
    }
}
