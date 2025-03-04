import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

//The location reader class will be the class responsible for reading from
//the csv file.
public class LocationReader {

    private DefaultTableModel tableModel;

    LocationReader(File file)
    {
        try(CSVReader reader = new CSVReader(new FileReader(file))){

            String[] header = reader.readNext();

            tableModel = new DefaultTableModel();

            if (header != null)
            {
                tableModel.setColumnIdentifiers(header);
            }

            String[] line;
            while((line = reader.readNext()) != null)
            {
                tableModel.addRow(line);
            }



        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    public DefaultTableModel getTableModel() {
        return this.tableModel;
    }

    public static void main(String[] args) throws FileNotFoundException {
        String geonamesPath = new File("").getAbsolutePath();
        geonamesPath += File.separator + "geonames.csv";
        LocationReader reader = new LocationReader(new File(geonamesPath));

        DefaultTableModel model = reader.getTableModel();

        // Create the JTable with the model
        JTable table = new JTable(model);

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
        searchField.setToolTipText("Type to search...");

        // Key listener for search field
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fuzzyFinder.performFuzzySearch(searchField.getText());
            }
        });

        // Add components to frame
        frame.add(searchField, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
