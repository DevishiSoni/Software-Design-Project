import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.ArrayList;

public class ChangeDatabase {
    public static boolean deleteFromFile(String landmarkName, String filePath) {
        boolean success = false;
        int indexOfName = 1; // Adjust if necessary

        File inputFile = new File(filePath);
        File tempFile = new File(inputFile.getParent(), "temp.csv");

        try (
                CSVReader reader = new CSVReader(new FileReader(inputFile));
                CSVWriter writer = new CSVWriter(new FileWriter(tempFile),
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.NO_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END)
        ) {
            String[] nextLine;
            boolean found = false;

            while ((nextLine = reader.readNext()) != null) {
                if (!found && nextLine.length > indexOfName && landmarkName.equals(nextLine[indexOfName])) {
                    found = true; // Skip writing this line
                } else {
                    writer.writeNext(nextLine);
                }
            }

            if (!found) {
                System.out.println("Landmark not found.");
            }

            writer.flush();
        } catch (CsvValidationException | IOException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }

        // Replace original file with updated one
        if (inputFile.delete() && tempFile.renameTo(inputFile)) {
            success = true;
        } else {
            System.err.println("Could not replace original file.");
        }

        return success;
    }

    public static void main(String[] args) {
        String filePath = "testnames.csv";
        boolean result = deleteFromFile("CN Tower", filePath);
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
                CSVWriter writer = new CSVWriter(new FileWriter(file, true),
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.NO_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END)
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
