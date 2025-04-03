package TourCatGUI.Catalog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
// Removed: import java.io.File; // No longer needed here
import java.net.URL; // Import URL for image loading

public class CatalogView {

    private final CatalogLogic logic; // Reference to the logic class
    private final String username;

    //GUI components.
    JFrame frame;
    JTable table;
    DefaultTableModel tableModel; // Model managed by logic, but GUI needs reference
    JTextField searchField;
    JButton viewButton;
    JButton returnButton;
    JButton deleteButton;
    JButton filterButton;
    JButton resetButton;
    JComboBox<String> provinceComboBox;
    JComboBox<String> typeComboBox;
    JScrollPane scrollPane;
    JPanel rightPanel;
    JPanel filterPanel;
    JPanel topPanel;
    JLabel filterBy;

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

        // ComboBox listeners
        provinceComboBox.addActionListener(e -> {
            String selection = (String) provinceComboBox.getSelectedItem();
            logic.updateSelectedProvince(selection.equals("Select Province") ? null : selection);
        });
        typeComboBox.addActionListener(e -> {
            String selection = (String) typeComboBox.getSelectedItem();
            logic.updateSelectedType(selection.equals("Select Type") ? null : selection);
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
}