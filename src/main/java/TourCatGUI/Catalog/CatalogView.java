package TourCatGUI.Catalog;

import TourCatGUI.Forms.AddFormLogic;
import TourCatSystem.DatabaseManager;
import com.opencsv.exceptions.CsvException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
// Removed: import java.io.File; // No longer needed here
import java.io.File;
import java.io.IOException;
import java.net.URL; // Import URL for image loading
import java.util.concurrent.atomic.AtomicReference;

public class CatalogView {

    private CatalogLogic logic; // Reference to the logic class
    private String username;

    // --- GUI Components ---
    JFrame frame;
    JTable table;
    DefaultTableModel tableModel; // Model managed by logic, but GUI needs reference
    JTextField searchField;
    JButton viewButton;
    JButton returnButton;
    JButton deleteButton;
    JButton filterButton;
    JButton resetButton;
    JButton editButton;
    JComboBox<String> provinceComboBox;
    JComboBox<String> typeComboBox;
    JScrollPane scrollPane;
    JPanel rightPanel;
    JPanel filterPanel;
    JPanel topPanel;
    JLabel filterBy;


    JTextField nameField, cityField, provinceField, categoryField;
    JButton saveButton, cancelButton, uploadImageButton;
    JLabel submissionReplyLabel, imagePreviewLabel, introLabel;
    //EditFormLogic editLogic;

    JDialog imgDialog = new JDialog();
    int imgClickCount = 0;

    // Constructor takes username, logic instance, and the table model
    CatalogView(String username, CatalogLogic logic, DefaultTableModel tableModel) {
        this.username = username;
        this.logic = logic;
        this.tableModel = tableModel; // Use the model created by logic

        initComponents();
        layoutComponents();
        attachListeners();

        // Initial setup
        searchField.setText("Search here:"); // Initial placeholder
        logic.hideIdColumn(table.getColumnModel()); // Ask logic to hide column
    }

    // --- Initialization Helper ---
    private void initComponents() {
        frame = new JFrame("Tour Catalog - " + username);
        table = new JTable(tableModel); // Use the provided model
        scrollPane = new JScrollPane(table);
        searchField = new JTextField();
        viewButton = new JButton("View Details");
        returnButton = new JButton("Return To Homepage");
        deleteButton = new JButton("Delete Location");
        filterButton = new JButton("Apply Filters");
        resetButton = new JButton("Reset Filters");
        editButton = new JButton("Edit Location");
        rightPanel = new JPanel();
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel = new JPanel(new BorderLayout());
        filterBy = new JLabel("Filter By:");

        // Province ComboBox Setup
        provinceComboBox = new JComboBox<>();
        provinceComboBox.addItem("Select Province");
        provinceComboBox.addItem("Ontario");
        provinceComboBox.addItem("Quebec");
        provinceComboBox.addItem("British Columbia");
        provinceComboBox.addItem("Alberta");
        provinceComboBox.addItem("Manitoba");
        provinceComboBox.addItem("Saskatchewan");
        provinceComboBox.addItem("Nova Scotia");
        provinceComboBox.addItem("New Brunswick");
        provinceComboBox.addItem("Prince Edward Island");
        provinceComboBox.addItem("Newfoundland and Labrador");
        // Add more provinces as needed

        // Type ComboBox Setup
        typeComboBox = new JComboBox<>();
        typeComboBox.addItem("Select Type");
        typeComboBox.addItem("Park");
        typeComboBox.addItem("Waterfall");
        typeComboBox.addItem("Historic Site");
        typeComboBox.addItem("Landmark");
        // Add more types as needed
    }

    // --- Layout Helper ---
    private void layoutComponents() {
        // Frame setup
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Right Panel (Buttons)
        rightPanel.setLayout(new GridLayout(5, 1, 5, 10)); // Added gaps
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
        rightPanel.add(returnButton);
        rightPanel.add(viewButton);
        rightPanel.add(deleteButton);
        rightPanel.add(editButton);

        // Filter Panel
        filterPanel.add(filterBy);
        filterPanel.add(provinceComboBox);
        filterPanel.add(typeComboBox);
        filterPanel.add(filterButton);
        filterPanel.add(resetButton);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding

        // Top Panel (Search + Filters)
        topPanel.add(searchField, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);

        // Add components to frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);

        frame.setLocationRelativeTo(null); // Center on screen
    }

    // --- Listener Setup Helper ---
    private void attachListeners() {
        // Search field placeholder text behavior
        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search here:")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search here:");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        searchField.setForeground(Color.GRAY);

        // Search key listener
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                logic.handleSearch(searchField.getText());
            }
        });

        // Button listeners
        viewButton.addActionListener(e ->
                {
                    int row = table.getSelectedRow();

                    if(row == -1) return;

                    String id = (String) tableModel.getValueAt(row, 0);
                    String name = (String) tableModel.getValueAt(row, 1);
                    String city = (String) tableModel.getValueAt(row, 2);
                    String province = (String) tableModel.getValueAt(row, 3);
                    String category = (String) tableModel.getValueAt(row, 4);

                    logic.handleViewAction(id, name, city, province, category);
                });
        returnButton.addActionListener(e -> logic.handleReturnAction());
        deleteButton.addActionListener(e -> logic.handleDeleteAction());
        filterButton.addActionListener(e -> logic.handleFilterAction());
        resetButton.addActionListener(e -> logic.handleResetAction());
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) return;
            editForm(this.tableModel,row);
        });

        // ComboBox listeners
        provinceComboBox.addActionListener(e -> {
            String selection = (String) provinceComboBox.getSelectedItem();
            logic.updateSelectedProvince(selection.equals("Select Province") ? null : selection);
        });
        typeComboBox.addActionListener(e -> {
            String selection = (String) typeComboBox.getSelectedItem();
            logic.updateSelectedType(selection.equals("Select Type") ? null : selection);
        });

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e){
                if (imgClickCount == 1) {
                    imgClickCount = 0;
                    imgDialog.dispose();
                    String id = (String) table.getModel().getValueAt(table.getSelectedRow(), 0);
                    URL imageURL = null;
                    String[] extensions = {".png", ".jpg", ".jpeg", ".gif"}; // Add more if needed
                    for (String ext : extensions) {
                        String resourcePath = "/image/" + id + ext;
                        imageURL = getClass().getResource(resourcePath);
                        if (imageURL != null) {
                            System.out.println("Found image resource: " + resourcePath);
                            break; // Found one, stop looking
                        } else {
                            System.out.println("Did not find image resource: " + resourcePath);
                        }
                    }

                    if (imageURL != null) {
                        // Make image viewable
                        popUpImg(imageURL);
                    }
                } else {
                    imgClickCount++;
                }
            }
        });
    }

    // --- Methods called by Logic to update GUI ---

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public void dispose() {
        frame.dispose();
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    public Object getValueAt(int row, int col) {
        if (row >= 0 && row < tableModel.getRowCount() && col >= 0 && col < tableModel.getColumnCount()) {
            return tableModel.getValueAt(row, col);
        }
        return null;
    }

    public void removeTableRow(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        // Logic class handles removing from the actual model
    }

    public String getSearchText() {
        String text = searchField.getText();
        return text.equals("Search here:") ? "" : text;
    }

    public void setSearchText(String text) {
        searchField.setText(text);
        if (text.isEmpty() || text.equals("Search here:")) {
            searchField.setText("Search here:");
            searchField.setForeground(Color.GRAY);
        } else {
            searchField.setForeground(Color.BLACK);
        }
    }

    public JTable getTable() {
        return table;
    }

    public void resetFilters() {
        provinceComboBox.setSelectedIndex(0);
        typeComboBox.setSelectedIndex(0);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // --- Details Popup (Modified) ---
    /**
     * Displays a popup window with location details and an image loaded from a URL.
     *
     * @param id         The location ID.
     * @param name       The location name.
     * @param city       The location city.
     * @param province   The location province.
     * @param category   The location category.
     * @param imageURL   The URL pointing to the image resource (can be null).
     */
    public void displayDetailsWindow(String id, String name, String city, String province, String category, URL imageURL) { // Changed parameter type
        JFrame detailsFrame = new JFrame("Location Details: " + name);
        detailsFrame.setSize(450, 450);
        detailsFrame.setLayout(new BorderLayout(10, 10));
        detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Panel for text details (unchanged)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(0, 1, 5, 5));
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textPanel.add(new JLabel("<html><b>Name:</b> " + name + "</html>"));
        textPanel.add(new JLabel("<html><b>City:</b> " + city + "</html>"));
        textPanel.add(new JLabel("<html><b>Province:</b> " + province + "</html>"));
        textPanel.add(new JLabel("<html><b>Category:</b> " + category + "</html>"));
        textPanel.add(new JLabel("<html><b>ID:</b> " + id + "</html>"));

        // Image Label
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Load image using URL ---
        if (imageURL != null) { // Check if URL was found by logic
            try {
                // Create ImageIcon directly from the URL
                ImageIcon icon = new ImageIcon(imageURL);

                // Check if image loaded successfully (basic check)
                if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
                    throw new Exception("Image failed to load from URL (invalid format or dimensions).");
                }

                // Scale image proportionally (unchanged logic)
                int maxWidth = 300;
                int maxHeight = 300;
                int imgWidth = icon.getIconWidth();
                int imgHeight = icon.getIconHeight();

                if (imgWidth > maxWidth || imgHeight > maxHeight) {
                    double scale = Math.min((double) maxWidth / imgWidth, (double) maxHeight / imgHeight);
                    int scaledWidth = (int) (imgWidth * scale);
                    int scaledHeight = (int) (imgHeight * scale);
                    Image scaledImage = icon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    imageLabel.setIcon(icon); // Use original size if small enough
                }
                imageLabel.setText(null); // Clear text if image is loaded

            } catch (Exception e) {
                // Catch potential errors during URL loading or ImageIcon creation
                System.err.println("Error loading image from URL: " + imageURL + " - " + e.getMessage());
                // Optionally log the stack trace: e.printStackTrace();
                imageLabel.setText("Error loading image");
                imageLabel.setIcon(null);
            }
        } else {
            // If imageURL was null (not found by logic)
            imageLabel.setText("No Image Available");
            imageLabel.setIcon(null);
        }
        // ---------------------------

        // Add components to frame
        detailsFrame.add(textPanel, BorderLayout.NORTH);
        detailsFrame.add(imageLabel, BorderLayout.CENTER);

        detailsFrame.setLocationRelativeTo(frame); // Center relative to main window
        detailsFrame.setVisible(true);
    }

    public void popUpImg(URL imageURL) {
//        imgDialog.dispose();

        imgDialog = new JDialog(frame);
        imgDialog.setSize(450, 450);
        imgDialog.setLayout(new BorderLayout(10, 10));
        imgDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            // Create ImageIcon directly from the URL
            ImageIcon icon = new ImageIcon(imageURL);

            // Check if image loaded successfully (basic check)
            if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
                throw new Exception("Image failed to load from URL (invalid format or dimensions).");
            }

            // Scale image proportionally (unchanged logic)
            int maxWidth = 300;
            int maxHeight = 300;
            int imgWidth = icon.getIconWidth();
            int imgHeight = icon.getIconHeight();

            if (imgWidth > maxWidth || imgHeight > maxHeight) {
                double scale = Math.min((double) maxWidth / imgWidth, (double) maxHeight / imgHeight);
                int scaledWidth = (int) (imgWidth * scale);
                int scaledHeight = (int) (imgHeight * scale);
                Image scaledImage = icon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                imageLabel.setIcon(icon); // Use original size if small enough
            }
            imageLabel.setText(null); // Clear text if image is loaded
            imgDialog.add(imageLabel, BorderLayout.CENTER);
            imgDialog.setVisible(true);

        } catch (Exception e) {
            // Catch potential errors during URL loading or ImageIcon creation
            System.err.println("Error loading image from URL: " + imageURL + " - " + e.getMessage());
            // Optionally log the stack trace: e.printStackTrace();
            imageLabel.setText("Error loading image");
            imageLabel.setIcon(null);
        }
    }







    // Edit Form
    public void editForm(DefaultTableModel model, int row){
        System.out.println(row);
        JFrame editFrame = new JFrame();
        editFrame.setTitle("Edit Location");
        editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose instead of Exit
        AtomicReference<File> selectedImg = new AtomicReference<>();


        introLabel = new JLabel("Enter details for the new location:");
        introLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 15));

        nameField = new JTextField((String) model.getValueAt(row, 1),25);
        cityField = new JTextField((String) model.getValueAt(row, 2),25);
        provinceField = new JTextField((String) model.getValueAt(row, 3),25);
        categoryField = new JTextField((String) model.getValueAt(row, 4),25);

        saveButton = new JButton("Save Location");
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

        String id = (String) table.getModel().getValueAt(row, 0);
        URL imageURL = null;
        String[] extensions = {".png", ".jpg", ".jpeg", ".gif"}; // Add more if needed
        for (String ext : extensions) {
            String resourcePath = "/image/" + id + ext;
            imageURL = getClass().getResource(resourcePath);
            if (imageURL != null) {
                System.out.println("Found image resource: " + resourcePath);
                break; // Found one, stop looking
            } else {
                System.out.println("Did not find image resource: " + resourcePath);
            }
        }
        if (imageURL != null) {
            File temp = new File(imageURL.getFile());
            selectedImg.set(temp);
            try {
                // Create a scaled ImageIcon for the preview (using File path is OK here)
                ImageIcon originalIcon = new ImageIcon(selectedImg.get().getAbsolutePath());
                if (originalIcon.getIconWidth() <= 0) { // Basic check if image loaded
                    throw new Exception("ImageIcon could not load image data.");
                }
                Image scaledImage = originalIcon.getImage().getScaledInstance(
                        150, 120, Image.SCALE_SMOOTH); // Adjust preview size if needed
                ImageIcon previewIcon = new ImageIcon(scaledImage);
                setImagePreview(previewIcon);
                setSubmissionReply("Image selected: " + selectedImg.get().getName(), false);
            } catch (Exception e) {
                System.err.println("Error creating image preview: " + e.getMessage());
                setImagePreview(null);
                setSubmissionReply("Error loading image preview.", true);
                selectedImg.set(null); // Invalidate on error
            }
        }

        editFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Increased insets for spacing
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.weightx = 1.0; // Allow title to expand horizontally
        editFrame.add(introLabel, gbc);
        gbc.weightx = 0; // Reset weightx for labels
        gbc.gridwidth = 1; // Reset gridwidth

        // Row 1: Landmark Name Label & Field
        gbc.gridx = 0; gbc.gridy = 1;
        editFrame.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; // Allow field to expand
        editFrame.add(nameField, gbc);
        gbc.weightx = 0; // Reset

        // Row 2: City Label & Field
        gbc.gridx = 0; gbc.gridy = 2;
        editFrame.add(new JLabel("City:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        editFrame.add(cityField, gbc);
        gbc.weightx = 0;

        // Row 3: Province Label & Field
        gbc.gridx = 0; gbc.gridy = 3;
        editFrame.add(new JLabel("Province:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        editFrame.add(provinceField, gbc);
        gbc.weightx = 0;

        // Row 4: Category Label & Field
        gbc.gridx = 0; gbc.gridy = 4;
        editFrame.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        editFrame.add(categoryField, gbc);
        gbc.weightx = 0;

        // Row 5: Image Preview
        gbc.gridx = 0; gbc.gridy = 5;
        editFrame.add(new JLabel("Image Preview:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; // Let preview take space
        gbc.fill = GridBagConstraints.NONE; // Don't stretch image label itself
        gbc.anchor = GridBagConstraints.CENTER; // Center preview
        editFrame.add(imagePreviewLabel, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reset fill
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor
        gbc.weightx = 0; // Reset weight

        // Row 6: Upload Image Button
        gbc.gridx = 1; gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE; // Don't stretch button
        gbc.anchor = GridBagConstraints.LINE_START; // Align button left within its cell
        editFrame.add(uploadImageButton, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reset fill
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor

        // Row 7: Submission Reply Label
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        editFrame.add(submissionReplyLabel, gbc);
        gbc.weightx = 0;
        gbc.gridwidth = 1;

        // Row 8: Buttons (using a sub-panel for better alignment)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Align right
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST; // Align panel right
        editFrame.add(buttonPanel, gbc);

        editFrame.setLocationRelativeTo(null); // Center on screen
        editFrame.pack(); // Adjusts size to fit components
        editFrame.setVisible(true);

        saveButton.addActionListener(e -> {
            String name = getNameText().trim();
            String city = getCityText().trim();
            String province = getProvinceText().trim();
            String category = getCategoryText().trim();

            System.out.println(row + " Flag 1");
            // 2. Validate input
            if (!isInputValid(name, city, province, category)) {
                setSubmissionReply("Validation Error: Please fill in Name, Province, and Category.", true);
                return;
            }

            // 3. Prepare data for storage
            String nextIdStr;
            try {
                nextIdStr = logic.generateNextId(); // Use the correctly initialized dbManager
            } catch (RuntimeException q) { // Catch potential errors from getMaxId/formatting
                showError("Error generating next ID: " + q.getMessage());
                q.printStackTrace();
                return;
            }

            String[] newLocationData = new String[5]; // Adjust size if more columns
            newLocationData[0] = nextIdStr;
            newLocationData[1] = name;
            newLocationData[2] = city;
            newLocationData[3] = province;
            newLocationData[4] = category;

            // 4. Attempt to add data to the CSV file (using the member dbManager)
            try {
                logic.getDatabaseManager().addRecord(newLocationData);
                // If addRecord succeeds, proceed to image saving
            } catch (IOException err) {
                showError("Error saving location data: " + err.getMessage());
                err.printStackTrace();
                // Don't proceed to image saving if data saving failed
                return; // Stop the submission process
            } catch (RuntimeException err) { // Catch other potential errors from addRecord
                showError("An unexpected error occurred saving data: " + err.getMessage());
                err.printStackTrace();
                return;
            }


            // 5. Attempt to save the image (if selected) to the *writable* image directory
            boolean imageSaveSuccess = true; // Assume success if no image selected
            if (selectedImg.get() != null) {
                imageSaveSuccess = logic.saveImageToWritableLocation(selectedImg, nextIdStr);
                if (!imageSaveSuccess) {
                    // Warn user, data is already saved. Cannot easily roll back CSV add.
                    setSubmissionReply("Warning: Location data saved, but failed to save image file.", true);
                    // Don't clear form, allow user to retry or cancel maybe?
                }
            }

            String selectedRowID = tableModel.getValueAt(row, 0).toString();

            try {
                // DatabaseManager needs to use the writable file
                // It should ideally be an instance variable or re-created safely

                // DatabaseManager databaseManager = new DatabaseManager(writableDatabaseFile); // Pass the correct file
                logic.getDatabaseManager().deleteById(tableModel.getValueAt(row, 0).toString());

                // If deleteById throws no exception, assume success
                //System.out.println(tableModel.getRowCount());
                model.removeRow(row); // Update the view
                //logic.updateTableModel(logic.readAllDataFromWritableFile());
                //tableModel.fireTableDataChanged();

            } catch (DatabaseManager.RecordNotFoundException err) {
                showError("Could not edit: Record not found (ID: " + selectedRowID + ")");
            } catch (IOException | CsvException err) {
                showError("Error overwriting location from database: " + err.getMessage());
                err.printStackTrace(); // Log for debugging
            } catch (RuntimeException err) { // Catch unexpected runtime errors
                showError("An unexpected error occurred during overwriting: " + err.getMessage());
                err.printStackTrace();
            }

            // 6. Final success handling (if data saved and image save was successful or not needed)
            if (imageSaveSuccess) {
                assert selectedImg != null;
                selectedImg.set(null); // Reset selected image state
                editFrame.dispose(); // Close
            }
        });
        cancelButton.addActionListener(e -> editFrame.dispose());
        uploadImageButton.addActionListener(e -> {
            File file = showImageFileChooser();
            if (file != null) {
                // Check file existence and readability before proceeding
                if (!file.exists() || !file.canRead()) {
                    showError("Cannot read selected image file: " + file.getName());
                    setImagePreview(null);
                    return;
                }

                assert selectedImg != null;
                selectedImg.set(file);
                try {
                    // Create a scaled ImageIcon for the preview (using File path is OK here)
                    ImageIcon originalIcon = new ImageIcon(selectedImg.get().getAbsolutePath());
                    if (originalIcon.getIconWidth() <= 0) { // Basic check if image loaded
                        throw new Exception("ImageIcon could not load image data.");
                    }
                    Image scaledImage = originalIcon.getImage().getScaledInstance(
                            150, 120, Image.SCALE_SMOOTH); // Adjust preview size if needed
                    ImageIcon previewIcon = new ImageIcon(scaledImage);
                    setImagePreview(previewIcon);
                    setSubmissionReply("Image selected: " + file.getName(), false);
                } catch (Exception a) {
                    System.err.println("Error creating image preview: " + a.getMessage());
                    setImagePreview(null);
                    setSubmissionReply("Error loading image preview.", true);
                    selectedImg.set(null); // Invalidate on error
                }
            } else {
                setSubmissionReply("Image selection cancelled.", false);
            }
        });
    }

    public File showImageFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose an image");
        // Filter for common image types
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Images (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false); // Only allow specified image types

        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null; // User cancelled or closed dialog
    }

    public void setImagePreview(ImageIcon icon) {
        imagePreviewLabel.setIcon(icon);
        if (icon == null) {
            imagePreviewLabel.setText("No Image Selected");
        } else {
            imagePreviewLabel.setText(null); // Remove text when image is present
        }
    }

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

    public boolean isInputValid (String name, String city, String province, String category) {
        return name != null && !name.isBlank() &&
                province != null && !province.isBlank() &&
                category != null && !category.isBlank();
    }
}