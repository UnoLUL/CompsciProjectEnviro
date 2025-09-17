#!/bin/bash
cd "$(dirname "$0")"
java --module-path ~/Downloads/javafx-sdk-24.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.swing -jar build/dist/climate-visualizer.jar
