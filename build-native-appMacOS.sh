#!/bin/bash
echo "Creating simple app bundle..."

# Create app structure
mkdir -p "Climate Data Visualizer.app/Contents/MacOS"
mkdir -p "Climate Data Visualizer.app/Contents/Resources"
mkdir -p "Climate Data Visualizer.app/Contents/Resources/javafx"

# Copy JavaFX
cp -r ~/Downloads/javafx-sdk-24.0.2/lib/* "Climate Data Visualizer.app/Contents/Resources/javafx/"

# Copy JAR
cp build/dist/climate-visualizer.jar "Climate Data Visualizer.app/Contents/Resources/"

# Create launcher script
cat > "Climate Data Visualizer.app/Contents/MacOS/launch.sh" << 'LAUNCHER'
#!/bin/bash
cd "$(dirname "$0")/../Resources"
java --module-path javafx --add-modules javafx.controls,javafx.fxml,javafx.swing -jar climate-visualizer.jar
LAUNCHER

chmod +x "Climate Data Visualizer.app/Contents/MacOS/launch.sh"

# Create Info.plist
cat > "Climate Data Visualizer.app/Contents/Info.plist" << 'PLIST'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleExecutable</key>
    <string>launch.sh</string>
    <key>CFBundleIdentifier</key>
    <string>com.climate.visualizer</string>
    <key>CFBundleName</key>
    <string>Climate Data Visualizer</string>
    <key>CFBundleVersion</key>
    <string>1.0</string>
</dict>
</plist>
PLIST

echo "✅ App bundle created: Climate Data Visualizer.app"
echo "🚀 Double-click to run!"
