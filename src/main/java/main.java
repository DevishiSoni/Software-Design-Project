import TourCatData.DatabaseManager;
import TourCatData.FileManager;
import TourCatGUI.HomePage;
import TourCatService.LocationService;

public class main {
    public static void main(String[] args) {

        FileManager fileManager = FileManager.getInstance();
        DatabaseManager databaseManager = new DatabaseManager();

        LocationService service = new LocationService(databaseManager, fileManager);

        HomePage homePage = new HomePage(null, service);

        System.out.println("Startup!!!!");
    }
}
