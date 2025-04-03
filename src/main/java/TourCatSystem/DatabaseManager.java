package TourCatSystem;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException; // Use CsvException for broader CSV errors

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt; // Good for returning optional numeric results

import static TourCatSystem.AppDataManager.INTERNAL_DB_PATH;

/**
 * Manages interaction with a location database stored in a CSV file.
 * Provides methods to read, add, delete, and query location records.
 * Each instance operates on a specific database file.
 * <p>
 * CSV Structure Expected:
 * - Column 0: ID (String, unique, typically numeric format like "00001")
 * - Column 1: Name (String)
 * - Column 2: City (String)
 * - Column 3: Province (String)
 * - Column 4: Category (String)
 * <p>
 * Dependencies: OpenCSV library.
 * <p>
 * Error Handling: Methods throw IOExceptions or CsvExceptions on failure.
 * <p>
 * Author: Garrett (Refactored by AI Assistant)
 * Version: 2.0
 * Date
 */
public class DatabaseManager {

    // --- Constants for CSV Column Indices ---
    private static final int ID_COLUMN = 0;
    private static final int NAME_COLUMN = 1;
    private static final int CITY_COLUMN = 2;
    private static final int PROVINCE_COLUMN = 3;
    private static final int CATEGORY_COLUMN = 4;
    // Add more if needed, ensure this matches your actual file structure

    private final File databaseFile;
    private final CSVParser csvParser; // Reusable parser configuration

    /**
     * Creates a DatabaseManager instance for the specified CSV file.
     *
     * @param databaseFile The CSV file to manage. Must not be null.
     * @throws IllegalArgumentException if databaseFile is null or not a file.
     * @throws IOException              if the file cannot be created or accessed appropriately.
     */
    public DatabaseManager (File databaseFile) throws IOException {
        if (databaseFile == null) {
            throw new IllegalArgumentException("Database file cannot be null.");
        }
        // Ensure the directory exists
        File parentDir = databaseFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Could not create parent directory: " + parentDir.getAbsolutePath());
            }
        }
        // Ensure the file exists (create if not) - Optional: you might want creation handled elsewhere
        if (!databaseFile.exists()) {
            try {
                if (databaseFile.createNewFile()) {
                    // Optionally write header row if creating a new file
                    writeHeaderIfNotPresent();
                    System.out.println("Created new database file: " + databaseFile.getAbsolutePath());
                } else {
                    throw new IOException("Could not create database file: " + databaseFile.getAbsolutePath());
                }
            } catch (SecurityException se) {
                throw new IOException("Security exception creating file: " + databaseFile.getAbsolutePath(), se);
            }
        }
        if (!databaseFile.isFile()) {
            throw new IllegalArgumentException("Database path does not point to a valid file: " + databaseFile.getAbsolutePath());
        }

        this.databaseFile = databaseFile;

        // Configure the parser once - assuming standard CSV, no quotes needed based on original code
        this.csvParser = new CSVParserBuilder()
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                // .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER) // Let parser handle quotes if they exist
                .build();
    }

    /**
     * Writes the header row if the database file is empty or newly created.
     *
     * @throws IOException if writing fails.
     */
    private void writeHeaderIfNotPresent () throws IOException {
        if (databaseFile.length() == 0) { // Check if file is empty
            try (ICSVWriter writer = createCsvWriter(false)) { // false = don't append
                writer.writeNext(new String[]{"ID", "Name", "City", "Province", "Category"});
            }
        }
    }


    /**
     * Helper to create a configured CSVWriter.
     *
     * @param append true to append to the file, false to overwrite.
     * @return An configured ICSVWriter instance.
     * @throws IOException If the writer cannot be created.
     */
    private ICSVWriter createCsvWriter (boolean append) throws IOException {
        // Based on original code, NO_QUOTE_CHARACTER was used. Be cautious if data might contain commas.
        // If data can contain commas or quotes, use DEFAULT_QUOTE_CHARACTER.
        return new CSVWriterBuilder(new FileWriter(databaseFile, append))
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER) // Adjust if needed
                .withEscapeChar(CSVWriter.NO_ESCAPE_CHARACTER) // Adjust if needed
                .withLineEnd(CSVWriter.DEFAULT_LINE_END)
                .build();
    }

    /**
     * Deletes a record from the CSV file based on its unique ID.
     * This is generally safer and more reliable than deleting by name if IDs are unique.
     *
     * @param locationIdToDelete The ID of the location record to delete.
     * @throws IOException             If file reading/writing fails.
     * @throws CsvException            If there's an error processing the CSV data.
     * @throws RecordNotFoundException If no record with the specified ID is found.
     */
    public void deleteById (String locationIdToDelete) throws IOException, CsvException, RecordNotFoundException {
        if (locationIdToDelete == null || locationIdToDelete.trim().isEmpty()) {
            throw new IllegalArgumentException("Location ID to delete cannot be null or empty.");
        }

        List<String[]> allRows;
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(databaseFile)).withCSVParser(csvParser).build()) {
            allRows = reader.readAll();
        }

        List<String[]> rowsToWrite = new ArrayList<>();
        boolean found = false;
        int expectedColumnCount = -1; // Track expected columns from header or first row

        for (String[] row : allRows) {
            if (row == null || row.length == 0) continue; // Skip empty lines

            if (expectedColumnCount == -1) {
                expectedColumnCount = row.length; // Set based on first valid row (usually header)
            }

            // Basic validation - Check against ID column
            if (row.length > ID_COLUMN && locationIdToDelete.equals(row[ID_COLUMN])) {
                found = true; // Found the record, don't add it to rowsToWrite
            } else {
                // Check for consistent column count if desired, otherwise just write valid rows
                if (row.length != expectedColumnCount) {
                    System.err.println("Warning: Row with inconsistent column count encountered: " + String.join(",", row));
                    // Decide whether to skip or write these malformed rows
                }
                rowsToWrite.add(row);
            }
        }

        if (!found) {
            throw new RecordNotFoundException("Location with ID '" + locationIdToDelete + "' not found for deletion.");
        }

        // Overwrite the original file with the filtered rows
        try (ICSVWriter writer = createCsvWriter(false)) { // false = overwrite
            writer.writeAll(rowsToWrite);
        }
    }

    /**
     * Adds a new location record to the end of the CSV file.
     *
     * @param newLocationData An array representing the new location record.
     *                        Must match the expected CSV structure (ID, Name, City, Province, Category).
     * @throws IOException              If writing to the file fails.
     * @throws IllegalArgumentException If newLocationData is null or has incorrect length.
     */
    public void addRecord (String[] newLocationData) throws IOException {
        // Basic validation - adjust expected length if columns change
        int expectedColumns = 5;
        if (newLocationData == null || newLocationData.length < expectedColumns) {
            throw new IllegalArgumentException("New location data is invalid or incomplete. Expected " + expectedColumns + " columns.");
        }

        // Ensure header exists before appending
        writeHeaderIfNotPresent();

        try (ICSVWriter writer = createCsvWriter(true)) { // true = append
            writer.writeNext(newLocationData);
        }
    }
    
    /**
     * Reads all valid data rows from the CSV file (excluding the header).
     *
     * @return A List of String arrays, where each array represents a row.
     * @throws IOException  if file reading fails.
     * @throws CsvException if CSV parsing fails.
     */
    public List<String[]> readAllRecords () throws IOException, CsvException {
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(databaseFile))
                .withCSVParser(csvParser)
                .withSkipLines(1) // Skip header row
                .build()) {
            return reader.readAll();
        }
    }

    /**
     * Custom exception for cases where a record lookup fails.
     */
    public static class RecordNotFoundException extends Exception {
        public RecordNotFoundException (String message) {
            super(message);
        }
    }


    /**
     * Ensures the writable database file exists, copying the default from resources if needed.
     *
     * @return The File object pointing to the writable database.
     * @throws IOException        If file operations fail.
     * @throws URISyntaxException If finding the app's running location fails.
     */
    private File initializeWritableDatabase () throws IOException, URISyntaxException {
        File externalDbFile = AppDataManager.getWritableDatabasePath().toFile();


        return externalDbFile;
    }


}