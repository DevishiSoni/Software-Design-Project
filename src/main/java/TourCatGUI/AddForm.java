package TourCatGUI;

import TourCatData.LocationData;
import TourCatService.LocationService;
import TourCatData.DatabaseManager;
import TourCatData.FileManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class AddForm extends JFrame {

    public JTextField nameField, cityField, provinceField, categoryField;
    public JButton submitButton, cancelButton, uploadImageButton;
    public JLabel submissionReplyLabel, imagePreviewLabel;
    private File imageDestination = null;
    private File selectedImage = null;

    LocationService locationService = null;

    public File saveFile;

    public AddForm(String username, LocationService locationService) {
        saveFile = FileManager.getInstance().getDatabaseFile();

        this.locationService = locationService;

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
        cityField = new JTextField(20);
        provinceField = new JTextField(20);
        categoryField = new JTextField(20);
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

        // Row 2: Landmark Location Label & Field
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Landmark city:"), gbc);
        gbc.gridx = 1;
        add(cityField, gbc);

        // Row 2: Landmark Location Label & Field
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Landmark province:"), gbc);
        gbc.gridx = 1;
        add(provinceField, gbc);

        // Row 2: Landmark Location Label & Field
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Landmark category:"), gbc);
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
        submitButton.addActionListener(e -> {
            submitForm();
        });
        cancelButton.addActionListener(e -> {
            new HomePage(username, locationService);
            dispose();
        });

        setVisible(true);
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose an image");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            selectedImage = file;


            ImageIcon icon = new ImageIcon(new ImageIcon(selectedImage.getAbsolutePath())
                .getImage().getScaledInstance(150, 120, Image.SCALE_SMOOTH));
            imagePreviewLabel.setIcon(icon);
        }

        System.out.println("Selected Image:");
        System.out.println(selectedImage.getAbsolutePath());
    }

    private void addImageToResourceFolder(File image) {
        // Define the relative folder (inside src or another location)
        File destinationFolder = FileManager.getInstance().getResourceFile("image");
    }

    private void submitForm() {

        String name = nameField.getText();
        String location = cityField.getText();
        String province = provinceField.getText();
        String category = categoryField.getText();

        if (!isInputValid()){
            submissionReplyLabel.setText("Please enter the requested information of the landmark");
            return;
        }


        LocationData newLocationData = locationService.addLocation(name, location, province, category, this.selectedImage);

        if(selectedImage != null) addImageToResourceFolder(selectedImage);

        if (newLocationData == null) {
            submissionReplyLabel.setText("Failed to add location to the database");
            return;
        }

        submissionReplyLabel.setText("Location successfully added to the database");
        nameField.setText("");
        cityField.setText("");
        provinceField.setText("");
        categoryField.setText("");
        imagePreviewLabel.setIcon(null);
        selectedImage = null;
    }

    boolean isInputValid() {
        return !nameField.getText().isBlank() && !provinceField.getText().isBlank() && !categoryField.getText().isBlank();
    }

    public static void main(String[] args) {


        FileManager fileManager = FileManager.getInstance(true);
        DatabaseManager databaseManager = new DatabaseManager();

        LocationService locationService = new LocationService(databaseManager, fileManager);

        new AddForm("tester", locationService);
    }
}
