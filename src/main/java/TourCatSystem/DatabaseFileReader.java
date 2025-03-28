package TourCatSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseFileReader {

    private final File databaseFile;

    public DatabaseFileReader(File databaseFile) {
        if (databaseFile == null || !databaseFile.exists() || !databaseFile.isFile()) {
            throw new IllegalArgumentException("Invalid database file provided: " +
                    (databaseFile == null ? "null" : databaseFile.getAbsolutePath()));
        }
        this.databaseFile = databaseFile;
    }

    /**
     * Reads all data lines (excluding the header) from the database file.
     *
     * @return A List of strings, each representing a data row from the file.  Returns an empty list on error.
     */
    public List<String> readAllDataLines() {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(databaseFile))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;  // Skip header
                    continue;
                }
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading database file: " + e.getMessage());
            // Consider throwing a custom exception here
        }
        return lines;
    }
}