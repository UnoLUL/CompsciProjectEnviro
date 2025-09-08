import data.DataLoader;
import Analysis.DataAnalyser;

public class TestAnalyser {
    public static void main(String[] args) throws Exception {
        DataLoader loader = new DataLoader();
        loader.loadCSV("data/co2-emissions-per-capita.csv");

        DataAnalyser analyzer = new DataAnalyser(loader);

        System.out.println("Average emissions (Australia): " + analyzer.getAverageEmissions("Australia"));
        System.out.println("Emissions in 2000 (Australia): " + analyzer.getEmissionsByYear("Australia", "2000"));
        System.out.println("Min/Max emissions (Australia): " + analyzer.getMinMaxEmissions("Australia"));
    }
}
