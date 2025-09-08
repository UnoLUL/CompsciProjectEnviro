package Analysis;

import data.DataLoader;
import java.util.*;

public class DataAnalyser {

    private DataLoader loader;

    public DataAnalyser(DataLoader loader) {
        this.loader = loader;
    }

    // Get average CO2 emissions for a given country
    public double getAverageEmissions(String country) {
        List<Map<String, String>> rows = loader.filterByCountry(country);
        double total = 0;
        int count = 0;

        for (Map<String, String> row : rows) {
            try {
                double value = Double.parseDouble(row.get("Annual CO₂ emissions (per capita)"));
                total += value;
                count++;
            } catch (NumberFormatException e) {
                
            }
        }
        return count > 0 ? total / count : 0;
    }

    // Get emissions in a given year
    public double getEmissionsByYear(String country, String year) {
        List<Map<String, String>> rows = loader.filterByCountry(country);
        for (Map<String, String> row : rows) {
            if (row.get("Year").equals(year)) {
                try {
                    return Double.parseDouble(row.get("Annual CO₂ emissions (per capita)"));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    // Get min & max year values
    public Map<String, Double> getMinMaxEmissions(String country) {
        List<Map<String, String>> rows = loader.filterByCountry(country);
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;

        for (Map<String, String> row : rows) {
            try {
                double value = Double.parseDouble(row.get("Annual CO₂ emissions (per capita)"));
                if (value < min) min = value;
                if (value > max) max = value;
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        Map<String, Double> result = new HashMap<>();
        result.put("min", min == Double.MAX_VALUE ? 0 : min);
        result.put("max", max == Double.MIN_VALUE ? 0 : max);
        return result;
    }
}
