import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class AddFormTest {

    static AddForm testAddForm;

    @BeforeAll
    public static void setup() {
        testAddForm = new AddForm("test");
        testAddForm.setFilepath("/testnames.csv");
    }

    private long countLines(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath)).count();
    }

    @Test
    void newCsvLineTest() throws IOException {
        // Count lines before submitting
        long initialLineCount = countLines(testAddForm.getSaveFileAbsPath());

        // Set input fields
        testAddForm.locationField.setText("newL");
        testAddForm.nameField.setText("newN");

        // Simulate button click
        testAddForm.submitButton.doClick();

        // Count lines after submitting
        long finalLineCount = countLines(testAddForm.getSaveFileAbsPath());

        // Ensure exactly one new line was added
        Assertions.assertEquals(initialLineCount + 1, finalLineCount, "A new entry should have been added to the file.");

        // Output success of add test.
        System.out.println("Test Passed: locationAddTest - The CSV file increased by one line after form submission.");
    }
}
