import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Search {
    public static void search(String filepath){

        //To receive user input
        Scanner scanner = new Scanner(System.in);
        System.out.println("Search: ");
        String input = scanner.nextLine().toLowerCase();
        boolean found = false;

        //Read the given file to search through it
        try{

            //Read file *SWITCH TO LocationReader WHEN IT'S COMPLETE*
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath));
            String line;

            //Print any matches found
            while((line = bufferedReader.readLine()) != null){
                if(line.toLowerCase().contains(input)){
                    System.out.println("Matches: " + line);
                    found = true;
                }
            }

            //Print a message if there are no matches
            if(!found){
                System.out.println("No matches found.");
            }

            //Catch an error
        } catch (IOException e) {
            System.out.println("Error: "+e.getMessage());
        }
        scanner.close();
    }

    public static void main(String[] args) {
        search("geonames.csv");
    }
}

