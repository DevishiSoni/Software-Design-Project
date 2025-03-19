package TourCatGUI;

import TourCatSystem.ChangeDatabase;
import TourCatSystem.FileManager;
import TourCatSystem.LocationReader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

public class CatalogView {

    public JButton alertButton;

    CatalogView(String username)
    {
        File dataBase = FileManager.getInstance().getResourceFile("test.csv");

        LocationReader reader = new LocationReader(dataBase);

        DefaultTableModel model = reader.getTableModel();

        // Create the JTable with the model
        JTable table = new JTable(model);

        TableColumnModel columnModel = table.getColumnModel();

        LocationReader.hideColumns(columnModel, new int[]{});

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
        JTextField searchField = new JTextField();

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

        alertButton = new JButton("Alert");

        fuzzyFinder.setMaxRows(1000);

        JButton returnButton = new JButton("Return To Homepage");

        returnButton.addActionListener(e -> {
            new HomePage(username);
            frame.dispose();
        });

        alertButton.addActionListener(e -> {
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

        rightPanel.add(alertButton);
        searchField.setText("Search:");

        JButton deleteButton = new JButton("Delete");

        // Add components to frame
        frame.add(rightPanel, BorderLayout.EAST);
        frame.add(searchField, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        rightPanel.add(deleteButton);




        frame.setVisible(true);

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow != -1) { // Ensure a row is selected
                int columnCount = table.getColumnCount();

                String selectedRowID = (String) table.getValueAt(selectedRow, 0);

                ChangeDatabase.deleteFromFile(selectedRowID, dataBase);

                model.removeRow(selectedRow);
            } else {
                System.out.println("No row selected.");
            }
        });




    }



}
