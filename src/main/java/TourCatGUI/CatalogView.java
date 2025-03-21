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

        LocationReader.hideColumns(columnModel, new int[]{0, 4});

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
            int selectedRow = table.getSelectedRow();

            if (selectedRow != -1) { // Ensure a row is selected
                int columnCount = table.getColumnCount();
                StringBuilder rowData = new StringBuilder();

                for (int i = 0; i < columnCount; i++) {
                    rowData.append(table.getValueAt(selectedRow, i)).append("\t");
                }

                System.out.println(rowData.toString().trim());
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