import TourCatData.DatabaseManager;
import TourCatData.FileManager;
import TourCatGUI.HomePage;
import TourCatService.LocationService;

import java.io.IOException;

public class main {
    public static void main(String[] args) {

        FileManager fileManager = FileManager.getInstance();
        DatabaseManager databaseManager = null;
        try {
            databaseManager = new DatabaseManager(fileManager.getDatabaseFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LocationService service = new LocationService(databaseManager, fileManager);

        HomePage homePage = new HomePage(null, service);

        System.out.println("Startup!!!!");
    }
}
