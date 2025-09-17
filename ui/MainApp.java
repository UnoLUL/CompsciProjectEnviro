package ui;

import data.*;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;
import java.util.DoubleSummaryStatistics;

public class MainApp extends Application {
    private DataLoader loader;
    private DataAnalyser analyser;
    private LineChart<Number, Number> lineChart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private ComboBox<String> countryBox1;
    private ComboBox<String> countryBox2;
    private VBox statsContent;
    private Label statusLabel;
    private List<DataRecord> data;
    private int exportCounter = 1;

    // Color scheme inspired by Claude AI
    private final String PRIMARY_BG = "#1a1a1a";
    private final String SECONDARY_BG = "#2d2d30";
    private final String CARD_BG = "#1a1a1a";
    private final String TEXT_PRIMARY = "#ffffff";
    private final String TEXT_SECONDARY = "#ffffff";
    private final String ACCENT_ORANGE = "#ff6b35";
    private final String ACCENT_BLUE = "#4a9eff";
    private final String SUCCESS_GREEN = "#10b981";
    private final String BORDER_COLOR = "#4a4a4a";

    @Override
    public void start(Stage primaryStage) {
        loader = new DataLoader();
        analyser = new DataAnalyser(loader);

        Scene scene = new Scene(createMainLayout(), 1400, 800);
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Climate Data Visualizer");
        primaryStage.show();
        
        // Apply custom styling after scene is shown
        applyCustomStyles(scene);
        
        updateCharts();
    }

    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + PRIMARY_BG + ";");

        // Header
        root.setTop(createHeader());
        
        // Main content area
        HBox mainContent = new HBox();
        mainContent.setSpacing(20);
        mainContent.setPadding(new Insets(20));
        
        // Chart area
        VBox chartContainer = createChartContainer();
        HBox.setHgrow(chartContainer, Priority.ALWAYS);
        
        // Stats panel
        VBox statsPanel = createStatsPanel();
        
        mainContent.getChildren().addAll(chartContainer, statsPanel);
        root.setCenter(mainContent);
        
        // Status bar
        root.setBottom(createStatusBar());
        
        return root;
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");
        header.setPadding(new Insets(20));
        header.setSpacing(15);

        // Title
        Label title = new Label("Climate Data Visualizer");
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        
        Button loadBtn = createStyledButton("Load Data", ACCENT_BLUE);
        loadBtn.setOnAction(e -> loadCSV((Stage) loadBtn.getScene().getWindow()));
        
        countryBox1 = createStyledComboBox("Select first country");
        countryBox1.setOnAction(e -> updateCharts());
        
        countryBox2 = createStyledComboBox("Select second country");
        countryBox2.setOnAction(e -> updateCharts());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button exportBtn = createStyledButton("Export Chart", ACCENT_ORANGE);
        exportBtn.setOnAction(e -> exportChartAsPNG());
        
        controls.getChildren().addAll(loadBtn, new Separator(), countryBox1, countryBox2, spacer, exportBtn);
        
        header.getChildren().addAll(title, controls);
        return header;
    }

    private VBox createChartContainer() {
        VBox container = new VBox();
        container.setSpacing(10);
        
        // Chart setup with modern styling
        xAxis = new NumberAxis();
        xAxis.setLabel("Year");
        xAxis.setAutoRanging(false);
        
        yAxis = new NumberAxis();
        yAxis.setLabel("CO₂ Emissions (tonnes per capita)");
        yAxis.setAutoRanging(true);
        
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Climate Data Comparison");
        lineChart.setLegendVisible(true);
        lineChart.setCreateSymbols(true);
        lineChart.setAnimated(true); // Re-enable animations
        lineChart.setStyle(
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-border-color: " + BORDER_COLOR + ";" + 
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );
        
        VBox.setVgrow(lineChart, Priority.ALWAYS);
        container.getChildren().add(lineChart);
        
        return container;
    }

    private VBox createStatsPanel() {
        VBox panel = new VBox();
        panel.setPrefWidth(350);
        panel.setMaxWidth(350);
        panel.setSpacing(20);
        panel.setStyle("-fx-background-color: " + CARD_BG + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;");
        
        Label statsTitle = new Label("Statistics");
        statsTitle.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        statsContent = new VBox(15);
        statsContent.setPadding(new Insets(10, 0, 0, 0));
        scrollPane.setContent(statsContent);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        panel.getChildren().addAll(statsTitle, new Separator(), scrollPane);
        
        return panel;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(10, 20, 10, 20));
        statusBar.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1 0 0 0;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("Ready to load data");
        statusLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        statusBar.getChildren().add(statusLabel);
        return statusBar;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-opacity: 0.9;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-opacity: 0.9;", "")));
        
        return button;
    }

    private ComboBox<String> createStyledComboBox(String prompt) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText(prompt);
        comboBox.setPrefWidth(200);
        comboBox.setStyle(
            "-fx-background-color: " + SECONDARY_BG + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 8 12 8 12;"
        );
        return comboBox;
    }

    private void applyCustomStyles(Scene scene) {
        // Apply dark theme styles to chart components
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        
        // Force chart background and styling
        if (lineChart != null) {
            // Apply dark theme to chart
            lineChart.lookup(".chart").setStyle(
                "-fx-background-color: " + CARD_BG + ";"
            );
            
            lineChart.lookup(".chart-plot-background").setStyle(
                "-fx-background-color: " + CARD_BG + ";"
            );
            
            lineChart.lookup(".chart-content").setStyle(
                "-fx-padding: 20px;"
            );
            
            // Style axes
            lineChart.lookup(".axis").setStyle(
                "-fx-tick-label-fill: " + TEXT_PRIMARY + ";" +
                "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;"
            );
            
            // Style axis labels
            lineChart.lookupAll(".axis-label").forEach(node -> {
                node.setStyle(
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
                    "-fx-font-weight: bold;"
                );
            });
            
            // Style chart title
            lineChart.lookup(".chart-title").setStyle(
                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
                "-fx-font-weight: bold;"
            );
            
            // Style legend
            lineChart.lookup(".chart-legend").setStyle(
                "-fx-background-color: " + SECONDARY_BG + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 10;"
            );
            
            lineChart.lookupAll(".chart-legend-item").forEach(node -> {
                node.setStyle(
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;"
                );
            });
            
            // Keep the colored symbols visible (don't override their colors)
            // The legend symbols will automatically match the line colors
            
            // Style grid lines
            lineChart.lookupAll(".chart-vertical-grid-lines").forEach(node -> {
                node.setStyle("-fx-stroke: #404040;");
            });
            
            lineChart.lookupAll(".chart-horizontal-grid-lines").forEach(node -> {
                node.setStyle("-fx-stroke: #404040;");
            });
        }
        
        // Style combo box dropdowns
        scene.getRoot().lookupAll(".combo-box-popup .list-view").forEach(node -> {
            node.setStyle(
                "-fx-background-color: " + SECONDARY_BG + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;"
            );
        });
        
        scene.getRoot().lookupAll(".combo-box .list-cell").forEach(node -> {
            node.setStyle(
                "-fx-background-color: " + SECONDARY_BG + ";" +
                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                "-fx-padding: 8 12 8 12;" +
                "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;"
            );
        });
    }

    private void loadCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Climate Data CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                statusLabel.setText("Loading data...");
                loader.loadCSV(file.getAbsolutePath());
                data = loader.getData();
                
                countryBox1.getItems().setAll(loader.getCountries());
                countryBox2.getItems().setAll(loader.getCountries());
                
                configureYearAxis();
                updateCharts();
                
                // Apply styling after loading data
                javafx.application.Platform.runLater(() -> {
                    applyCustomStyles(stage.getScene());
                });
                
                statusLabel.setText("Loaded " + data.size() + " records from " + loader.getCountries().size() + " countries");
                statusLabel.setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
                
            } catch (Exception ex) {
                statusLabel.setText("Failed to load data: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: " + ACCENT_ORANGE + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
            }
        }
    }

    private void configureYearAxis() {
        if (data == null || data.isEmpty()) return;

        int minYear = data.stream().mapToInt(DataRecord::getYear).min().orElse(1900);
        int maxYear = data.stream().mapToInt(DataRecord::getYear).max().orElse(2025);

        xAxis.setLowerBound(minYear - 2);
        xAxis.setUpperBound(maxYear + 2);
        
        int yearRange = maxYear - minYear;
        int tickUnit = Math.max(1, yearRange / 8);
        xAxis.setTickUnit(tickUnit);
    }

    private void updateCharts() {
        lineChart.getData().clear();
        statsContent.getChildren().clear();

        if (data == null || data.isEmpty()) {
            Label noDataLabel = new Label("No data loaded");
            noDataLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px; -fx-padding: 20; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
            statsContent.getChildren().add(noDataLabel);
            return;
        }

        if (countryBox1.getValue() != null) {
            addCountrySeries(countryBox1.getValue(), ACCENT_BLUE);
        }
        if (countryBox2.getValue() != null) {
            addCountrySeries(countryBox2.getValue(), ACCENT_ORANGE);
        }

        updateStatistics();
        
        // Reapply chart styling after data is updated
        javafx.application.Platform.runLater(() -> {
            applyCustomStyles(lineChart.getScene());
        });
    }

    private void addCountrySeries(String country, String color) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(country);

        for (DataRecord record : data) {
            if (record.getCountry().equals(country)) {
                series.getData().add(new XYChart.Data<>(record.getYear(), record.getEmission()));
            }
        }

        lineChart.getData().add(series);
    }

    private void updateStatistics() {
        if (countryBox1.getValue() != null) {
            addCountryStatistics(countryBox1.getValue());
        }
        if (countryBox2.getValue() != null && !countryBox2.getValue().equals(countryBox1.getValue())) {
            addCountryStatistics(countryBox2.getValue());
        }
        
        if (statsContent.getChildren().isEmpty()) {
            Label selectLabel = new Label("Select countries above to view statistics");
            selectLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px; -fx-padding: 20; -fx-wrap-text: true; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
            statsContent.getChildren().add(selectLabel);
        }
    }

    private void addCountryStatistics(String country) {
        List<DataRecord> countryData = data.stream()
            .filter(record -> record.getCountry().equals(country))
            .toList();

        if (countryData.isEmpty()) return;

        DoubleSummaryStatistics stats = countryData.stream()
            .mapToDouble(DataRecord::getEmission)
            .summaryStatistics();

        int minYear = countryData.stream().mapToInt(DataRecord::getYear).min().orElse(0);
        int maxYear = countryData.stream().mapToInt(DataRecord::getYear).max().orElse(0);

        double firstValue = countryData.stream()
            .filter(r -> r.getYear() == minYear)
            .mapToDouble(DataRecord::getEmission)
            .findFirst().orElse(0);
            
        double lastValue = countryData.stream()
            .filter(r -> r.getYear() == maxYear)
            .mapToDouble(DataRecord::getEmission)
            .findFirst().orElse(0);

        double totalChange = lastValue - firstValue;

        VBox countryStats = new VBox(8);
        countryStats.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");
        
        Label countryTitle = new Label(country);
        countryTitle.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        VBox metrics = new VBox(6);
        metrics.getChildren().addAll(
            createStatLabel("Mean", String.format("%.3f", stats.getAverage())),
            createStatLabel("Median", String.format("%.3f", calculateMedian(countryData))),
            createStatLabel("Range", String.format("%.3f - %.3f", stats.getMin(), stats.getMax())),
            createStatLabel("Std Dev", String.format("%.3f", calculateStdDev(countryData, stats.getAverage()))),
            createStatLabel("Period", String.format("%d - %d", minYear, maxYear)),
            createStatLabel("Total Change", String.format("%.3f", totalChange), totalChange > 0 ? ACCENT_ORANGE : SUCCESS_GREEN)
        );
        
        countryStats.getChildren().addAll(countryTitle, metrics);
        statsContent.getChildren().add(countryStats);
    }

    private HBox createStatLabel(String label, String value) {
        return createStatLabel(label, value, TEXT_PRIMARY);
    }

    private HBox createStatLabel(String label, String value, String valueColor) {
        HBox statBox = new HBox();
        statBox.setSpacing(5);
        statBox.setAlignment(Pos.CENTER_LEFT);
        
        Label labelText = new Label(label + ":");
        labelText.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        labelText.setPrefWidth(85);
        
        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: " + valueColor + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        statBox.getChildren().addAll(labelText, valueText);
        return statBox;
    }

    private double calculateMedian(List<DataRecord> countryData) {
        List<Double> emissions = countryData.stream()
            .mapToDouble(DataRecord::getEmission)
            .sorted()
            .boxed()
            .toList();
        
        int size = emissions.size();
        if (size == 0) return 0;
        if (size % 2 == 0) {
            return (emissions.get(size/2 - 1) + emissions.get(size/2)) / 2.0;
        } else {
            return emissions.get(size/2);
        }
    }

    private double calculateStdDev(List<DataRecord> countryData, double mean) {
        if (countryData.size() <= 1) return 0;
        
        double sumSquaredDiffs = countryData.stream()
            .mapToDouble(DataRecord::getEmission)
            .map(x -> Math.pow(x - mean, 2))
            .sum();
        
        return Math.sqrt(sumSquaredDiffs / (countryData.size() - 1));
    }

    private void exportChartAsPNG() {
        String filename = "climate_chart_" + String.format("%03d", exportCounter) + ".png";
        File file = new File(System.getProperty("user.home") + File.separator + "Downloads", filename);
        
        file.getParentFile().mkdirs();
        
        try {
            javafx.scene.image.WritableImage image = lineChart.snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            
            exportCounter++;
            statusLabel.setText("Chart exported as: " + filename);
            statusLabel.setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
            
        } catch (Exception ex) {
            statusLabel.setText("Export failed: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: " + ACCENT_ORANGE + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}