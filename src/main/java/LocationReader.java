import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

//The location reader class will be the class responsible for reading from
//the csv file.
public class LocationReader {

    public static void main(String[] args) throws FileNotFoundException {
        String filePath = new File("").getAbsolutePath();
        filePath += "/geonames.csv";

        Scanner sc = new Scanner(new File(filePath));
        //sc.useDelimiter(",");


        System.out.print(filePath);

        while(sc.hasNext()) System.out.println(sc.next());

        sc.close();

    }
}
