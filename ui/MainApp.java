package ui;
// all major imports for javaFX
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
// some other imports for javaIO like lists, file and hashmaps.
import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Climate Data Visualizer - A  data visualisation application
 * for analysing and comparing CO2 emissions data across countries and time periods
 * the program allows users to select a csv file, then the program uses an algorithim i wrote to sort through all collums and get the raw data
 * afterwards it displays the data in graph format and allows the user to select the year periods and some basic or detailed stats depending on their 
 * interests, after everything is finished, if needed the user can export their graphs to PNG.
 */
public class MainApp extends Application {
    
    // ========== CORE COMPONENTS ==========
    private DataLoader loader;
    private DataAnalyser analyser;
    private List<DataRecord> data;
    
    // ========== UI COMPONENTS ==========
    // Chart components
    private LineChart<Number, Number> lineChart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    
    // Controls
    private ComboBox<String> countryBox1;
    private ComboBox<String> countryBox2;
    private Slider minYearSlider;
    private Slider maxYearSlider;
    private Label yearRangeLabel;
    
    // Layout panels
    private VBox statsContent;
    private VBox comparisonPanel;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;
    
    // ========== DATA MANAGEMENT ==========
    private ObservableList<String> allCountries = FXCollections.observableArrayList();
    private int globalMinYear = 1750;
    private int globalMaxYear = 2025;
    private int exportCounter = 1;
    
    // Performance caching so your computer doesnt blow up
    private Map<String, DoubleSummaryStatistics> statsCache = new HashMap<>();
    private Map<String, Double> medianCache = new HashMap<>();
    private Map<String, Double> stdDevCache = new HashMap<>();
    
    // ========== STYLING CONSTANTS ==========
    private final String PRIMARY_BG = "#1a1a1a";
    private final String SECONDARY_BG = "#2d2d30";
    private final String CARD_BG = "#1a1a1a";
    private final String TEXT_PRIMARY = "#ffffff";
    private final String TEXT_SECONDARY = "#ffffff";
    private final String ACCENT_ORANGE = "#ff6b35";
    private final String ACCENT_BLUE = "#4a9eff";
    private final String SUCCESS_GREEN = "#10b981";
    private final String BORDER_COLOR = "#4a4a4a";

    // ========== APPLICATION STARTUP ==========
    
  
    public void start(Stage primaryStage) {
        loader = new DataLoader();
        analyser = new DataAnalyser(loader);

        Scene scene = new Scene(createMainLayout(), 1600, 950);
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Climate Data Visualizer");
        primaryStage.show();
        
        applyCustomStyles(scene);
        updateCharts();
    }

    // ========== LAYOUT CREATION ==========
    
    /**
     * Creates the main application layout with header, content panels, and status bar
     */
    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + PRIMARY_BG + ";");

        root.setTop(createHeader());
        
        HBox mainContent = new HBox();
        mainContent.setSpacing(20);
        mainContent.setPadding(new Insets(20));
        
        VBox leftPanel = createLeftPanel();
        VBox chartContainer = createChartContainer();
        VBox statsPanel = createStatsPanel();
        
        HBox.setHgrow(chartContainer, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(leftPanel, chartContainer, statsPanel);
        root.setCenter(mainContent);
        root.setBottom(createStatusBar());
        
        return root;
    }

    /**
     * Creates the application header with title and main controls
     */
    private VBox createHeader() {
        VBox header = new VBox();
        header.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");
        header.setPadding(new Insets(20));
        header.setSpacing(15);

        // Application title
        Label title = new Label("Climate Data Visualizer");
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        // Main control bar
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        
        Button loadBtn = createStyledButton("Load Data", ACCENT_BLUE);
        loadBtn.setOnAction(e -> loadCSVAsync((Stage) loadBtn.getScene().getWindow()));
        
        countryBox1 = createSearchableComboBox("Search first country...");
        countryBox2 = createSearchableComboBox("Search second country...");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button exportBtn = createStyledButton("Export Chart", ACCENT_ORANGE);
        exportBtn.setOnAction(e -> exportChartAsPNG());
        
        controls.getChildren().addAll(loadBtn, new Separator(), countryBox1, countryBox2, spacer, exportBtn);
        header.getChildren().addAll(title, controls);
        return header;
    }

    /**
     * Creates the left control panel with year range controls and comparison
     */
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox();
        leftPanel.setSpacing(20);
        leftPanel.setPrefWidth(300);
        leftPanel.setMaxWidth(300);

        VBox yearControls = createYearRangeControls();
        comparisonPanel = createComparisonPanel();
        
        leftPanel.getChildren().addAll(yearControls, comparisonPanel);
        return leftPanel;
    }

    /**
     * Creates year range selection controls with sliders
     */
    private VBox createYearRangeControls() {
        VBox yearPanel = new VBox(15);
        yearPanel.setStyle("-fx-background-color: " + CARD_BG + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;");
        
        Label yearTitle = new Label("Year Range");
        yearTitle.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        yearRangeLabel = new Label("1750 - 2025");
        yearRangeLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        // Min year controls
        Label minLabel = new Label("From:");
        minLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        minYearSlider = new Slider(1750, 2020, 1750);
        minYearSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() >= maxYearSlider.getValue()) {
                minYearSlider.setValue(maxYearSlider.getValue() - 1);
            }
            updateYearRange();
        });
        javafx.application.Platform.runLater(() -> styleSliderComponents(minYearSlider));
        
        // Max year controls
        Label maxLabel = new Label("To:");
        maxLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        maxYearSlider = new Slider(1751, 2025, 2025);
        maxYearSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() <= minYearSlider.getValue()) {
                maxYearSlider.setValue(minYearSlider.getValue() + 1);
            }
            updateYearRange();
        });
        javafx.application.Platform.runLater(() -> styleSliderComponents(maxYearSlider));

        Button resetYearBtn = createStyledButton("Reset Range", ACCENT_BLUE);
        resetYearBtn.setPrefWidth(240);
        resetYearBtn.setOnAction(e -> resetYearRange());
        
        yearPanel.getChildren().addAll(yearTitle, yearRangeLabel, minLabel, minYearSlider, maxLabel, maxYearSlider, resetYearBtn);
        return yearPanel;
    }

    /**
     * Creates the quick comparison panel for country analysis
     */
    private VBox createComparisonPanel() {
        VBox panel = new VBox(15);
        panel.setStyle("-fx-background-color: " + CARD_BG + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;");
        
        Label compTitle = new Label("Quick Comparison");
        compTitle.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        panel.getChildren().add(compTitle);
        return panel;
    }

    /**
     * Creates the main chart container for data visualization
     */
    private VBox createChartContainer() {
        VBox container = new VBox();
        container.setSpacing(10);
        
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
        lineChart.setAnimated(true);
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

    /**
     * Creates the statistics panel for displaying country data
     */
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

    /**
     * Creates the bottom status bar with loading indicator,
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(10, 20, 10, 20));
        statusBar.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1 0 0 0;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("Ready to load data");
        statusLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(18, 18);
        loadingIndicator.setStyle(
            "-fx-progress-color: " + ACCENT_BLUE + ";" +
            "-fx-background-color: transparent;"
        );
        loadingIndicator.setVisible(false);
        
        statusBar.getChildren().addAll(statusLabel, spacer, loadingIndicator);
        return statusBar;
    }

    // ========== UI COMPONENT FACTORIES ==========
    
    /**
     * Creates a styled button with consistent theme and hover effects
     */
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
            "-fx-border-width: 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );
        
        // Hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: derive(" + color + ", 10%);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
                "-fx-padding: 10 20 10 20;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-width: 0;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);" +
                "-fx-scale-x: 1.02;" +
                "-fx-scale-y: 1.02;"
            );
        });
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
                "-fx-padding: 10 20 10 20;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-width: 0;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);" +
                "-fx-scale-x: 1.0;" +
                "-fx-scale-y: 1.0;"
            );
        });
        
        return button;
    }

    /**
     * Creates a searchable combo box with real-time filtering
     */
    private ComboBox<String> createSearchableComboBox(String prompt) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText(prompt);
        comboBox.setPrefWidth(250);
        comboBox.setEditable(true);
        comboBox.setVisibleRowCount(8);
        
        comboBox.setStyle(
            "-fx-background-color: " + SECONDARY_BG + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 12 8 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);"
        );

        // Style the text field
        comboBox.getEditor().setStyle(
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-background-color: transparent;" +
            "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
            "-fx-font-size: 13px;"
        );

        // Enable search functionality
        comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (comboBox.getItems().equals(allCountries)) {
                FilteredList<String> filteredItems = new FilteredList<>(allCountries);
                filteredItems.setPredicate(item -> {
                    if (newText == null || newText.isEmpty()) {
                        return true;
                    }
                    return item.toLowerCase().contains(newText.toLowerCase());
                });
                comboBox.setItems(filteredItems);
                if (!comboBox.isShowing() && !filteredItems.isEmpty()) {
                    comboBox.show();
                }
            }
        });

        comboBox.setOnAction(e -> updateChartsWithAnimation());
        return comboBox;
    }

    // ========== DATA LOADING ==========
    
    /**
     * Loads CSV data asynchronously with progress feedback
     */
    private void loadCSVAsync(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Climate Data CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            showLoading("Loading data...");
            
            CompletableFuture.supplyAsync(() -> {
                try {
                    loader.loadCSV(file.getAbsolutePath());
                    return loader.getData();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }).thenAcceptAsync(loadedData -> {
                javafx.application.Platform.runLater(() -> {
                    data = loadedData;
                    allCountries.setAll(loader.getCountries());
                    countryBox1.setItems(allCountries);
                    countryBox2.setItems(allCountries);
                    
                    configureYearAxis();
                    updateChartsWithAnimation();
                    applyCustomStyles(stage.getScene());
                    
                    hideLoading("Loaded " + data.size() + " records from " + loader.getCountries().size() + " countries", SUCCESS_GREEN);
                });
            }).exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    hideLoading("Failed to load data: " + ex.getCause().getMessage(), ACCENT_ORANGE);
                });
                return null;
            });
        }
    }

    // ========== CHART MANAGEMENT ==========
    
    /**
     * Configures the year axis based on loaded data range
     */
    private void configureYearAxis() {
        if (data == null || data.isEmpty()) return;

        globalMinYear = data.stream().mapToInt(DataRecord::getYear).min().orElse(1750);
        globalMaxYear = data.stream().mapToInt(DataRecord::getYear).max().orElse(2025);

        minYearSlider.setMin(globalMinYear);
        minYearSlider.setMax(globalMaxYear - 1);
        minYearSlider.setValue(globalMinYear);
        
        maxYearSlider.setMin(globalMinYear + 1);
        maxYearSlider.setMax(globalMaxYear);
        maxYearSlider.setValue(globalMaxYear);
        
        updateYearRange();
    }

    /**
     * Updates the chart's year range based on slider values
     */
    private void updateYearRange() {
        int minYear = (int) minYearSlider.getValue();
        int maxYear = (int) maxYearSlider.getValue();
        
        xAxis.setLowerBound(minYear - 2);
        xAxis.setUpperBound(maxYear + 2);
        
        int yearRange = maxYear - minYear;
        int tickUnit = Math.max(1, yearRange / 8);
        xAxis.setTickUnit(tickUnit);
        
        yearRangeLabel.setText(minYear + " - " + maxYear);
        
        if (data != null && !data.isEmpty()) {
            updateChartsWithAnimation();
        }
    }

    /**
     * Resets year sliders to full data range
     */
    private void resetYearRange() {
        minYearSlider.setValue(globalMinYear);
        maxYearSlider.setValue(globalMaxYear);
    }

    /**
     * Updates charts with smooth fade animation
     */
    private void updateChartsWithAnimation() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), lineChart);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);
        fadeOut.setOnFinished(e -> {
            updateCharts();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), lineChart);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    /**
     * Main chart update method - refreshes all chart content
     */
    private void updateCharts() {
        lineChart.getData().clear();
        statsContent.getChildren().clear();
        updateComparisonPanel();

        if (data == null || data.isEmpty()) {
            Label noDataLabel = new Label("No data loaded");
            noDataLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px; -fx-padding: 20; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
            statsContent.getChildren().add(noDataLabel);
            return;
        }

        String country1 = countryBox1.getValue();
        String country2 = countryBox2.getValue();

        if (country1 != null) {
            addCountrySeriesWithTooltips(country1, ACCENT_BLUE);
        }
        if (country2 != null && !country2.equals(country1)) {
            addCountrySeriesWithTooltips(country2, ACCENT_ORANGE);
        }

        updateStatistics();
        
        javafx.application.Platform.runLater(() -> {
            applyCustomStyles(lineChart.getScene());
        });
    }

    /**
     * Adds a country data series to the chart with interactive tooltips
     */
    private void addCountrySeriesWithTooltips(String country, String color) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(country);

        int minYear = (int) minYearSlider.getValue();
        int maxYear = (int) maxYearSlider.getValue();

        // Add filtered data points
        for (DataRecord record : data) {
            if (record.getCountry().equals(country) && 
                record.getYear() >= minYear && record.getYear() <= maxYear) {
                XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(record.getYear(), record.getEmission());
                series.getData().add(dataPoint);
            }
        }

        lineChart.getData().add(series);

        // Add tooltips to each data point
        series.getData().forEach(dataPoint -> {
            Tooltip tooltip = new Tooltip(
                String.format("%s\nYear: %d\nEmissions: %.3f tonnes per capita", 
                    country, dataPoint.getXValue().intValue(), dataPoint.getYValue().doubleValue())
            );
            tooltip.setStyle(
                "-fx-background-color: " + SECONDARY_BG + ";" +
                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
                "-fx-font-size: 12px;" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;"
            );
            
            dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, tooltip);
                }
            });
        });
    }

    // ========== STATISTICS AND COMPARISON ==========
    
    /**
     * Updates the statistics panel with country data
     */
    private void updateStatistics() {
        String country1 = countryBox1.getValue();
        String country2 = countryBox2.getValue();

        if (country1 != null) {
            addCountryStatistics(country1);
        }
        if (country2 != null && !country2.equals(country1)) {
            addCountryStatistics(country2);
        }
        
        if (statsContent.getChildren().isEmpty()) {
            Label selectLabel = new Label("Select countries above to view statistics");
            selectLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px; -fx-padding: 20; -fx-wrap-text: true; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
            statsContent.getChildren().add(selectLabel);
        }
    }

    /**
     * Updates the quick comparison panel with country analysis
     */
    private void updateComparisonPanel() {
        // Clear existing content (keep title)
        comparisonPanel.getChildren().removeIf(node -> !(node instanceof Label && ((Label) node).getText().equals("Quick Comparison")));

        String country1 = countryBox1.getValue();
        String country2 = countryBox2.getValue();

        if (country1 != null && country2 != null && !country1.equals(country2)) {
            List<DataRecord> data1 = getFilteredCountryData(country1);
            List<DataRecord> data2 = getFilteredCountryData(country2);

            if (!data1.isEmpty() && !data2.isEmpty()) {
                DoubleSummaryStatistics stats1 = getCachedStats(country1, data1);
                DoubleSummaryStatistics stats2 = getCachedStats(country2, data2);

                double diff = ((stats1.getAverage() - stats2.getAverage()) / stats2.getAverage()) * 100;
                String comparison;
                String color;

                if (Math.abs(diff) < 1) {
                    comparison = String.format("%s and %s have similar average emissions", country1, country2);
                    color = TEXT_PRIMARY;
                } else if (diff > 0) {
                    comparison = String.format("%s emits %.1f%% more than %s on average", country1, Math.abs(diff), country2);
                    color = ACCENT_ORANGE;
                } else {
                    comparison = String.format("%s emits %.1f%% less than %s on average", country1, Math.abs(diff), country2);
                    color = SUCCESS_GREEN;
                }

                Label comparisonLabel = new Label(comparison);
                comparisonLabel.setWrapText(true);
                comparisonLabel.setPrefWidth(260);
                comparisonLabel.setMaxWidth(260);
                comparisonLabel.setStyle(
                    "-fx-text-fill: " + color + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
                    "-fx-line-spacing: 2px;" +
                    "-fx-padding: 5px;" +
                    "-fx-text-alignment: left;"
                );

                // Peak information
                double peak1 = stats1.getMax();
                double peak2 = stats2.getMax();
                int peakYear1 = data1.stream().filter(r -> r.getEmission() == peak1).mapToInt(DataRecord::getYear).findFirst().orElse(0);
                int peakYear2 = data2.stream().filter(r -> r.getEmission() == peak2).mapToInt(DataRecord::getYear).findFirst().orElse(0);

                Label peakInfo = new Label(String.format("%s peaked in %d (%.3f)\n%s peaked in %d (%.3f)", 
                    country1, peakYear1, peak1, country2, peakYear2, peak2));
                peakInfo.setWrapText(true);
                peakInfo.setStyle(
                    "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;"
                );

                comparisonPanel.getChildren().addAll(new Separator(), comparisonLabel, peakInfo);
            }
        } else {
            Label noComparison = new Label("Select two countries to see comparison");
            noComparison.setWrapText(true);
            noComparison.setStyle(
                "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;"
            );
            comparisonPanel.getChildren().addAll(new Separator(), noComparison);
        }
    }

    /**
     * Adds detailed statistics for a country to the stats panel
     */
    private void addCountryStatistics(String country) {
        List<DataRecord> countryData = getFilteredCountryData(country);
        if (countryData.isEmpty()) return;

        String cacheKey = country + "_" + (int)minYearSlider.getValue() + "_" + (int)maxYearSlider.getValue();
        DoubleSummaryStatistics stats = getCachedStats(country, countryData);

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
        double median = medianCache.computeIfAbsent(cacheKey, k -> calculateMedian(countryData));
        double stdDev = stdDevCache.computeIfAbsent(cacheKey, k -> calculateStdDev(countryData, stats.getAverage()));

        VBox countryStats = new VBox(8);
        countryStats.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");
        
        Label countryTitle = new Label(country);
        countryTitle.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
        
        VBox metrics = new VBox(6);
        metrics.getChildren().addAll(
            createStatLabel("Mean", String.format("%.3f", stats.getAverage())),
            createStatLabel("Median", String.format("%.3f", median)),
            createStatLabel("Range", String.format("%.3f - %.3f", stats.getMin(), stats.getMax())),
            createStatLabel("Std Dev", String.format("%.3f", stdDev)),
            createStatLabel("Period", String.format("%d - %d", minYear, maxYear)),
            createStatLabel("Total Change", String.format("%.3f", totalChange), totalChange > 0 ? ACCENT_ORANGE : SUCCESS_GREEN)
        );
        
        countryStats.getChildren().addAll(countryTitle, metrics);
        statsContent.getChildren().add(countryStats);
    }

    /**
     * Creates a formatted statistics label
     */
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

    // ========== DATA UTILITIES ==========
    
    /**
     * Gets filtered country data based on current year range
     */
    private List<DataRecord> getFilteredCountryData(String country) {
        int minYear = (int) minYearSlider.getValue();
        int maxYear = (int) maxYearSlider.getValue();
        
        return data.stream()
            .filter(record -> record.getCountry().equals(country) && 
                    record.getYear() >= minYear && record.getYear() <= maxYear)
            .toList();
    }

    /**
     * Gets cached statistics for performance on lower end computers, before i added this and looked into performance gains even my laptop was having a hard time rendering
     */
    private DoubleSummaryStatistics getCachedStats(String country, List<DataRecord> countryData) {
        String cacheKey = country + "_" + (int)minYearSlider.getValue() + "_" + (int)maxYearSlider.getValue();
        return statsCache.computeIfAbsent(cacheKey, k -> 
            countryData.stream().mapToDouble(DataRecord::getEmission).summaryStatistics()
        );
    }

    /**
     * Calculates median value from data records
     */
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

    /**
     * Calculates standard deviation,
     */
    private double calculateStdDev(List<DataRecord> countryData, double mean) {
        if (countryData.size() <= 1) return 0;
        
        double sumSquaredDiffs = countryData.stream()
            .mapToDouble(DataRecord::getEmission)
            .map(x -> Math.pow(x - mean, 2))
            .sum();
        
        return Math.sqrt(sumSquaredDiffs / (countryData.size() - 1));
    }

    // ========== UI STATUS AND STYLING ==========
    
    /*
     * turns out, you can use JavaFX with a pseudo-css style font and colour manager and because i already know CSS implementation is quite easy.
     */


    /**
     * Shows loading state with progress indicator
     */
    private void showLoading(String message) {
        statusLabel.setText(message);
        loadingIndicator.setVisible(true);
    }

    /**
     * Hides loading state and shows result message
     */
    private void hideLoading(String message, String color) {
        loadingIndicator.setVisible(false);
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;");
    }

    /**
     * Applies custom dark theme styling to all components, it uses functionality from Css which is super cool because i already know it
     */
    private void applyCustomStyles(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        
        // Style sliders after rendering
        if (minYearSlider != null) {
            javafx.application.Platform.runLater(() -> {
                styleSliderComponents(minYearSlider);
                styleSliderComponents(maxYearSlider);
            });
        }
        
        if (lineChart != null) {
            // Chart backgrounds
            lineChart.lookup(".chart").setStyle("-fx-background-color: " + CARD_BG + ";");
            lineChart.lookup(".chart-plot-background").setStyle("-fx-background-color: " + CARD_BG + ";");
            lineChart.lookup(".chart-content").setStyle("-fx-padding: 20px;");
            
            // Chart text elements, sorts through font families, applies the ones i want and sets up backups if the user doesnt have the correct fonts.
            lineChart.lookup(".axis").setStyle(
                "-fx-tick-label-fill: " + TEXT_PRIMARY + ";" +
                "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;"
            );
            
            lineChart.lookupAll(".axis-label").forEach(node -> {
                node.setStyle(
                    "-fx-" +
                    "text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
                    "-fx-font-weight: bold;"
                );
            });
            
            lineChart.lookup(".chart-title").setStyle(
                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                "-fx-font-family: 'SF Pro Display', 'Helvetica Neue', 'Segoe UI', system-ui, sans-serif;" +
                "-fx-font-weight: bold;"
            );
            
            // Chart legend
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
            
            // Grid lines on the graph (they got messed up when i tried changing the colour from white to black)
            lineChart.lookupAll(".chart-vertical-grid-lines").forEach(node -> {
                node.setStyle("-fx-stroke: #404040;");
            });
            
            lineChart.lookupAll(".chart-horizontal-grid-lines").forEach(node -> {
                node.setStyle("-fx-stroke: #404040;");
            });
        }
        
        // Combo box styling
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

    /**
     * Applies custom styling to slider components
     */
    private void styleSliderComponents(Slider slider) {
        try {
            if (slider.lookup(".track") != null) {
                slider.lookup(".track").setStyle(
                    "-fx-background-color: " + BORDER_COLOR + ";" +
                    "-fx-background-radius: 2;" +
                    "-fx-pref-height: 4px;"
                );
            }
            
            if (slider.lookup(".thumb") != null) {
                slider.lookup(".thumb").setStyle(
                    "-fx-background-color: " + ACCENT_BLUE + ";" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: " + TEXT_PRIMARY + ";" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-pref-width: 20px;" +
                    "-fx-pref-height: 20px;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 2);"
                );
            }
        } catch (Exception e) {
            // Ignore styling errors - use defaults
        }
    }

    // ========== EXPORT FUNCTIONALITY ==========
    
    /**
     * Exports the current chart as a PNG image
     */
    private void exportChartAsPNG() {
        String filename = "climate_chart_" + String.format("%03d", exportCounter) + ".png";
        File file = new File(System.getProperty("user.home") + File.separator + "Downloads", filename);
        
        file.getParentFile().mkdirs();
        
        try {
            javafx.scene.image.WritableImage image = lineChart.snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            
            exportCounter++;
            hideLoading("Chart exported as: " + filename, SUCCESS_GREEN);
            
        } catch (Exception ex) {
            hideLoading("Export failed: " + ex.getMessage(), ACCENT_ORANGE);
        }
    }



    public static void main(String[] args) {
        launch(args);
    }
}