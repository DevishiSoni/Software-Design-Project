package TourCatGUI.Catalog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class FuzzyFinder { // Renaming might be good later if not using fuzzy logic

    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    // Define which columns are searchable (using their MODEL indices)
    // Example: Assuming 0=ID, 1=Name, 2=City, 3=Province, 4=Category
    private final List<Integer> searchableColumns = List.of(1, 2, 3, 4); // Exclude ID column (index 0)

    /**
     * Creates a finder/filterer for the given JTable.
     * It sets up a TableRowSorter to enable dynamic filtering.
     *
     * @param table The JTable to apply filtering to.
     */
    public FuzzyFinder(JTable table) {
        this.table = table;
        if (!(table.getModel() instanceof DefaultTableModel)) {
            throw new IllegalArgumentException("FuzzyFinder requires a DefaultTableModel for the JTable.");
        }
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        this.sorter = new TableRowSorter<>(model);
        this.table.setRowSorter(sorter);
    }

    /**
     * Applies a filter to the table based on the query string.
     * Rows are included if the query (case-insensitive) is found as a substring
     * in any of the searchable columns.
     *
     * @param query The text to search for. If empty or null, the filter is cleared.
     */
    public void performFuzzySearch(String query) {
        final String preparedQuery = (query == null) ? "" : query.trim().toLowerCase();

        if (preparedQuery.isEmpty()) {
            // If query is empty, remove the filter to show all rows
            sorter.setRowFilter(null);
        } else {
            try {
                // Create a RowFilter that checks designated columns for the query substring
                RowFilter<DefaultTableModel, Integer> rowFilter = new RowFilter<>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        // Iterate only through the searchable columns defined earlier
                        for (int colIndex : searchableColumns) {
                            // Ensure the column index is valid for the current row entry
                            if (colIndex >= 0 && colIndex < entry.getValueCount()) {
                                Object value = entry.getValue(colIndex);
                                if (value != null) {
                                    // Convert cell value to lowercase string and check for substring
                                    String cellText = value.toString().toLowerCase();
                                    if (cellText.contains(preparedQuery)) {
                                        return true; // Match found in this row, include it
                                    }
                                }
                            }
                        }
                        // No match found in any searchable column for this row
                        return false;
                    }
                };
                // Apply the filter
                sorter.setRowFilter(rowFilter);

            } catch (PatternSyntaxException e) {
                // This catch block might be relevant if using regex-based filters in the future.
                // For simple contains, it's less likely to be hit unless the query itself causes issues
                // (highly unlikely for basic strings).
                System.err.println("Error applying filter: " + e.getMessage());
                sorter.setRowFilter(null); // Clear filter on error
            }
        }
    }

    // --- Optional: Alternative using Regex for more complex patterns ---
    // (Uncomment and use this instead of the contains-based filter if needed)
    /*
    public void performRegexSearch(String query) {
        final String preparedQuery = (query == null) ? "" : query.trim();

        if (preparedQuery.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            try {
                // Create a case-insensitive regex pattern
                // We escape the query to treat it literally unless you intend regex features
                Pattern pattern = Pattern.compile(Pattern.quote(preparedQuery), Pattern.CASE_INSENSITIVE);

                RowFilter<DefaultTableModel, Integer> rowFilter = new RowFilter<>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        for (int colIndex : searchableColumns) {
                            if (colIndex >= 0 && colIndex < entry.getValueCount()) {
                                Object value = entry.getValue(colIndex);
                                if (value != null) {
                                    Matcher matcher = pattern.matcher(value.toString());
                                    if (matcher.find()) { // Check if the pattern is found anywhere
                                        return true;
                                    }
                                }
                            }
                        }
                        return false;
                    }
                };
                sorter.setRowFilter(rowFilter);
            } catch (PatternSyntaxException e) {
                System.err.println("Invalid regex pattern: " + e.getMessage());
                sorter.setRowFilter(null); // Clear filter on error
            }
        }
    }
    */

    /**
     * Clears any active filter, showing all rows.
     */
    public void clearFilter() {
        sorter.setRowFilter(null);
    }


    // Example main method for basic testing (requires a visible JFrame)
    public static void main(String[] args) {
        // --- Setup Minimal GUI for Testing ---
        JFrame frame = new JFrame("FuzzyFinder Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Sample Data
        String[] columnNames = {"ID", "Name", "City", "Type"};
        Object[][] data = {
                {"1", "Niagara Falls", "Niagara", "Waterfall"},
                {"2", "CN Tower", "Toronto", "Landmark"},
                {"3", "Stanley Park", "Vancouver", "Park"},
                {"4", "Parliament Hill", "Ottawa", "Historic Site"},
                {"5", "Old Quebec", "Quebec City", "Historic Site"},
                {"6", "Lake Louise", "Banff", "Lake"},
                {"7", "Signal Hill", "St. John's", "Historic Site"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // Create the Finder instance AFTER table and model are set up
        FuzzyFinder finder = new FuzzyFinder(table);
        // IMPORTANT: Make sure finder's searchableColumns match your sample data indices
        // finder.searchableColumns = List.of(1, 2, 3); // Adjust if needed

        JTextField searchField = new JTextField();
        searchField.addActionListener(e -> finder.performFuzzySearch(searchField.getText())); // Filter on Enter
        searchField.putClientProperty("JTextField.placeholderText", "Type to search and press Enter...");


        JButton clearButton = new JButton("Clear Filter");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            finder.clearFilter();
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(clearButton, BorderLayout.EAST);


        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // --- End GUI Setup ---


        // You can also test programmatically (though less visual)
        System.out.println("Testing programmatically:");
        finder.performFuzzySearch("Park");
        System.out.println("Rows visible after searching 'Park': " + table.getRowCount()); // Shows filtered row count

        try { Thread.sleep(2000); } catch (InterruptedException e) {} // Pause

        finder.performFuzzySearch("hiStoric");
        System.out.println("Rows visible after searching 'hiStoric': " + table.getRowCount());

        try { Thread.sleep(2000); } catch (InterruptedException e) {} // Pause

        finder.clearFilter();
        System.out.println("Rows visible after clearing filter: " + table.getRowCount());
    }
}