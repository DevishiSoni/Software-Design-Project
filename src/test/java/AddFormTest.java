import TourCatGUI.AddForm;
import TourCatSystem.DatabaseManager;
import TourCatSystem.FileManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class AddFormTest {

    static AddForm testAddForm;

    @BeforeAll
    public static void setup() {
        testAddForm = new AddForm("test");

        testAddForm.saveFile = FileManager.getInstance(true).getResourceFile("test.csv");


        FileManager.getInstance(true);


    }

    private long countLines(File file) throws IOException {
        return Files.lines(file.toPath()).count();
    }

    @Test
    void newCsvLineTest() throws IOException {
        File testFile = testAddForm.saveFile;
        long initialLineCount = countLines(testFile);

        testAddForm.locationField.setText("newL");
        testAddForm.nameField.setText("newN");

        testAddForm.submitButton.doClick();

        long finalLineCount = countLines(testFile);
        boolean passed = (initialLineCount + 1 == finalLineCount);

        printTestResult("newCsvLineTest", initialLineCount, finalLineCount, passed);
    }

    @Test
    void incompleteFormTest() throws IOException {
        File testFile = testAddForm.saveFile;
        long initialLineCount = countLines(testFile);

        testAddForm.locationField.setText("newL");
        testAddForm.submitButton.doClick();

        long finalLineCount = countLines(testFile);
        boolean passed = (initialLineCount == finalLineCount);

        printTestResult("incompleteFormTest", initialLineCount, finalLineCount, passed);
    }

    @Test
    void newHighestIDTest() {
        File testFile = testAddForm.saveFile;
        int expHighestID = DatabaseManager.getMaxID(testFile) + 1;

        // TODO: Add actual location submission logic
        // testAddForm.locationField.setText("...");
        // testAddForm.submitButton.doClick();

        int actHighestID = DatabaseManager.getMaxID(testFile);
        boolean passed = (expHighestID == actHighestID);

        printTestResult("newHighestIDTest", expHighestID, actHighestID, passed);
    }

    private void printTestResult(String testName, Object expected, Object actual, boolean passed) {
        System.out.printf("| %-20s | %-10s | %-10s | %-8s |\n", testName, expected, actual, passed ? "✅ PASS" : "❌ FAIL");
    }


}
