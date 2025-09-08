package data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataAnalyser {
    private DataLoader loader;

    public DataAnalyser(DataLoader loader) {
        this.loader = loader;
    }

    // Example method: average emissions per country
    public Map<String, Double> getAverageEmissions() {
        return loader.getData().stream()
                .collect(Collectors.groupingBy(
                        DataRecord::getCountry,
                        Collectors.averagingDouble(DataRecord::getEmission)
                ));
    }

    // Example method: emissions for a specific year
    public List<DataRecord> getEmissionsByYear(int year) {
        return loader.getData().stream()
                .filter(record -> record.getYear() == year)
                .collect(Collectors.toList());
    }

    // Example method: min/max emissions for each country
    public Map<String, DoubleSummaryStatistics> getMinMaxEmissions() {
        return loader.getData().stream()
                .collect(Collectors.groupingBy(
                        DataRecord::getCountry,
                        Collectors.summarizingDouble(DataRecord::getEmission)
