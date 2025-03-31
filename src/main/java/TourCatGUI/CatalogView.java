package TourCatGUI;

import TourCatData.LocationData;
import TourCatService.LocationService;
import TourCatSystem.LocationReader; // Keep for hideColumns helper

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter; // Import the sorter
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList; // Needed for RowFilter list
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException; // For regex errors

// Import Levenshtein distance if you want true fuzzy filtering in RowFilter
import org.apache.commons.text.similarity.LevenshteinDistance;


public class CatalogView {

    //Ui componentes.
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<TableModel> sorter; // Add the sorter
    private JTextField searchField;
    private JButton viewButton;
    private JButton returnButton;
    private JButton deleteButton;
    private JButton filterButton; // Can likely remove this now
    private JButton resetButton;
    private JComboBox<String> provinceComboBox;
    private JComboBox<String> typeComboBox;

    // --- Service Layer ---
    private final LocationService locationService;

    // --- State ---
    // No longer need selectedProvince/Type state here if reading directly from combo boxes
    private final String username;

    // Levenshtein distance instance for fuzzy filter
    private static final LevenshteinDistance levenshteinDistance = new LevenshteinDistance(2); // Allow distance up to 2

    public CatalogView(String username, LocationService locationService) {
        if (locationService == null) {
            throw new IllegalArgumentException("LocationService cannot be null");
        }
        this.locationService = locationService;
        this.username = username;

        initComponents();
        layoutComponents();
        loadInitialData();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initComponents() {
        frame = new JFrame("TourCat Locations");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        String[] columnNames = {"ID", "Name", "City", "Province", "Category"};
        tableModel = new DefaultTableModel(columnNames, 0);

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- Setup TableRowSorter ---
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter); // Attach sorter to the table

        hideTableColumns(new int[]{0});

        searchField = new JTextField();
        searchField.setToolTipText("Type to search (fuzzy match)...");
        setupSearchFieldPlaceholder(); // Use helper method

        // Key listener for live filtering using the sorter
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyCombinedFilters(); // Apply filters on key release
            }
        });

        viewButton = new JButton("View Details");
        returnButton = new JButton("Return To Homepage");
        deleteButton = new JButton("Delete Selected");
        // filterButton = new JButton("Apply Filters"); // No longer needed if combo boxes filter live
        resetButton = new JButton("Reset Filters/Search");

        provinceComboBox = createProvinceComboBox();
        typeComboBox = createTypeComboBox();

        // Add Action Listeners (Listeners for ComboBoxes now trigger filtering)
        addActionListeners();
    }

    private void setupSearchFieldPlaceholder() {
        final String placeholder = "Search name, city, category...";
        searchField.setForeground(Color.GRAY);
        searchField.setText(placeholder);

        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText(placeholder);
                }
            }
        });
    }


    private void hideTableColumns(int[] columnsToHide) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int colIndex : columnsToHide) {
            if (colIndex >= 0 && colIndex < columnModel.getColumnCount()) {
                // Using the existing static helper method from LocationReader
                LocationReader.hideColumns(columnModel, new int[]{colIndex});
            } else {
                System.err.println("Warning: Cannot hide column index " + colIndex + ". Index out of bounds.");
            }
        }
    }

    private JComboBox<String> createProvinceComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.addItem("All Provinces"); // Changed default text
        String[] provinces = {"Ontario", "Quebec", "British Columbia", "Alberta", "Manitoba", "Saskatchewan", "Nova Scotia", "New Brunswick", "Prince Edward Island", "Newfoundland and Labrador"};
        for (String p : provinces) {
            comboBox.addItem(p);
        }
        // Add listener to trigger filtering when selection changes
        comboBox.addActionListener(e -> applyCombinedFilters());
        return comboBox;
    }

    private JComboBox<String> createTypeComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.addItem("All Types"); // Changed default text
        String[] types = {"Park", "Waterfall", "Historic Site", "Landmark", "Bridge", "Lake"};
        for (String t : types) {
            comboBox.addItem(t);
        }
        // Add listener to trigger filtering when selection changes
        comboBox.addActionListener(e -> applyCombinedFilters());
        return comboBox;
    }

    private void addActionListeners() {
        viewButton.addActionListener(e -> viewSelectedLocation());
        deleteButton.addActionListener(e -> deleteSelectedLocation());
        // filterButton.addActionListener(e -> applyCombinedFilters()); // Not needed now
        resetButton.addActionListener(e -> resetView());

        returnButton.addActionListener(e -> {
            new HomePage(username, locationService).setVisible(true);
            frame.dispose();
        });
    }

    private void layoutComponents() {
        // --- Top Panel (Search + Filters) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.add(new JLabel("Filter By:"));
        filterPanel.add(provinceComboBox);
        filterPanel.add(typeComboBox);
        // filterPanel.add(filterButton); // Removed button
        filterPanel.add(resetButton); // Moved Reset button here for better grouping

        JPanel topPanel = new JPanel(new BorderLayout(0, 5)); // Add vertical gap
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Add padding
        topPanel.add(searchField, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER); // Use Center for filters

        // --- Right Panel (Actions) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.add(returnButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(viewButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(deleteButton);
        rightPanel.add(Box.createVerticalGlue());

        // --- Center Panel (Table) ---
        JScrollPane scrollPane = new JScrollPane(table);

        // --- Add Panels to Frame ---
        frame.setLayout(new BorderLayout(5, 5));
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadInitialData() {
        try {
            // Clear existing filters before loading
            if (sorter != null) {
                sorter.setRowFilter(null);
            }
            List<LocationData> allLocations = locationService.getAllLocations();
            updateTableModel(allLocations); // This just updates the model data
            System.out.println("Initial data loaded. Rows in model: " + tableModel.getRowCount());
        } catch (Exception e) {
            System.err.println("Error loading initial location data: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                    "Could not load location data:\n" + e.getMessage(),
                    "Data Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates the table model data. The sorter will handle displaying it.
     */
    private void updateTableModel(List<LocationData> locations) {
        tableModel.setRowCount(0); // Clear existing rows in the model
        if (locations != null && !locations.isEmpty()) {
            for (LocationData loc : locations) {
                tableModel.addRow(new Object[]{
                        loc.getId(),
                        loc.getName() != null ? loc.getName() : "",
                        loc.getCity() != null ? loc.getCity() : "",
                        loc.getProvince() != null ? loc.getProvince() : "",
                        loc.getCategory() != null ? loc.getCategory() : ""
                });
            }
        }
        // The sorter will automatically update the view based on the new model data
        // and existing filter (if any).
    }

    /**
     * Applies filters based on the current state of search field and combo boxes.
     */
    private void applyCombinedFilters() {
        if (sorter == null) return; // Should not happen after init

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // 1. Text Filter (Fuzzy or Regex)
        String searchText = searchField.getText();
        final String placeholder = "Search name, city, category...";
        if (searchText != null && !searchText.trim().isEmpty() && !searchText.equals(placeholder)) {
            // Option A: Simple Regex Filter (Case-Insensitive)
            // try {
            //     filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText)));
            // } catch (PatternSyntaxException pse) {
            //     System.err.println("Bad regex pattern: " + pse.getMessage());
            // }
            // Option B: Custom Fuzzy RowFilter
            filters.add(createFuzzyRowFilter(searchText.toLowerCase()));
        }

        // 2. Province Filter
        String province = (String) provinceComboBox.getSelectedItem();
        if (province != null && !province.equals("All Provinces")) {
            // Column 3 is Province
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(province) + "$", 3));
        }

        // 3. Type/Category Filter
        String type = (String) typeComboBox.getSelectedItem();
        if (type != null && !type.equals("All Types")) {
            // Column 4 is Category
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(type) + "$", 4));
        }

        // Combine filters using AND logic
        RowFilter<Object, Object> combinedFilter = null;
        if (!filters.isEmpty()) {
            combinedFilter = RowFilter.andFilter(filters);
        }

        // Apply the combined filter to the sorter
        sorter.setRowFilter(combinedFilter);
    }

    /**
     * Creates a custom RowFilter implementing fuzzy matching.
     */
    private RowFilter<Object, Object> createFuzzyRowFilter(String query) {
        return new RowFilter<Object, Object>() {
            @Override
            public boolean include(Entry<?, ?> entry) {
                // Iterate through columns relevant for searching (skip ID column 0)
                for (int i = 1; i < entry.getValueCount(); i++) {
                    Object value = entry.getValue(i);
                    if (value != null) {
                        String cellText = value.toString().toLowerCase();
                        // Check if any word in the cell text is close enough
                        String[] words = cellText.split("\\s+"); // Split by whitespace
                        for (String word : words) {
                            if (levenshteinDistance.apply(query, word) <= 2) { // Using threshold 2
                                return true; // Match found in this row
                            }
                        }
                        // Optional: Check the whole cell text as well?
                        // if (levenshteinDistance.apply(query, cellText) <= 2) return true;
                    }
                }
                return false; // No match found in any relevant cell of this row
            }
        };
    }


    /**
     * Resets filters, search field, and clears the sorter's filter.
     */
    private void resetView() {
        provinceComboBox.setSelectedIndex(0); // "All Provinces"
        typeComboBox.setSelectedIndex(0);     // "All Types"
        setupSearchFieldPlaceholder(); // Reset search field text and color

        // Clear the sorter's filter to show all rows from the model
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
        System.out.println("Filters and search reset. Showing all model data.");
        // Optional: Could reload data if needed, but usually not necessary if model is intact
        // loadInitialData();
    }


    // --- Action Methods ---

    private void viewSelectedLocation() {
        int selectedViewRow = table.getSelectedRow();
        if (selectedViewRow != -1) {
            int modelRow = table.convertRowIndexToModel(selectedViewRow);
            LocationData locationData = getLocationDataFromModelRow(modelRow);
            if (locationData != null) {
                DetailView detailView = new DetailView(this.frame, locationData);
                detailView.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(frame, "Could not retrieve data for the selected row.", "Data Error", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a location from the table to view.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedLocation() {
        int selectedViewRow = table.getSelectedRow();
        if (selectedViewRow != -1) {
            int modelRow = table.convertRowIndexToModel(selectedViewRow); // Use model row index!
            String locationId = (String) tableModel.getValueAt(modelRow, 0);
            String locationName = (String) tableModel.getValueAt(modelRow, 1);

            int confirmation = JOptionPane.showConfirmDialog(frame,"Are you sure you want to delete '" + locationName + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirmation == JOptionPane.YES_OPTION) {
                try {
                    locationService.deleteLocation(locationId);
                    // IMPORTANT: Remove the row from the MODEL, not the view directly.
                    // The sorter handles the view update.
                    tableModel.removeRow(modelRow);
                    // Re-apply filters in case the deletion affects the view, although
                    // removeRow should notify the sorter. It's usually safer though.
                    // applyCombinedFilters(); // Maybe not needed, test first.
                    JOptionPane.showMessageDialog(frame, "'" + locationName + "' deleted successfully.", "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    System.err.println("Error deleting location: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Could not delete location:\n" + e.getMessage(), "Deletion Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a location from the table to delete.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    // --- Helper Methods ---

    private LocationData getLocationDataFromModelRow(int modelRowIndex) {
        if (modelRowIndex < 0 || modelRowIndex >= tableModel.getRowCount()) {
            System.err.println("Error: Attempted to access invalid model row index: " + modelRowIndex);
            return null;
        }
        try {
            String id = String.valueOf(tableModel.getValueAt(modelRowIndex, 0));
            String name = String.valueOf(tableModel.getValueAt(modelRowIndex, 1));
            String city = String.valueOf(tableModel.getValueAt(modelRowIndex, 2));
            String province = String.valueOf(tableModel.getValueAt(modelRowIndex, 3));
            String category = String.valueOf(tableModel.getValueAt(modelRowIndex, 4));
            return new LocationData(id, name, city, province, category);
        } catch (Exception e) {
            System.err.println("Unexpected error retrieving data from model row " + modelRowIndex + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}