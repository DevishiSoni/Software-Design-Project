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

        testAddForm.setSaveFile(FileManager.getInstance(true).getResourceFile("test.csv"));

        FileManager.getInstance(true);


    }

    private long countLines(File file) throws IOException {
        return Files.lines(file.toPath()).count();
    }

    @Test
    void newCsvLineTest() throws IOException {
        // Count lines before submitting
        File testFile = FileManager.getInstance(true).getResourceFile("test.csv");
        long initialLineCount = countLines(testFile);

        // Set input fields
        testAddForm.locationField.setText("newL");
        testAddForm.nameField.setText("newN");

        // Simulate button click
        testAddForm.submitButton.doClick();

        // Count lines after submitting
        long finalLineCount = countLines(testFile);
        // Ensure exactly one new line was added
        Assertions.assertEquals(initialLineCount + 1, finalLineCount, "A new entry should have been added to the file.");

        // Output success of add test.
        System.out.println("Test Passed: locationAddTest - The CSV file increased by one line after form submission.");
    }

    @Test
    void incompleteFormTest() throws IOException {

        File testFile = FileManager.getInstance(true).getResourceFile("test.csv");
        // Count lines after submitting
        long initialLineCount = countLines(testFile);

        // Set input fields
        testAddForm.locationField.setText("newL");

        // Simulate button click
        testAddForm.submitButton.doClick();


        // Count lines after submitting
        long finalLineCount = countLines(testFile);

        // Ensure exactly one new line was added
        Assertions.assertEquals(initialLineCount, finalLineCount, "A new entry should not have been added to the file.");

        // Output success of add test.
        System.out.println("Test Passed: location was not added to the file.");

    }

    void newHighestIDTest()
    {
        File testFile = FileManager.getInstance(true).getResourceFile("test.csv");


        int expHighestID = DatabaseManager.getMaxID(testFile) + 1; //Expect that the next ID added is one larger.


        //TODO: Add code that adds in new location! EXPECT A FAIL!



        int actHighestID = DatabaseManager.getMaxID(testFile);

        Assertions.assertEquals(expHighestID, actHighestID, "The new ID Should Have been one larger than previous highest.");

        System.out.println("Test Passed: Location was added to the file.");
    }
}
