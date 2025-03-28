package TourCatGUI.Forms;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class AddFormGUI extends JFrame {

    // Reference to the logic class
    private final AddFormLogic logic;
    private final String username; // Keep username if needed for cancel action

    // --- GUI Components ---
    JTextField nameField, cityField, provinceField, categoryField;
    JButton submitButton, cancelButton, uploadImageButton;
    JLabel submissionReplyLabel, imagePreviewLabel, introLabel;

    // Constructor takes username and logic instance
    public AddFormGUI(String username, AddFormLogic logic) {
        this.username = username;
        this.logic = logic;

        initComponents();
        layoutComponents();
        attachListeners();

        // Frame setup
        setTitle("Add New Location");
        // setSize(500, 500); // Let pack() determine size initially
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose instead of Exit
        setLocationRelativeTo(null); // Center on screen
        pack(); // Adjusts size to fit components
    }

    // --- Initialization Helper ---
    private void initComponents() {
        introLabel = new JLabel("Enter details for the new location:");
        introLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 15));

        nameField = new JTextField(25); // Slightly wider fields
        cityField = new JTextField(25);
        provinceField = new JTextField(25);
        categoryField = new JTextField(25);

        submitButton = new JButton("Submit Location");
        cancelButton = new JButton("Cancel");
        uploadImageButton = new JButton("Choose Image...");

        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(150, 120));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setBorder(BorderFactory.createEtchedBorder()); // Use EtchedBorder
        imagePreviewLabel.setText("No Image Selected");

        submissionReplyLabel = new JLabel(" "); // Start with a space for layout stability
        submissionReplyLabel.setFont(new Font("Trebuchet MS", Font.ITALIC, 12));
        submissionReplyLabel.setForeground(Color.GRAY); // Default color
    }

    // --- Layout Helper ---
    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Increased insets for spacing
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.weightx = 1.0; // Allow title to expand horizontally
        add(introLabel, gbc);
        gbc.weightx = 0; // Reset weightx for labels
        gbc.gridwidth = 1; // Reset gridwidth

        // Row 1: Landmark Name Label & Field
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; // Allow field to expand
        add(nameField, gbc);
        gbc.weightx = 0; // Reset

        // Row 2: City Label & Field
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("City:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        add(cityField, gbc);
        gbc.weightx = 0;

        // Row 3: Province Label & Field
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Province:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        add(provinceField, gbc);
        gbc.weightx = 0;

        // Row 4: Category Label & Field
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        add(categoryField, gbc);
        gbc.weightx = 0;

        // Row 5: Image Preview
        gbc.gridx = 0; gbc.gridy = 5;
        add(new JLabel("Image Preview:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; // Let preview take space
        gbc.fill = GridBagConstraints.NONE; // Don't stretch image label itself
        gbc.anchor = GridBagConstraints.CENTER; // Center preview
        add(imagePreviewLabel, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reset fill
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor
        gbc.weightx = 0; // Reset weight

        // Row 6: Upload Image Button
        gbc.gridx = 1; gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE; // Don't stretch button
        gbc.anchor = GridBagConstraints.LINE_START; // Align button left within its cell
        add(uploadImageButton, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reset fill
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor

        // Row 7: Submission Reply Label
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(submissionReplyLabel, gbc);
        gbc.weightx = 0;
        gbc.gridwidth = 1;

        // Row 8: Buttons (using a sub-panel for better alignment)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Align right
        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST; // Align panel right
        add(buttonPanel, gbc);
    }

    // --- Listener Setup Helper ---
    private void attachListeners() {
        // Delegate actions to the logic class
        submitButton.addActionListener(e -> logic.handleSubmitAction());
        cancelButton.addActionListener(e -> logic.handleCancelAction());
        uploadImageButton.addActionListener(e -> logic.handleUploadImageAction());
    }

    // --- Methods Called by Logic to Update GUI ---

    /**
     * Displays a file chooser for image selection.
     * @return The selected File, or null if none was selected.
     */
    public File showImageFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose an image");
        // Filter for common image types
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Images (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false); // Only allow specified image types

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null; // User cancelled or closed dialog
    }

    /**
     * Sets the image preview label.
     * @param icon The ImageIcon to display (should be appropriately scaled), or null to clear.
     */
    public void setImagePreview(ImageIcon icon) {
        imagePreviewLabel.setIcon(icon);
        if (icon == null) {
            imagePreviewLabel.setText("No Image Selected");
        } else {
            imagePreviewLabel.setText(null); // Remove text when image is present
        }
    }

    /**
     * Updates the submission status label.
     * @param message The message to display.
     * @param isError True if the message represents an error (sets text color to red), false otherwise (green for success, gray for info).
     */
    public void setSubmissionReply(String message, boolean isError) {
        submissionReplyLabel.setText(message);
        if (isError) {
            submissionReplyLabel.setForeground(Color.RED);
        } else if (message.toLowerCase().contains("success")) {
            submissionReplyLabel.setForeground(new Color(0, 128, 0)); // Dark Green
        } else {
            submissionReplyLabel.setForeground(Color.GRAY); // Default informational
        }
    }

    /** Clears all input fields and the image preview. */
    public void clearForm() {
        nameField.setText("");
        cityField.setText("");
        provinceField.setText("");
        categoryField.setText("");
        setImagePreview(null); // Clear image preview
        setSubmissionReply(" ", false); // Reset reply label
    }

    // --- Getters for Logic ---
    public String getNameText() { return nameField.getText(); }
    public String getCityText() { return cityField.getText(); }
    public String getProvinceText() { return provinceField.getText(); }
    public String getCategoryText() { return categoryField.getText(); }

}