package TourCatGUI;

import TourCatSystem.ChangeDatabase;
import TourCatSystem.FileManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class AddForm extends JFrame {

    public JTextField nameField;
    public JTextField locationField;
    public JButton submitButton, cancelButton;
    public JLabel submissionReplyLabel;

    private String saveFileName = File.separator + "geonames.csv";

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

        submissionReplyLabel = new JLabel("");
        submissionReplyLabel.setFont(new Font("Trebuchet MS", Font.ITALIC, 15));

        add(intro);
        add(new JLabel("Landmark name:"));
        add(nameField);
        add(new JLabel("Landmark location:"));
        add(locationField);
        add(submissionReplyLabel);
        add(submitButton);
        add(cancelButton);

        submitButton.addActionListener(e ->
        {
            File filePath = FileManager.getInstance().getResourceFile("test.csv");
            addFieldsToFile(filePath);
        });

        cancelButton.addActionListener(e -> {
            new HomePage(username);
            dispose();
        });
    }

    private void addFieldsToFile(File f) {

        String name = nameField.getText();
        String location = locationField.getText();

        if (!isNameLocationValid()){
            submissionReplyLabel.setText("Please enter the name and location of the landmark to be added");
            return;
        }

        ArrayList<String> newLandmark = new ArrayList<String>();
        newLandmark.add(name);
        newLandmark.add(location);

        boolean success = ChangeDatabase.addToFile(newLandmark, f.getAbsolutePath());

        if (!success) {
            submissionReplyLabel.setText("Failed to add location to the database");
            return;
        }


        submissionReplyLabel.setText("Location successfully added to the database");
        nameField.setText("");
        locationField.setText("");

    }

    boolean isNameLocationValid()
    {
        return !nameField.getText().isBlank() && !locationField.getText().isBlank();
    }

    public String getCSFilename() {
        return saveFileName;
    }

    public void setFilepath(String filepath) {
        this.saveFileName = filepath;
    }

    public String getSaveFileAbsPath(){
        return FileManager.getInstance().getResourceDirectoryPath() + File.separator + this.getCSFilename();
    }

    public static void main(String[] args) {
        AddForm af = new AddForm("tester");
        af.setFilepath(File.separator + "testnames.csv");
        af.setVisible(true);
    }
}
