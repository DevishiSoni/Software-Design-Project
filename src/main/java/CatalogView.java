import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

public class CatalogView {

    CatalogView(String username)
    {
        String geonamesPath = new File("").getAbsolutePath();
        geonamesPath += File.separator + "geonames.csv";
        LocationReader reader = new LocationReader(new File(geonamesPath));

        DefaultTableModel model = reader.getTableModel();

        // Create the JTable with the model
        JTable table = new JTable(model);

        TableColumnModel columnModel = table.getColumnModel();

        LocationReader.hideColumns(columnModel, new int[]{0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15});

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

        JButton returnButton = new JButton("Return To Homepage");

        returnButton.addActionListener(e -> {
            new HomePage(username);
            frame.dispose();
        });

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(4, 1));

        rightPanel.add(returnButton);

        // Add components to frame
        frame.add(rightPanel, BorderLayout.EAST);
        frame.add(searchField, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);




        frame.setVisible(true);
    }
}
