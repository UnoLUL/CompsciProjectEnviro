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

import java.io.File;
import java.util.*;

public class MainApp extends Application {
    private DataLoader loader = new DataLoader();
    private DataAnalyser analyser = new DataAnalyser(loader);

    private ComboBox<String> countryBox1 = new ComboBox<>();
    private ComboBox<String> countryBox2 = new ComboBox<>();
    private LineChart<Number, Number> lineChart;

    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        Button loadBtn = new Button("Load CSV");
        loadBtn.setOnAction(e -> loadCSV(primaryStage));

        countryBox1.setPromptText("Select Country 1");
        countryBox2.setPromptText("Select Country 2");
        countryBox1.setOnAction(e -> updateCharts());
        countryBox2.setOnAction(e -> updateCharts());

        HBox controls = new HBox(10, loadBtn, countryBox1, countryBox2);

        // Line Chart: Emissions over time
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Year");
        yAxis.setLabel("CO₂ Emissions per Capita");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Emissions Over Time");

        root.setTop(controls);
        root.setCenter(lineChart);

        Scene scene = new Scene(root, 900, 600);
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
            } catch (Exception ex) {
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

        for (String country : Arrays.asList(selectedCountry1, selectedCountry2)) {
            if (country != null) {
                List<DataRecord> countryRecords = loader.getData().stream()
                        .filter(r -> r.getCountry().equals(country))
                        .sorted(Comparator.comparingInt(DataRecord::getYear))
                        .toList();
                if (!countryRecords.isEmpty()) {
                    countryRecordsList.add(countryRecords);
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
                String countryName = (i == 0) ? selectedCountry1 : selectedCountry2;
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
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}