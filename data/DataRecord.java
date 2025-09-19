package data;

public class DataRecord {
    private final String country;
    private final int year;
    private final double emission;

    public DataRecord(String country, int year, double emission) {
        this.country = country;
        this.year = year;
        this.emission = emission;
    }

    public String getCountry() { return country; }
    public int getYear() { return year; }
    public double getEmission() { return emission; }
}


//DataRecord has a some different methods that can be called by other programs to get different parts of the CSV file