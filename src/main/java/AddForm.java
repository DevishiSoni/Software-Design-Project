import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class AddForm extends JFrame {

    public JTextField nameField;
    public JTextField locationField;
    public JButton submitButton;
    public JButton cancelButton;

    public AddForm(String username) {
        setTitle("Add Form");
        setSize(500, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        nameField = new JTextField();
        locationField = new JTextField();
        submitButton = new JButton("Submit");
        cancelButton = new JButton("Cancel");

        JLabel intro = new JLabel("Please enter the name and location of the landmark to be added");
        intro.setFont(new Font("Trebuchet MS", Font.BOLD, 15));

        JLabel submissionReplyLabel = new JLabel(" ");
        submissionReplyLabel.setFont(new Font("Trebuchet MS", Font.ITALIC, 15));

        add(intro);
        add(new JLabel("Landmark name:"));
        add(nameField);
        add(new JLabel("Landmark location:"));
        add(locationField);
        add(submissionReplyLabel);
        add(submitButton);
        add(cancelButton);

        submitButton.addActionListener(e->{
            String filePath = new File("").getAbsolutePath();
            filePath += "/geonames.csv";

            String name = nameField.getText();
            String location = locationField.getText();

            if (!name.isBlank() && !location.isBlank()) {

                ArrayList<String> newLandmark = new ArrayList<String>();
                newLandmark.add(name);
                newLandmark.add(location);

                boolean success = ChangeDatabase.addToFile(newLandmark, filePath);

                if (success) {
                    submissionReplyLabel.setText("Location successfully added to the database");
                    nameField.setText("");
                    locationField.setText("");
                }
                else {
                    submissionReplyLabel.setText("Failed to add location to the database");
                }
            }
            else {
                submissionReplyLabel.setText("Please enter the name and location of the landmark to be added");
            }
        });

        cancelButton.addActionListener(e->{
            new HomePage(username);
            dispose();
        });
    }
}
