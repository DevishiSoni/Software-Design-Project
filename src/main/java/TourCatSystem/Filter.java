package TourCatSystem;

import java.io.*; // Import necessary IO classes
import java.util.ArrayList;
import java.util.List; // Use List interface

public class Filter {
    private final File databaseFile; // Make final, set in constructor
    private ArrayList<String> results; // Store results here

    // Define column indices (adjust if your CSV is different)
    private static final int PROVINCE_COLUMN_INDEX = 3;
    private static final int TYPE_COLUMN_INDEX = 4;

    // Constructor takes the database file
    public Filter(File databaseFile) {
        if (databaseFile == null || !databaseFile.exists()) {
            throw new IllegalArgumentException("Database file must exist and not be null.");
        }
        this.databaseFile = databaseFile;
        this.results = new ArrayList<>();
    }

    // Method to read all relevant lines (excluding header)
    private List<String> readAllLines() {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(databaseFile))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading database file in Filter: " + e.getMessage());
            // Consider throwing a custom exception or returning empty list
        }
        return lines;
    }

    // Helper to safely get column data
    private String getColumnData(String line, int columnIndex) {
        if (line == null) return null;
        String[] parts = line.split(","); // Simple CSV split
        if (columnIndex >= 0 && columnIndex < parts.length) {
            return parts[columnIndex].trim(); // Trim whitespace
        }
        return null; // Index out of bounds or bad split
    }


    // --- Filtering Methods ---

    // Filter by Province only
    public void filterProvince(String selectedProvince) {
        results.clear();
        if (selectedProvince == null || selectedProvince.trim().isEmpty()) {
            return; // No filter applied if province is null/empty
        }
        List<String> allLines = readAllLines();
        for (String line : allLines) {
            String provinceInLine = getColumnData(line, PROVINCE_COLUMN_INDEX);
            if (provinceInLine != null && provinceInLine.equalsIgnoreCase(selectedProvince.trim())) {
                results.add(line);
            }
        }
    }

    // Filter by Type only
    public void filterType(String selectedType) {
        results.clear();
        if (selectedType == null || selectedType.trim().isEmpty()) {
            return; // No filter applied if type is null/empty
        }
        List<String> allLines = readAllLines();
        for (String line : allLines) {
            String typeInLine = getColumnData(line, TYPE_COLUMN_INDEX);
            // Special handling for "Historic Site" possibly containing a comma
            // A more robust CSV parser would be better here.
            // Let's assume for now the simple split works or the data is clean.
            if (typeInLine != null && typeInLine.equalsIgnoreCase(selectedType.trim())) {
                results.add(line);
            }
        }
    }

    // Filter by Both Province and Type
    public void filterBoth(String selectedProvince, String selectedType) {
        results.clear();
        if (selectedProvince == null || selectedProvince.trim().isEmpty() ||
                selectedType == null || selectedType.trim().isEmpty()) {
            // Maybe filter by the one that IS provided? Or require both?
            // Current logic requires both. If only one provided, result is empty.
            return;
        }

        List<String> allLines = readAllLines();
        String targetProvince = selectedProvince.trim();
        String targetType = selectedType.trim();

        for (String line : allLines) {
            String provinceInLine = getColumnData(line, PROVINCE_COLUMN_INDEX);
            String typeInLine = getColumnData(line, TYPE_COLUMN_INDEX);

            if (provinceInLine != null && provinceInLine.equalsIgnoreCase(targetProvince) &&
                    typeInLine != null && typeInLine.equalsIgnoreCase(targetType)) {
                results.add(line);
            }
        }
    }

    // Get results
    public ArrayList<String> getResults() {
        // Return a copy to prevent external modification? Optional.
        // return new ArrayList<>(results);
        return results;
    }

    // Reset filter results
    public void reset() {
        results.clear();
        // Maybe also reset internal province/type state if they were instance vars
    }

    // Simple print method (mainly for testing)
    public void printResults() {
        if (results.isEmpty()) {
            System.out.println("No matching results found for the last filter operation.");
        } else {
            System.out.println("Filter Results (" + results.size() + " items):");
            for (String result : results) {
                System.out.println(result);
            }
        }
    }
}