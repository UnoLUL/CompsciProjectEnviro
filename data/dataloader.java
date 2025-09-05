package data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DataLoader {
    // making lists to store the data for later.
    private List<Map<String, String>> data;
    private List<String> headers;

    public DataLoader() { //main dataloader, can be called to pull out the data from whatever type of excel doc
        this.data = new ArrayList<>();
        this.headers = new ArrayList<>();
    }

    // Load CSV file into memory, then read all files into filePath
    public void loadCSV(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        if (lines.isEmpty()) {
            throw new IOException("CSV file cannot be read (or is empty)");
        }

        // Extract headers from the excel documents, then add a comment between each entry
        headers = Arrays.asList(lines.get(0).split(","));

        // Load and clean each row for use
        for (int i = 1; i < lines.size(); i++) { // for loop that interates through a hashmap that helps with the excel document.
            String[] values = lines.get(i).split(",");
            if (values.length == headers.size()) {
                Map<String, String> row = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) { // trimming the values to make them better formatted
                    row.put(headers.get(j), values[j].trim());
                }  // getting the headers from an array.
                data.add(row); 
            }
        }
    }

    // Get unique countries from datast
    public Set<String> getCountries() {
        Set<String> countries = new TreeSet<>(); //creates a set of countries.
        for (Map<String, String> row : data) {
            countries.add(row.get("Entity"));
        }
        return countries;
    }

    // Filter data by a specific country
    public List<Map<String, String>> filterByCountry(String country) { //Filtering by country
        List<Map<String, String>> filtered = new ArrayList<>();
        for (Map<String, String> row : data) {
            if (row.get("Entity").equalsIgnoreCase(country)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    // Get the full dataset
    public List<Map<String, String>> getData() {
        return data;
    }

    // pull headers from the list
    public List<String> getHeaders() {
        return headers;
    }
}
