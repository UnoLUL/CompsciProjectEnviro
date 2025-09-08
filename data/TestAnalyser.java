package data;
import java.util.DoubleSummaryStatistics;

public class TestAnalyser {
    public static void main(String[] args) {
        // Load the CSV file
        DataLoader loader = new DataLoader("data/co2-emissions-per-capita.csv");

        // Create analyser with loader
        DataAnalyser analyser = new DataAnalyser(loader);

        // Average emissions per country
        System.out.println("Average emissions per country:");
        analyser.getAverageEmissions().forEach((country, avg) ->
                System.out.println(country + ": " + avg));

        // Emissions in 2000
        System.out.println("\nEmissions in 2000:");
        analyser.getEmissionsByYear(2000).forEach(record ->
                System.out.println(record.getCountry() + " -> " + record.getEmission()));

        // Min/max emissions per country
        System.out.println("\nMin/Max emissions per country:");
        analyser.getMinMaxEmissions().forEach((country, stats) -> {
            System.out.println(country + " -> Min: " + stats.getMin() + ", Max: " + stats.getMax());
        });
    }
}
