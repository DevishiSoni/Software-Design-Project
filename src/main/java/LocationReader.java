import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

//The location reader class will be the class responsible for reading from
//the csv file.
public class LocationReader {

    public static void main(String[] args) throws FileNotFoundException {
        String filePath = new File("").getAbsolutePath();
        filePath += "/geonames.csv";

        CSVReader reader = null;

        try {
            reader = new CSVReader(new FileReader(filePath));
            String[] nextLine;

            while((nextLine = reader.readNext()) != null)
            {
                for(String token: nextLine)
                {
                    System.out.println(token);
                }
            }
        } catch (CsvValidationException | IOException e) {
            e.printStackTrace();
        }

    }
}
