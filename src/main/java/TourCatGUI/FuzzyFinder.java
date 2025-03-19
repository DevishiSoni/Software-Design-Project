package TourCatGUI;

import org.apache.commons.text.similarity.LevenshteinDistance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
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

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return true;
            }
        });

        table.setRowSorter(sorter);
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
                if (cell != null && levenshteinDistance.apply(query.toLowerCase(), cell.toString().toLowerCase()) <= 3) {
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

    }
}
