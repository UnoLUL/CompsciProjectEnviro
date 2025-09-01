package data;

import java.io.IOException;

public class TestLoader {
    public static void main(String[] args) {
        try {
            DataLoader loader = new DataLoader();
            loader.loadCSV("data/co2-emissions-per-capita.csv");
            System.out.println("Countries in dataset: " + loader.getCountries());
            System.out.println("Headers: " + loader.getHeaders());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
