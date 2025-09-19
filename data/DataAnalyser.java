package data;

import java.util.*;
import java.util.stream.*;

public class DataAnalyser {
    private DataLoader loader;

    public DataAnalyser(DataLoader loader) {
        this.loader = loader;
    }
    // via some different java util imports i can pull the data from the excel spreadsheet (using the dataloader) and sort it into a better format.
    public Map<String, Double> getAverageEmissions() {
        return loader.getData().stream()
                .collect(Collectors.groupingBy(
                        DataRecord::getCountry,
                        Collectors.averagingDouble(DataRecord::getEmission)
                ));
    }

    public List<DataRecord> getEmissionsByYear(int year) {
        return loader.getData().stream()
                .filter(record -> record.getYear() == year)
                .collect(Collectors.toList());
    }

    public Map<String, DoubleSummaryStatistics> getMinMaxEmissions() {
        return loader.getData().stream()
                .collect(Collectors.groupingBy(
                        DataRecord::getCountry,
                        Collectors.summarizingDouble(DataRecord::getEmission)
                ));
    }
}
