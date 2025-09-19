package data;

import java.io.*;
import java.util.*;

public class DataLoader {
    private List<DataRecord> data = new ArrayList<>();
    private List<String> headers = new ArrayList<>();
   /*The loadCSV method is used when the user clicks on the button in the program (the call is inside MainApp) and then
    *  the data is cleared from anything previously being used and then it checks if the document is the correct format, puts it into an array
    *  and then sets different variables (int , double) to the collums for later use.
    */
    public void loadCSV(String path) throws IOException {
        data.clear();
        headers.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            if (line != null) {
                headers = Arrays.asList(line.split(","));
            }
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 3) {
                    String country = tokens[0];
                    int year = Integer.parseInt(tokens[1]);
                    double emission = Double.parseDouble(tokens[2]);
                    data.add(new DataRecord(country, year, emission));
                }
            }
        }
    } 

    public List<DataRecord> getData() { return data; }
    public List<String> getHeaders() { return headers; }
    public Set<String> getCountries() {
        Set<String> countries = new HashSet<>();
        for (DataRecord record : data) {
            countries.add(record.getCountry());
        }
        return countries;
    }
}