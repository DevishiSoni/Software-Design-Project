import org.apache.commons.text.similarity.LevenshteinDistance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

public class FuzzyFinder {
    private static final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    private final JTable table;
    private final Vector<Vector<Object>> originalData; // Manually copied for type safety

    public FuzzyFinder(JTable table) {
        this.table = table;
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        this.originalData = new Vector<>();  // Properly initialize the data storage

        // **Manually copy the data to enforce correct typing**
        for (Object rowObj : tableModel.getDataVector()) {
            Vector<?> rawRow = (Vector<?>) rowObj;  // Cast each row safely
            Vector<Object> typedRow = new Vector<>(rawRow);  // Convert to correct type
            originalData.add(typedRow);
        }
    }

    public void performFuzzySearch(String query) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);  // Clear the table

        if (query.isEmpty()) {
            for (Vector<Object> row : originalData) {
                model.addRow(row);
            }
            return;
        }

        for (Vector<Object> row : originalData) {
            boolean match = false;
            for (Object cell : row) {
                if (cell != null && levenshteinDistance.apply(query.toLowerCase(), cell.toString().toLowerCase()) <= 2) {
                    match = true;
                    break;
                }
            }
            if (match) {
                model.addRow(row);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Fuzzy Search with JTable");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

            // Table data
            String[] columnNames = {"ID", "Name", "Category"};
            Object[][] data = {
                    {1, "Apple", "Fruit"},
                    {2, "Banana", "Fruit"},
                    {3, "Carrot", "Vegetable"},
                    {4, "Grape", "Fruit"},
                    {5, "Mango", "Fruit"}
            };

            // JTable setup
            DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);

            // Search field
            JTextField searchField = new JTextField();
            searchField.setToolTipText("Type to search...");

            // FuzzyFinder instance
            FuzzyFinder fuzzyFinder = new FuzzyFinder(table);

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
        });
    }
}
