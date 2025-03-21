package TourCatSystem;

public class Landmark {
    int ID;
    String GeographicalName;
    String LandmarkName;
    String City;
    PROVINCE Province;
    String Category;

    public enum PROVINCE {
        ONTARIO,
        QUEBEC,
        BRITISH_COLUMBIA,
        ALBERTA,
        MANITOBA,
        SASKATCHEWAN,
        NOVA_SCOTIA,
        NEW_BRUNSWICK,
        PRINCE_EDWARD_ISLAND,
        NEWFOUNDLAND_AND_LABRADOR,
        NORTHWEST_TERRITORIES,
        YUKON,
        NUNAVUT
    }

    public enum CATEGORYTYPE{
    }

    Landmark(int ID, String GeoName, String LandmarkName, String city, PROVINCE PROVINCE, String category)
    {
        this.ID = ID;
        this.GeographicalName = GeoName;
        this.LandmarkName = LandmarkName;
        this.City = city;
        this.Province = PROVINCE;
        this.Category = category;
    }




}

