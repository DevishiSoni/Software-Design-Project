import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class DeleteForm extends JFrame {

    public JTextField nameField;
    public JButton deleteButton;
    public JButton cancelButton;

    public DeleteForm(String username) {
        setTitle("Delete Form");
        setSize(500, 185);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        nameField = new JTextField(10);
        deleteButton = new JButton("Delete");
        cancelButton = new JButton("Cancel");

        JLabel intro = new JLabel("Please enter the name of the landmark to be deleted");
        intro.setFont(new Font("Trebuchet MS", Font.BOLD, 15));

        JLabel submissionReplyLabel = new JLabel(" ");
        submissionReplyLabel.setFont(new Font("Trebuchet MS", Font.ITALIC, 15));

        add(intro);
        add(new JLabel("Landmark name:"));
        add(nameField);
        add(submissionReplyLabel);
        add(deleteButton);
        add(cancelButton);

        deleteButton.addActionListener(e->{
            String filePath = new File("").getAbsolutePath();
            filePath += "/geonames.csv";

            String name = nameField.getText();

            if (!name.isBlank()) {
                boolean success = ChangeDatabase.deleteFromFile(name,filePath);

                if (success) {
                    submissionReplyLabel.setText("Location successfully deleted from the database");
                    nameField.setText("");
                }
                else {
                    submissionReplyLabel.setText("Failed to delete location from the database");
                }
            }
            else {
                submissionReplyLabel.setText("Please enter the name of the landmark to be deleted");
            }
        });

        cancelButton.addActionListener(e->{
            new HomePage(username);
            dispose();
        });
    }
}
