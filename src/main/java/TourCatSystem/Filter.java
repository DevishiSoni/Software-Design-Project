package TourCatSystem;

import TourCatData.LocationData; // Import the DTO
import TourCatService.LocationService;
// No longer need FileManager here if LocationService handles everything
// No longer need Search class

import TourCatData.DatabaseManager; // Needed for main method example setup
import TourCatData.FileManager;     // Needed for main method example setup
import java.io.IOException;       // Needed for main method example setup


import java.util.ArrayList;
import java.util.List; // Use List interface

public class Filter {

    // Store LocationData objects, use List interface
    private List<LocationData> results;
    // Keep hardcoded lists for now, but they could come from service/data later
    private final List<String> provinces;
    private final List<String> types;

    // Remove static province and type variables - they are incorrect
    // static String province;
    // static String type;

    private final LocationService locationService; // Make final

    public Filter(LocationService locationService){
        if (locationService == null) {
            throw new IllegalArgumentException("LocationService cannot be null");
        }
        this.locationService = locationService;

        // Initialize lists
        provinces = new ArrayList<>();
        types = new ArrayList<>();
        results = new ArrayList<>(); // Initialize as empty List<LocationData>

        // Populate hardcoded types and provinces
        types.add("Park");
        types.add("Historic Site");
        types.add("Bridge");
        types.add("Waterfall");
        types.add("Landmark"); // Add any others used
        types.add("Lake");

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

    /**
     * Filters locations by the selected province using the LocationService.
     * Updates the internal results list.
     * @param selectedProvince The province to filter by.
     */
    public void filterProvince(String selectedProvince) {
        // province = selectedProvince; // REMOVE - was static and incorrect
        results.clear();
        // Use the service, passing null for parameters not being filtered
        results = locationService.findLocations(null, selectedProvince, null);
    }


    /**
     * Filters locations by the selected type (category) using the LocationService.
     * Updates the internal results list.
     * @param selectedType The category/type to filter by.
     */
    public void filterType(String selectedType) {
        // Search searchObj = new Search(); // REMOVE Search dependency
        // type = selectedType; // REMOVE - was static and incorrect
        results.clear();
        // results = searchObj.search(database, type); // REMOVE Search dependency
        // Use the service, passing null for parameters not being filtered
        results = locationService.findLocations(null, null, selectedType);
    }

    /**
     * Filters locations by both province and type (category) using the LocationService.
     * Updates the internal results list.
     * @param selectedProvince The province to filter by.
     * @param selectedType The category/type to filter by.
     */
    public void filterBoth(String selectedProvince, String selectedType) {
        // Search searchObj = new Search(); // REMOVE Search dependency
        // province = selectedProvince; // REMOVE - was static and incorrect
        // type = selectedType; // REMOVE - was static and incorrect
        results.clear();

        // Remove manual filtering - let the service handle it
        // ArrayList<String> firstResults = searchObj.search(database, province);
        // for(String line : firstResults){
        //     if(line.toLowerCase().contains(type.toLowerCase())){
        //         results.add(line); // This was adding String, but results should be LocationData
        //     }
        // }
        // Use the service, passing both filters
        results = locationService.findLocations(null, selectedProvince, selectedType);
    }

    /**
     * Gets the list of LocationData objects from the last filter operation.
     * @return A List of LocationData objects.
     */
    public List<LocationData> getResults() { // Return type changed to List<LocationData>
        // Return a defensive copy if modification outside this class is a concern
        // return new ArrayList<>(results);
        return results;
    }

    /**
     * Prints the details of the locations in the results list to the console.
     */
    public void printResults() {
        if (results == null || results.isEmpty()) { // Check for null too
            System.out.println("No matching results found.");
        } else {
            System.out.println("Found " + results.size() + " results:");
            // Iterate through LocationData objects and print details
            for (LocationData loc : results) {
                System.out.println("  - " + loc.getName() + " (" + loc.getCategory() + ") in " +
                        (loc.getCity() != null ? loc.getCity() + ", " : "") + loc.getProvince() +
                        " [ID: " + loc.getId() + "]");
                // Or use a well-defined toString() in LocationData:
                // System.out.println("  " + loc.toString());
            }
        }
    }

    /**
     * Clears the internal results list.
     */
    public void reset() {
        if (results != null) {
            results.clear();
        }
    }
}