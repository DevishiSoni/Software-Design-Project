package TourCatGUI;

import TourCatSystem.DatabaseManager;
import TourCatSystem.FileManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class AddForm extends JFrame {

    public JTextField nameField,cityField, provinceField, categoryField;
    public JButton submitButton, cancelButton, uploadImageButton;
    public JLabel submissionReplyLabel, imagePreviewLabel;
    private String imagePath = null;

    File saveFile;

    public AddForm(String username) {
        saveFile = FileManager.getInstance().getResourceFile("test.csv");

        setTitle("Add Form");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel intro = new JLabel("Please enter the name and location of the landmark to be added");
        intro.setFont(new Font("Trebuchet MS", Font.BOLD, 15));

        nameField = new JTextField(20);
        cityField = new JTextField();
        provinceField = new JTextField();
        categoryField = new JTextField();
        submitButton = new JButton("Submit");
        cancelButton = new JButton("Cancel");
        uploadImageButton = new JButton("Choose Image");

        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(150, 120));
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        submissionReplyLabel = new JLabel("");
        submissionReplyLabel.setFont(new Font("Trebuchet MS", Font.ITALIC, 12));

        // Row 0: Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(intro, gbc);

        // Row 1: Landmark Name Label & Field
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        add(new JLabel("Landmark name:"), gbc);
        gbc.gridx = 1;
        add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        add(new JLabel("Landmark City:"), gbc);
        gbc.gridx = 1;
        add(cityField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        add(new JLabel("Landmark Province:"), gbc);
        gbc.gridx = 1;
        add(provinceField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        add(new JLabel("Landmark Category:"), gbc);
        gbc.gridx = 1;
        add(categoryField, gbc);



        // Row 3: Image Label & Preview
        gbc.gridx = 0; gbc.gridy = 5;
        add(new JLabel("Selected Image:"), gbc);
        gbc.gridx = 1;
        add(imagePreviewLabel, gbc);

        // Row 4: Upload Image Button
        gbc.gridx = 1; gbc.gridy = 6;
        add(uploadImageButton, gbc);

        // Row 5: Submission Reply Label
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        add(submissionReplyLabel, gbc);

        // Row 6: Submit & Cancel Buttons
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        add(submitButton, gbc);
        gbc.gridx = 1;
        add(cancelButton, gbc);

        // Button Actions
        uploadImageButton.addActionListener(e -> selectImage());
        submitButton.addActionListener(e -> addFieldsToFile(saveFile));
        cancelButton.addActionListener(e -> {
            new HomePage(username);
            dispose();
        });

        setVisible(true);
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose an image");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Define the relative folder (inside src or another location)
            File destinationFolder = new File("src/Resources/images");

            // Define the destination file inside the project folder
            File destinationFile = new File(destinationFolder, file.getName());

            try {
                // Copy the file to the project folder
                java.nio.file.Files.copy(file.toPath(), destinationFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // Save the relative path (relative to src)
                imagePath = "src/Resources/images/" + file.getName();

                // Show preview
                ImageIcon icon = new ImageIcon(new ImageIcon(destinationFile.getAbsolutePath())
                        .getImage().getScaledInstance(150, 120, Image.SCALE_SMOOTH));
                imagePreviewLabel.setIcon(icon);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving image.");
            }
        }
    }


    private void addFieldsToFile(File f) {
        String name = nameField.getText();
        String city = cityField.getText();
        String province = provinceField.getText();
        String category = categoryField.getText();


        if (!isNameLocationValid()) {
            submissionReplyLabel.setText("Please enter all fields!");
            return;
        }

        ArrayList<String> newLandmark = new ArrayList<>();
        newLandmark.add(name);
        newLandmark.add(city);
        newLandmark.add(province);
        newLandmark.add(category);
        // If an image is selected, add its path; otherwise, save "No Image"
        newLandmark.add(imagePath != null ? imagePath : "No Image");
        boolean success = DatabaseManager.addToFile(newLandmark, f);
        submissionReplyLabel.setText(success ? "Location successfully added!" : "Failed to add location.");

        if (success) {
            nameField.setText("");
            cityField.setText("");
            provinceField.setText("");
            categoryField.setText("");
            // Clear image preview
            imagePreviewLabel.setIcon(null);
            imagePath = null; // Reset image path
        }
    }

    boolean isNameLocationValid() {
        return !nameField.getText().isBlank() && !cityField.getText().isBlank() && !provinceField.getText().isBlank() && !categoryField.getText().isBlank();
    }

    public static void main(String[] args) {
        new AddForm("tester");
    }
}
