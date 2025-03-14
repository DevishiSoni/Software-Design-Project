import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class DeleteFormTest {

    static DeleteForm testDelForm;

    @BeforeAll
    public static void setup() {
        testDelForm = new DeleteForm("test");
        testDelForm.setFilename("testnames.csv");
    }

    private long countLines(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath)).count();
    }

    @Test
    void delSuccessTest() throws IOException {
        // Count lines before submitting
        long initialLineCount = countLines(testDelForm.getAbsCSVPath());



        // Set input fields
        testDelForm.nameField.setText("newN");

        // Simulate button click
        testDelForm.deleteButton.doClick();

        // Count lines after submitting
        long finalLineCount = countLines(testDelForm.getAbsCSVPath());

        // Ensure exactly one new line was added
        Assertions.assertEquals(initialLineCount - 1, finalLineCount, "A new entry should have been added to the file.");

        // Output success of add test.
        System.out.println("Test Passed: locationAddTest - The CSV file increased by one line after form submission.");
    }


    @Test
    void delFailTest() throws IOException {
        // Count lines before submitting
        long initialLineCount = countLines(testDelForm.getAbsCSVPath());



        // Set input fields
        testDelForm.nameField.setText("");

        // Simulate button click
        testDelForm.deleteButton.doClick();

        // Count lines after submitting
        long finalLineCount = countLines(testDelForm.getAbsCSVPath());

        // Ensure exactly one new line was added
        Assertions.assertEquals(initialLineCount, finalLineCount, "A new entry should have been added to the file.");

        // Output success of add test.
        System.out.println("Test Passed: locationAddTest - The CSV file increased by one line after form submission.");
    }
}
