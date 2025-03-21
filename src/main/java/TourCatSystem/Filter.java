package TourCatSystem;

import java.util.ArrayList;

public class Filter {
    private ArrayList<String> results;
    private ArrayList<String> provinces;
    private ArrayList<String> types;

    static String province;
    static String type;

    private static final String filepath = String.valueOf(FileManager.getInstance().getResourceFile("test.csv"));;




    public Filter(){
        provinces = new ArrayList<>();
        types = new ArrayList<>();
        results = new ArrayList<>();

        types.add("Park");
        types.add("Historic Site");
        types.add("Bridge");
        types.add("Waterfall");


        provinces.add("Ontario");
        provinces.add("Quebec");
        provinces.add("British Columbia");
        provinces.add("Alberta");
        provinces.add("Manitoba");
        provinces.add("Saskatchewan");
        provinces.add("Nova Scotia");
        provinces.add("New Brunswick");
        provinces.add("Prince Edward Island");
        provinces.add("Newfoundland and Labrador");

    }

    // User clicks 1 chosen province

    public void filterProvince(String selectedProvince) {
        Search searchObj = new Search();
        province = selectedProvince; // Dropdown input
        results.clear();
        results = searchObj.search(filepath, province);
    }


    // User clicks 1 chosen type

    public void filterType(String selectedType) {
        Search searchObj = new Search();
        type = selectedType; // Dropdown input
        results.clear();
        results = searchObj.search(filepath, type);
    }

    // User clicks 1 chosen province AND 1 chosen type

    public void filterBoth(String selectedProvince, String selectedType) {
        Search searchObj = new Search();
        province = selectedProvince; // Dropdown input
        type = selectedType; // Dropdown input
        results.clear();

        ArrayList<String> firstResults = searchObj.search(filepath, province);

        for(String line : firstResults){
            if(line.toLowerCase().contains(type.toLowerCase())){
                results.add(line);
            }
        }
    }
    public ArrayList<String> getResults() {
        return results;
    }

    public void printResults() {
        if (results.isEmpty()) {
            System.out.println("No matching results found.");
        } else {
            for (String result : results) {
                System.out.println(result);
            }
        }
    }


    public static void main(String[] args) {
        Filter filter = new Filter();

        // Search by province
        filter.filterProvince("Ontario");
        System.out.println("\nShowing results for province: " + province);
        filter.printResults();

        // Search by type
        filter.filterType("Lake");
        System.out.println("\nShowing results for type: " + type);
        filter.printResults();

        // Search by both province and type
        filter.filterBoth("Ontario", "Water");
        System.out.println("\nShowing results for both province and type: " + province + " ," + type);
        filter.printResults();
    }
}

