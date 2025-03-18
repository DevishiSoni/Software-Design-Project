import TourCatGUI.DeleteForm;
import TourCatSystem.FileManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class DeleteFormTest {

    static DeleteForm testDelForm;

    static File testingDatabase;

    @BeforeAll
    public static void setup() {
        testDelForm = new DeleteForm("test");
        testingDatabase = FileManager.getInstance(true).getResourceFile("test.csv");

        testDelForm.setDatabaseFile(testingDatabase);
    }

    private long countLines(File file) throws IOException {
        return Files.lines(Paths.get(file.toURI())).count();
    }

    @Test
    void delSuccessTest() throws IOException {
        // Count lines before submitting
        long initialLineCount = countLines(testingDatabase);

        // Set input fields
        testDelForm.nameField.setText("newN");

        // Simulate button click
        testDelForm.deleteButton.doClick();

        // Count lines after submitting
        long finalLineCount = countLines(FileManager.getInstance(true).getResourceFile("test.csv"));

        // Ensure exactly one new line was added
        Assertions.assertEquals(initialLineCount - 1, finalLineCount, "A new entry should have been removed from the file.");

        // Output success of add test.
        System.out.println("Test Passed: locationAddTest - The CSV file decreased.");
    }


    @Test
    void delFailTest() throws IOException {
        // Count lines before submitting
        long initialLineCount = countLines(testingDatabase);


        // Set input fields
        testDelForm.nameField.setText("");

        // Simulate button click
        testDelForm.deleteButton.doClick();

        // Count lines after submitting
        long finalLineCount = countLines(testingDatabase);

        // Ensure exactly one new line was added
        Assertions.assertEquals(initialLineCount, finalLineCount, "A new entry should have been added to the file.");

        // Output success of add test.
        System.out.println("Test Passed: locationAddTest - The CSV file increased by one line after form submission.");
    }
}
