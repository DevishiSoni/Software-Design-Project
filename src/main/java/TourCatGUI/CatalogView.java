package TourCatGUI;

import TourCatSystem.DatabaseManager;
import TourCatSystem.FileManager;
import TourCatSystem.Filter;
import TourCatSystem.LocationReader;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class CatalogView {

    public JButton viewButton;

    JTable table;
    File dataBase;
    DefaultTableModel model;

    JTextField searchField;


    String selectedProvince = null;
    String selectedType = null;



    CatalogView(String username)
    {
        this.dataBase = FileManager.getInstance().getDatabaseFile();

        LocationReader reader = new LocationReader(dataBase);

        this.model = reader.getTableModel();

        // Create the JTable with the model
        this.table = new JTable(model);


        TableColumnModel columnModel = table.getColumnModel();

        LocationReader.hideColumns(columnModel, new int[]{0, 5});

        // Create a JScrollPane for scrolling functionality
        JScrollPane scrollPane = new JScrollPane(table);

        // Set up JFrame
        JFrame frame = new JFrame("GeoNames Data");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); // Set window size
        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER); // Add JScrollPane containing the table to the frame

        // Make the frame visible
        frame.setVisible(true);

        FuzzyFinder fuzzyFinder = new FuzzyFinder(table);

        // Search field
        searchField = new JTextField();

        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                searchField.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                searchField.setText("Search here:");
            }
        });

        searchField.setToolTipText("Type to search...");

        // Key listener for search field
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fuzzyFinder.performFuzzySearch(searchField.getText());
            }
        });

        viewButton = new JButton("View");


        JButton returnButton = new JButton("Return To Homepage");

        returnButton.addActionListener(e -> {
            new HomePage(username);
            frame.dispose();
        });

        viewButton.addActionListener(e -> {
            //TODO: Replace this specific function with a class with these parameters.

            int selectedRow = table.getSelectedRow();

            if (selectedRow != -1) { // Ensure a row is selected
                // Extract data from the selected row

                String id = (String) table.getValueAt(selectedRow, 0);
                String name = (String) table.getValueAt(selectedRow, 1);
                String city = (String) table.getValueAt(selectedRow, 2);
                String province = (String) table.getValueAt(selectedRow, 3);
                String category = (String) table.getValueAt(selectedRow, 4);
                String imagePath = (String) table.getValueAt(selectedRow, 5);

                // Create a new JFrame to display details
                JFrame detailsFrame = new JFrame("Location Details");
                detailsFrame.setSize(400, 400);
                detailsFrame.setLayout(new BorderLayout());

                // Create a panel for text details
                JPanel textPanel = new JPanel();
                textPanel.setLayout(new GridLayout(5, 1)); // 5 rows for different fields

                textPanel.add(new JLabel("Name: " + name));
                textPanel.add(new JLabel("City: " + city));
                textPanel.add(new JLabel("Province: " + province));
                textPanel.add(new JLabel("Category: " + category));

                // Image Label
                JLabel imageLabel = new JLabel();

                File imageFile = FileManager.getInstance().getImageFile(id + ".png");
                if(!imageFile.exists()) imageFile = FileManager.getInstance().getImageFile(id + ".jpg");

                System.out.println(imageFile.getAbsolutePath());

                if (imagePath != null && !imagePath.equals("No Image")) {

                    ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());

                    Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    imageLabel.setText("No Image Available");
                    imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                }

                // Add components to frame
                detailsFrame.add(textPanel, BorderLayout.NORTH);
                detailsFrame.add(imageLabel, BorderLayout.CENTER);

                detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                detailsFrame.setVisible(true);
            } else {
                System.out.println("No row selected.");
            }
        });


        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(4, 1));


        rightPanel.add(returnButton);

        rightPanel.add(viewButton);
        searchField.setText("Search:");

        JButton deleteButton = new JButton("Delete");

        // Add components to frame
        frame.add(rightPanel, BorderLayout.EAST);
        frame.add(searchField, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        rightPanel.add(deleteButton);

        //Filter Functionality
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JLabel filterBy = new JLabel("Filter By:");

        filterPanel.add(filterBy);
        JComboBox<String> provinceComboBox = new JComboBox<>();
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

        provinceComboBox.addActionListener(e -> {
            selectedProvince = (String) provinceComboBox.getSelectedItem();
        });
        JComboBox<String> typeComboBox = new JComboBox<>();
        typeComboBox.addItem("Select Type");
        typeComboBox.addItem("Park");
        typeComboBox.addItem("Waterfall");
        typeComboBox.addItem("Historic Site");
        typeComboBox.addItem("Landmark");


        typeComboBox.addActionListener(e -> {
            selectedType = (String) typeComboBox.getSelectedItem();
        });

        filterPanel.add(provinceComboBox);
        filterPanel.add(typeComboBox);

        JButton filterButton = new JButton("Filter");

        filterButton.addActionListener(e -> {
            Filter filter = new Filter();

            if (selectedProvince != null && selectedType != null) {
                filter.filterBoth(selectedProvince, selectedType);
            } else if (selectedProvince != null) {
                filter.filterProvince(selectedProvince);
            } else if (selectedType != null) {
                filter.filterType(selectedType);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select at least one filter option.");
                return;
            }

            // Get results and update table
            ArrayList<String> results = filter.getResults();
            updateTable(results);
        });
        filterPanel.add(filterButton);

        JButton resetButton = new JButton("Reset Filters");
        resetButton.addActionListener(e -> {
            provinceComboBox.setSelectedIndex(0);
            typeComboBox.setSelectedIndex(0);

            // Reset the filter variables
            selectedProvince = null;
            selectedType = null;

            // Manually read the original data from the file
            ArrayList<String> allResults = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(dataBase))) {
                String line;
                while ((line = br.readLine()) != null) {
                    allResults.add(line);  // Read each line and add to the results
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Update the table with all the rows from the file
            updateTable(allResults);
        });

        filterPanel.add(resetButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchField, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(rightPanel, BorderLayout.EAST);

        frame.setVisible(true);

        deleteButton.addActionListener(e -> {
            deleteRow();
        });

    }
    private void updateTable(ArrayList<String> results) {
        model.setRowCount(0); // Clear current table data

        for (String result : results) {
            String[] rowData = result.split(","); // Assuming CSV format
            model.addRow(rowData);
        }
    }

    void deleteRow()
    {
        int selectedRow = table.getSelectedRow();

        if (selectedRow != -1) { // Ensure a row is selected
            int columnCount = table.getColumnCount();

            String selectedRowID = (String) table.getValueAt(selectedRow, 0);

            DatabaseManager.deleteFromFile(selectedRowID, dataBase);

            model.removeRow(selectedRow);
        } else {
            System.out.println("No row selected.");
        }
    }


    public void setSearch(String text) {
        searchField.requestFocus();
        this.searchField.setText(text);
        System.out.println(text);
    }
}