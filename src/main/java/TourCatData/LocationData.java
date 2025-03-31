package TourCatData;

public class LocationData {
    private final String id;
    private final String name;
    private final String city;
    private final String province;
    private final String category;
    // Maybe add image path later if needed, but ID is better

    public LocationData(String id, String name, String city, String province, String category) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.province = province;
        this.category = category;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getCategory() { return category; }

    // toString() for debugging can be useful
    @Override
    public String toString() {
        return "LocationData{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}