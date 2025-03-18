import TourCatSystem.FileManager;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.ArrayList;

public class ChangeDatabase {

    public static boolean deleteFromFile(String landmarkName, String filePath) {
        boolean success = false;
        int indexOfName = 0; // Make sure this matches your CSV structure

        File fileToDeleteFrom = new File(FileManager.getInstance().getResourceDirectoryPath() + File.separator + "test.csv");

        System.out.println("Deleting from: " + fileToDeleteFrom.getAbsolutePath());

        File tempFile = new File(fileToDeleteFrom.getParent() + File.separator + "tmp.csv");

        try (
                CSVReader reader = new CSVReaderBuilder(new FileReader(fileToDeleteFrom))
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
            if (fileToDeleteFrom.delete() && tempFile.renameTo(fileToDeleteFrom)) {
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

    public static void main(String[] args) {
        String filePath = "testnames.csv";
        boolean result = deleteFromFile("newN", filePath);
        System.out.println("Delete successful: " + result);
    }

    public static boolean addToFile(ArrayList<String> newLandmark, String absolutePath) {
        if (newLandmark == null || newLandmark.isEmpty()) {
            System.err.println("Error: Empty landmark data.");
            return false;
        }

        File file = new File(absolutePath);
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
}