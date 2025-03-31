package TourCatData;

import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.ArrayList;

/**
 * The DatabaseManager class provides functionality to manage data stored in a CSV file.
 * It includes methods to add and delete records related to landmarks.
 *
 * <p>
 * Methods:
 * <ul>
 *     <li><b>deleteFromFile(String landmarkName, File file)</b> - Deletes a record from the
 *         CSV file that matches the given landmark name. The operation ensures that
 *         the original file is replaced only if a record is successfully removed.</li>
 *     <li><b>addToFile(ArrayList<String> newLandmark, File file)</b> - Adds a new landmark
 *         record to the CSV file. Appends the entry to the end of the file.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Usage:
 * <ul>
 *     <li>Use <b>deleteFromFile()</b> to search for and remove a landmark by name.</li>
 *     <li>Use <b>addToFile()</b> to insert a new landmark record in the CSV.</li>
 * </ul>
 * </p>
 *
 * <p>
 * File Structure:
 * <ul>
 *     <li>Each row in the CSV file represents a landmark.</li>
 *     <li>1st Column - Represents the landmark id - useful for pulling additional info from externals</li>
 *     <li>2nd Column - Geographical name</li>
 *     <li>3rc Column - City</li>
 *     <li>4th Column - Province</li>
 *     <li>5th Column - Category</li>
 * </ul>
 * </p>
 *
 * <p>
 * Dependencies:
 * <ul>
 *     <li>com.opencsv.CSVReader</li>
 *     <li>com.opencsv.CSVWriter</li>
 *     <li>com.opencsv.exceptions.CsvValidationException</li>
 *     <li>FileManager for resolving file paths</li>
 * </ul>
 * </p>
 *
 * <p>
 * Error Handling:
 * <ul>
 *     <li>Logs warnings for malformed rows encountered in the CSV.</li>
 *     <li>Logs errors encountered while reading or writing files.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Example Usage:
 * <pre>
 *     File database = new File(FileManager.getInstance().getResourceDirectoryPath() + File.separator + "database.csv");
 *     boolean result = deleteFromFile("landmarkName", database);
 *     System.out.println("Delete successful: " + result);
 * </pre>
 * </p>
 *
 * <p>
 * Author: Garrett
 * Version: 1.0
 * Date: 3-20-2025
 * </p>
 */
public class DatabaseManager {

    public static boolean deleteFromFile(String landmarkName, File file) {
        boolean success = false;
        int indexOfName = 0; // Make sure this matches your CSV structure

        System.out.println("Deleting from: " + file.getAbsolutePath());

        File tempFile = new File(file.getParent() + File.separator + "tmp.csv");

        try (
                CSVReader reader = new CSVReaderBuilder(new FileReader(file))
                        .withSkipLines(0)
                        .build();
                CSVWriter writer = (CSVWriter) new CSVWriterBuilder(new FileWriter(tempFile))
                        .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                        .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                        .withEscapeChar(CSVWriter.NO_ESCAPE_CHARACTER)
                        .withLineEnd(CSVWriter.DEFAULT_LINE_END)
                        .build()
        ) {
            String[] nextLine;
            boolean found = false;

            while ((nextLine = reader.readNext()) != null) {
                // Make sure we're not trying to access beyond array bounds
                System.out.println(nextLine[0]);
                if (nextLine.length > indexOfName) {
                    if (!found && landmarkName.equals(nextLine[indexOfName])) {
                        found = true; // Skip writing this line
                        success = true; // Mark that we found and deleted the record
                    } else {
                        writer.writeNext(nextLine);
                    }
                } else {
                    // Handle malformed rows by just writing them back
                    writer.writeNext(nextLine);
                    System.err.println("Warning: Malformed row encountered in CSV.");
                }
            }

            if (!found) {
                System.out.println("Landmark not found: " + landmarkName);
                // No changes needed to original file
                return false;
            }

            writer.flush();
        } catch (CsvValidationException | IOException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }

        // Replace original file with updated one only if we found and removed a record
        if (success) {
            if (file.delete() && tempFile.renameTo(file)) {
                return true;
            } else {
                System.err.println("Could not replace original file.");
                return false;
            }
        } else {
            // Clean up temp file if no changes were made
            tempFile.delete();
            return false;
        }
    }

    public static boolean addToFile(ArrayList<String> newLandmark, File file) {
        if (newLandmark == null || newLandmark.isEmpty()) {
            System.err.println("Error: Empty landmark data.");
            return false;
        }

        boolean success = false;

        try (
                CSVWriter writer = (CSVWriter) new CSVWriterBuilder(new FileWriter(file, true))
                        .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                        .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                        .withEscapeChar(CSVWriter.NO_ESCAPE_CHARACTER)
                        .withLineEnd(CSVWriter.DEFAULT_LINE_END)
                        .build()
        ) {
            String[] newEntry = newLandmark.toArray(new String[0]);
            writer.writeNext(newEntry);
            writer.flush();
            success = true;
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        return success;
    }

    public static int getMaxId(File file) {
        try(CSVReader reader = new CSVReaderBuilder(new FileReader(file))
                .withSkipLines(0)
                .build();)
        {
            String[] nextLine = reader.readNext();
            int maxId = -1;
            while ((nextLine = reader.readNext()) != null) {
                // Make sure we're not trying to access beyond array bounds
                if (nextLine.length > 1) {
                    int id = Integer.parseInt(nextLine[0]);
                    if(id > maxId){
                        maxId = id;
                    }
                }
            }
            return maxId;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    public static void main(String[] args) {
        String fileName = "test.csv";

        File database = new File(FileManager.getInstance(true).getResourceDirectoryPath() + File.separator + fileName);

        boolean result = deleteFromFile("newN", database);

        System.out.println("Delete successful: " + result);
    }

}
