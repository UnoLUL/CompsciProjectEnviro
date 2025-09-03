package data;

import java.io.IOException;

public class TestLoader {
    public static void main(String[] args) {
        try {
            DataLoader loader = new DataLoader(); //create a new instance of DataLoader
            loader.loadCSV("data/co2-emissions-per-capita.csv"); //use the loadCSV function and load the file
            System.out.println("Countries in dataset: " + loader.getCountries()); // print out each country using the getCountries method
            System.out.println("Headers: " + loader.getHeaders());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
