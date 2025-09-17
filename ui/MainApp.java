package data;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MainApp extends Application {
    private DataLoader loader;
    private DataAnalyser analyser;
    private LineChart<Number, Number> lineChart;
    private ComboBox<String> countryBox1;
    private ComboBox<String> countryBox2;

    // Stats panel redone
    private BorderPane statsPanel;
    private VBox statsContent;
    private Button exportBtn;
    private Button exportPngBtn; 

    private List<EmissionRecord> data;

   
    public void start(Stage primaryStage) {
        loader = new DataLoader();
        analyser = new DataAnalyser(loader);

        // Controls
        Button loadBtn = new Button("Load CSV");
        loadBtn.setOnAction(e -> loadCSV(primaryStage));
        loadBtn.setPrefWidth(100);

        exportBtn = new Button("Export to CSV");
        exportBtn.setOnAction(e -> exportData());
        exportBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        exportBtn.setPrefWidth(120);

        exportPngBtn = new Button("Export Chart as PNG");
        exportPngBtn.setOnAction(e -> exportChartAsPNG());
        exportPngBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        exportPngBtn.setPrefWidth(160);

        countryBox1 = new ComboBox<>();
        countryBox2 = new ComboBox<>();
        countryBox1.setPromptText("Select Country 1");
        countryBox2.setPromptText("Select Country 2");
        countryBox1.setPrefWidth(160);
        countryBox2.setPrefWidth(160);

        // Prevent ComboBoxes from expanding and hiding buttons
        HBox controls = new HBox(10, loadBtn, exportBtn, exportPngBtn, countryBox1, countryBox2);
        controls.setPadding(new Insets(10));
        controls.setPrefHeight(50);
        HBox.setHgrow(loadBtn, Priority.NEVER);
        HBox.setHgrow(exportBtn, Priority.NEVER);
        HBox.setHgrow(exportPngBtn, Priority.NEVER);
        HBox.setHgrow(countryBox1, Priority.NEVER);
        HBox.setHgrow(countryBox2, Priority.NEVER);

        // Chart setup
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Year");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Emissions");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Climate Data");

        // Stats panel setup
        statsPanel = new BorderPane();
        statsPanel.setPadding(new Insets(10));
        statsPanel.setPrefWidth(300);

        statsContent = new VBox(10);
        statsPanel.setCenter(statsContent);

        // base layout
        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(lineChart);
        root.setRight(statsPanel);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Climate Data Visualizer");
        primaryStage.show();

        // ensure stats panel is initialised
        updateCharts();
    }

    private void loadCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Climate Data CSV");
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            loader.loadCSV(file.getAbsolutePath());
            data = loader.getData();
            countryBox1.getItems().setAll(loader.getCountries());
            countryBox2.getItems().setAll(loader.getCountries());
            updateCharts();
        }
    }

    private void updateCharts() {
        lineChart.getData().clear();
        statsContent.getChildren().clear();

        if (data == null || data.isEmpty()) return;

        if (countryBox1.getValue() != null) {
            addCountrySeries(countryBox1.getValue());
        }
        if (countryBox2.getValue() != null) {
            addCountrySeries(countryBox2.getValue());
        }

        Map<String, Double> avg = analyser.getAverageEmissions();
        for (Map.Entry<String, Double> e : avg.entrySet()) {
            statsContent.getChildren().add(
                new Label(e.getKey() + ": " + String.format("%.2f", e.getValue()))
            );
        }
    }

    private void addCountrySeries(String country) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(country);

        for (EmissionRecord record : data) {
            if (record.getCountry().equals(country)) {
                series.getData().add(new XYChart.Data<>(record.getYear(), record.getEmission()));
            }
        }

        lineChart.getData().add(series);
    }

    private void exportData() {
        if (data == null || data.isEmpty()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Exported Data");
        fileChooser.setInitialFileName("export.csv");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Country,Year,Emission\n");
                for (EmissionRecord record : data) {
                    writer.write(record.getCountry() + "," +
                                 record.getYear() + "," +
                                 record.getEmission() + "\n");
                }
                showAlert("Export Successful", "Data exported to " + file.getAbsolutePath());
            } catch (IOException ex) {
                showAlert("Export Failed", "Could not save file.");
            }
        }
    }

    private void exportChartAsPNG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Chart as PNG");
        fileChooser.setInitialFileName("chart.png");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                javafx.scene.image.WritableImage image = lineChart.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                showAlert("Export Successful", "Chart exported to " + file.getAbsolutePath());
            } catch (IOException ex) {
                showAlert("Export Failed", "Could not save PNG file.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
