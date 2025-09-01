package data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DataLoader {

    private List<Map<String, String>> data;
    private List<String> headers;

    public DataLoader() {
        this.data = new ArrayList<>();
        this.headers = new ArrayList<>();
    }

    // Load CSV into memory
    public void loadCSV(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        if (lines.isEmpty()) {
            throw new IOException("CSV file is empty or cannot be read.");
        }

        // Extract headers
        headers = Arrays.asList(lines.get(0).split(","));

        // Load and clean each row
        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split(",");
            if (values.length == headers.size()) {
                Map<String, String> row = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    row.put(headers.get(j), values[j].trim());
                }
                data.add(row);
            }
        }
    }

    // Get unique countries from data
    public Set<String> getCountries() {
        Set<String> countries = new TreeSet<>();
        for (Map<String, String> row : data) {
            countries.add(row.get("Entity"));
        }
        return countries;
    }

    // Filter data by a specific country
    public List<Map<String, String>> filterByCountry(String country) {
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

    // Get headers
    public List<String> getHeaders() {
        return headers;
    }
}
