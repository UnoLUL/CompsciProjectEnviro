#!/bin/bash
# filepath: /Users/charliedavey/Coding/CompsciProjectEnviro/run.sh

echo "Starting Climate Data Visualizer compilation..."

# Clean any existing class files
echo "Cleaning previous builds..."
find . -name "*.class" -delete

# Compile only the files we need (excluding test files)
echo "Compiling Java files..."
javac --module-path ~/Downloads/javafx-sdk-24.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.swing ui/MainApp.java data/DataLoader.java data/DataRecord.java data/DataAnalyser.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful! Running application..."
    # Run the JavaFX application
    java --module-path ~/Downloads/javafx-sdk-24.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.swing ui.MainApp
else
    echo "Compilation failed! Please check for errors above."
    exit 1
fi