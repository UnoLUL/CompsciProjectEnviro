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

import java.io.File;
import java.util.*;

public class MainApp extends Application {
    private DataLoader loader = new DataLoader();
    private DataAnalyser analyser = new DataAnalyser(loader);

    private ComboBox<String> countryBox = new ComboBox<>();
    private ComboBox<Integer> yearBox = new ComboBox<>();
    private LineChart<Number, Number> lineChart;
    private BarChart<String, Number> barChart;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        Button loadBtn = new Button("Load CSV");
        loadBtn.setOnAction(e -> loadCSV(primaryStage));

        countryBox.setPromptText("Select Country");
        yearBox.setPromptText("Select Year");

        countryBox.setOnAction(e -> updateCharts());
        yearBox.setOnAction(e -> updateCharts());

        HBox controls = new HBox(10, loadBtn, countryBox, yearBox);

        // Line Chart: Emissions over time
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Year");
        yAxis.setLabel("CO₂ Emissions per Capita");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Emissions Over Time");

        // Bar Chart: Average emissions per country
        CategoryAxis barXAxis = new CategoryAxis();
        NumberAxis barYAxis = new NumberAxis();
        barChart = new BarChart<>(barXAxis, barYAxis);
        barChart.setTitle("Average Emissions per Country");

        TabPane tabPane = new TabPane();
        Tab lineTab = new Tab("Line Chart", lineChart);
        Tab barTab = new Tab("Bar Chart", barChart);
        tabPane.getTabs().addAll(lineTab, barTab);

        root.setTop(controls);
        root.setCenter(tabPane);

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
                countryBox.setItems(FXCollections.observableArrayList(loader.getCountries()));
                Set<Integer> years = new TreeSet<>();
                for (DataRecord r : loader.getData()) years.add(r.getYear());
                yearBox.setItems(FXCollections.observableArrayList(years));
                updateCharts();
            } catch (Exception ex) {
                showError("Failed to load CSV: " + ex.getMessage());
            }
        }
    }

    private void updateCharts() {
        String selectedCountry = countryBox.getValue();
        Integer selectedYear = yearBox.getValue();

        // Line chart: emissions over time for selected country
        lineChart.getData().clear();
        if (selectedCountry != null) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(selectedCountry);
            loader.getData().stream()
                    .filter(r -> r.getCountry().equals(selectedCountry))
                    .sorted(Comparator.comparingInt(DataRecord::getYear))
                    .forEach(r -> series.getData().add(new XYChart.Data<>(r.getYear(), r.getEmission())));
            lineChart.getData().add(series);
        }

        // Bar chart: average emissions per country
        barChart.getData().clear();
        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Average Emissions");
        analyser.getAverageEmissions().forEach((country, avg) -> {
            avgSeries.getData().add(new XYChart.Data<>(country, avg));
        });
        barChart.getData().add(avgSeries);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}