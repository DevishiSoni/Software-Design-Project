package TourCatSystem;
import TourCatData.FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Search {
    public static ArrayList<String> search(File file, String query) {
        ArrayList<String> results = new ArrayList<>();
        boolean found = false;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.toLowerCase().contains(query.toLowerCase())) {
//                    System.out.println("Match: " + line);
                    found = true;
                    results.add(line);
                }
            }

            if (!found) {
//                System.out.println("No matches found.");
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return results;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
//        System.out.print("Search: ");
        String input = scanner.nextLine();
        scanner.close();

        search(FileManager.getInstance().getDatabaseFile(), input);
    }
}
