import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DeleteFormTest {

    private DeleteForm deleteForm;

    @Mock
    private ChangeDatabase changeDatabaseMock;  // Mock ChangeDatabase class

    @Mock
    private JLabel submissionReplyLabel;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Create the DeleteForm instance
        deleteForm = new DeleteForm("testUser");

        // Inject mock submissionReplyLabel into DeleteForm
        deleteForm.setSubmissionReplyLabel(submissionReplyLabel);
    }

    @Test
    void testDeleteButton_withValidName() {
        // Set the text for the landmark name
        deleteForm.nameField.setText("Eiffel Tower");

        // Mock the behavior of ChangeDatabase.deleteFromFile (simulate success)
        when(ChangeDatabase.deleteFromFile(anyString(), anyString())).thenReturn(true);

        // Simulate clicking the delete button
        deleteForm.deleteButton.doClick();

        // Verify that the label text was updated to reflect the success
        verify(submissionReplyLabel).setText("Location successfully deleted from the database");

        // Verify that the name field is cleared
        assertEquals("", deleteForm.nameField.getText(), "The name field should be cleared after deletion");
    }

    @Test
    void testDeleteButton_withInvalidName() {
        // Set the name field to empty (invalid name)
        deleteForm.nameField.setText("");

        // Simulate clicking the delete button
        deleteForm.deleteButton.doClick();

        // Verify that the label text was updated to ask for a valid landmark name
        verify(submissionReplyLabel).setText("Please enter the name of the landmark to be deleted");
    }

    @Test
    void testDeleteButton_whenDeletionFails() {
        // Set the name field with a fake landmark
        deleteForm.nameField.setText("Fake Landmark");

        // Mock the behavior of ChangeDatabase.deleteFromFile (simulate failure)
        when(ChangeDatabase.deleteFromFile(anyString(), anyString())).thenReturn(false);

        // Simulate clicking the delete button
        deleteForm.deleteButton.doClick();

        // Verify that the label text was updated to reflect the failure
        verify(submissionReplyLabel).setText("Failed to delete location from the database");
    }

    @Test
    void testCancelButton() {
        // Simulate clicking the cancel button
        deleteForm.cancelButton.doClick();

        // Verify that the form was closed
        verify(deleteForm).dispose();
    }
    public static void main(String[] args) {

    }
}
