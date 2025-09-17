package ui;

import data.*;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.collections.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.io.File;
import java.util.*;

public class MainApp extends Application {
    private DataLoader loader = new DataLoader();
    private DataAnalyser analyser = new DataAnalyser(loader);

    private ComboBox<String> countryBox1 = new ComboBox<>();
    private ComboBox<String> countryBox2 = new ComboBox<>();
    private LineChart<Number, Number> lineChart;
    private VBox statsPanel = new VBox(10);

    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        Button loadBtn = new Button("Load CSV");
        loadBtn.setOnAction(e -> loadCSV(primaryStage));

        countryBox1.setPromptText("Select Country 1");
        countryBox2.setPromptText("Select Country 2");
        countryBox1.setOnAction(e -> updateCharts());
        countryBox2.setOnAction(e -> updateCharts());

        HBox controls = new HBox(10, loadBtn, countryBox1, countryBox2);

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Year");
        yAxis.setLabel("CO₂ Emissions per Capita");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Emissions Over Time");

        statsPanel.setPadding(new Insets(10));
        statsPanel.setPrefWidth(300);

        root.setTop(controls);
        root.setCenter(lineChart);
        root.setRight(statsPanel);

        Scene scene = new Scene(root, 1200, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("CO₂ Emissions Visualiser");
        primaryStage.show();
    }

    private void loadCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV File");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                loader.loadCSV(file.getAbsolutePath());
                List<String> countries = new ArrayList<>(loader.getCountries());
                countryBox1.setItems(FXCollections.observableArrayList(countries));
                countryBox2.setItems(FXCollections.observableArrayList(countries));
                updateCharts();
            } catch (Exception ex) { //if the CSV is failing to load for some reason the program will let you know
                showError("Failed to load CSV: " + ex.getMessage());
            }
        }
    }

    private void updateCharts() {
        String selectedCountry1 = countryBox1.getValue();
        String selectedCountry2 = countryBox2.getValue();

        lineChart.getData().clear();
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();

        List<Integer> allYears = new ArrayList<>();
        List<List<DataRecord>> countryRecordsList = new ArrayList<>();
        List<String> countryNames = new ArrayList<>();

        for (String country : Arrays.asList(selectedCountry1, selectedCountry2)) {
            if (country != null) {
                List<DataRecord> countryRecords = loader.getData().stream()
                        .filter(r -> r.getCountry().equals(country))
                        .sorted(Comparator.comparingInt(DataRecord::getYear))
                        .toList();
                if (!countryRecords.isEmpty()) {
                    countryRecordsList.add(countryRecords);
                    countryNames.add(country);
                    allYears.add(countryRecords.get(0).getYear());
                    allYears.add(countryRecords.get(countryRecords.size() - 1).getYear());
                }
            }
        }

        if (!countryRecordsList.isEmpty()) {
            int minYear = Collections.min(allYears);
            int maxYear = Collections.max(allYears);

            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(minYear);
            xAxis.setUpperBound(maxYear);
            xAxis.setTickUnit(5);

            for (int i = 0; i < countryRecordsList.size(); i++) {
                List<DataRecord> records = countryRecordsList.get(i);
                String countryName = countryNames.get(i);
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(countryName);

                for (DataRecord r : records) {
                    series.getData().add(new XYChart.Data<>(r.getYear(), r.getEmission()));
                }
                lineChart.getData().add(series);
            }
        } else {
            xAxis.setAutoRanging(true);
        }

        // Update summary statistics panel
        statsPanel.getChildren().clear();
        for (int i = 0; i < countryRecordsList.size(); i++) {
            List<DataRecord> records = countryRecordsList.get(i);
            String countryName = countryNames.get(i);
            List<Double> emissions = records.stream().map(DataRecord::getEmission).toList();

            double mean = emissions.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
            double min = emissions.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN);
            double max = emissions.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
            double stddev = calcStdDev(emissions, mean);
            double median = calcMedian(emissions);
            Double mode = calcMode(emissions);

            Label title = new Label("Statistics for " + countryName);
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label meanLabel = new Label("Mean: " + String.format("%.3f", mean));
            Label medianLabel = new Label("Median: " + String.format("%.3f", median));
            Label modeLabel = new Label("Mode: " + (mode != null ? String.format("%.3f", mode) : "N/A"));
            Label minLabel = new Label("Min: " + String.format("%.3f", min));
            Label maxLabel = new Label("Max: " + String.format("%.3f", max));
            Label stdLabel = new Label("Std Dev: " + String.format("%.3f", stddev));
            Label yearsLabel = new Label("Year Range: " + records.get(0).getYear() + " - " + records.get(records.size()-1).getYear());
            Label changeLabel = new Label("Total Change: " + String.format("%.3f", records.get(records.size()-1).getEmission() - records.get(0).getEmission()));

            VBox stats = new VBox(3, title, meanLabel, medianLabel, modeLabel, minLabel, maxLabel, stdLabel, yearsLabel, changeLabel);
            stats.setStyle("-fx-border-color: #ccc; -fx-padding: 8px; -fx-background-color: #f9f9f9; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            statsPanel.getChildren().add(stats);
        }
    }

    // Helper methods for statistics
    private double calcMedian(List<Double> values) {
        if (values.isEmpty()) return Double.NaN;
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();
        if (n % 2 == 1) return sorted.get(n / 2);
        return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
    }

    private Double calcMode(List<Double> values) {
        if (values.isEmpty()) return null;
        Map<Double, Integer> freq = new HashMap<>();
        for (Double v : values) freq.put(v, freq.getOrDefault(v, 0) + 1);
        int maxFreq = Collections.max(freq.values());
        if (maxFreq == 1) return null; // No mode
        for (Map.Entry<Double, Integer> entry : freq.entrySet()) {
            if (entry.getValue() == maxFreq) return entry.getKey();
        }
        return null;
    }

    private double calcStdDev(List<Double> values, double mean) {
        if (values.isEmpty()) return Double.NaN;
        double sumSq = 0;
        for (double v : values) sumSq += Math.pow(v - mean, 2);
        return Math.sqrt(sumSq / values.size());
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}