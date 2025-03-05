import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.ArrayList;

public class ChangeDatabase {
    /**
     * This function adds to the database
     * @param landmark , ArrayList of Strings containing the name and location of the landmark
     * @param filePath , The String absolute path to the database
     * @return boolean if addition was successfully done
     **/
    public static boolean addToFile (ArrayList<String> landmark, String filePath) {
        // Assumption is location is a list containing the name and location

        boolean success = false;

        // Can read the file to find the index of the columns, with the column names being a String constant to change is necessary
        int indexOfName = 1;
        int indexOfLocation = 11;
        int numOfColumns = 16;
        File file = new File(filePath);

        // save the String inputs
        ArrayList<String> columns = new ArrayList<>() ;
        for (int i = 0; i < numOfColumns; i++) {columns.add("");}
        columns.add(indexOfName,landmark.getFirst());
        columns.add(indexOfLocation,landmark.getLast());

        //Populate a string in the csv format with the saved information
        String row = columns.getFirst();
        for(int i = 1; i < numOfColumns; i++){
            row = row + "," + columns.get(i);
        }

        //Open the file and print to it
        try {
            FileWriter fr = new FileWriter(file, true);
            PrintWriter printWriter = new PrintWriter(fr);
            printWriter.println(row);
            printWriter.flush();
            printWriter.close();
            success = true;
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return success;
    }

    /**
     * This function removes from the database@param landmarkName , A string containing the landmark name to remove
     * @param landmarkName , A String containing the name of the landmark to remove
     * @param filePath , The String absolute path to the database
     * @return boolean is deletion was successfully done
     **/
    public static boolean deleteFromFile (String landmarkName, String filePath) {

        boolean success = false;
        String databaseName = "geonames.csv";

        int indexOfName = 1;
        CSVReader reader = null;
        CSVWriter writer = null;

        //Create a new file to write to
        File resultFile = new File("reWrite.csv");
        String writerFilePath = resultFile.getAbsolutePath();

        try {
            //Open files with specifications
            reader = new CSVReader(new FileReader(filePath));
            writer = new CSVWriter(new FileWriter(writerFilePath),CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER,CSVWriter.NO_ESCAPE_CHARACTER,CSVWriter.DEFAULT_LINE_END);
            String[] nextLine = null;
            boolean found = false;

            //Read through the file, pasting each line into the new file only if the names do not match
            while((nextLine = reader.readNext()) != null)
            {
                if(!found && landmarkName.equals(nextLine[indexOfName])){
                    found = true;
                }
                else {
                    writer.writeNext(nextLine);
                }
            }

            reader.close();
            writer.close();

            //Delete the old database and rename the new one to follow project code guidelines
            new File(filePath).delete();
            resultFile.renameTo(new File(databaseName));
            success = true;
        }
        catch (CsvValidationException | IOException e) {
            System.out.println(e.getMessage());
        }

        return success;
    }

    public static void main(String[] args) {
        String filePath = new File("").getAbsolutePath();
        filePath += "/geonames.csv";

        ArrayList<String> locations = new ArrayList<>(2);
        locations.add("Pyramids");
        locations.add("Egypt");

        //addToFile(locations,filePath);
        //deleteFromFile(locations.getFirst(),filePath);
    }
    
}
