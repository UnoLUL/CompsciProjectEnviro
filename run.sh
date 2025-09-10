#!/bin/bash
# filepath: /Users/charliedavey/Coding/CompsciProjectEnviro/run.sh

# Compile all Java files in 'ui' and 'data' folders
javac --module-path ~/Downloads/javafx-sdk-24.0.2/lib --add-modules javafx.controls,javafx.fxml ui/*.java data/*.java

# Run the JavaFX application
java --module-path ~/Downloads/javafx-sdk-24.0.2/lib --add-modules javafx.controls,javafx.fxml ui.MainApp