#!/bin/bash
# Cross-Platform Executable Builder for Climate Data Visualizer
# Option 1: Minimal distribution without sample data files

echo "Building Climate Data Visualizer for all platforms (no sample data)..."

# Create build directory structure
mkdir -p build/{classes,dist,executables/{windows,macos,linux}}

# Step 1: Compile and create JAR (common for all platforms)
echo "1. Building JAR file..."
javac --module-path ~/Downloads/javafx-sdk-24.0.2/lib \
      --add-modules javafx.controls,javafx.fxml,javafx.swing \
      -d build/classes \
      ui/MainApp.java data/DataLoader.java data/DataRecord.java data/DataAnalyser.java

cd build/classes
jar cfe ../dist/climate-visualizer.jar ui.MainApp ui/*.class data/*.class
cd ../..

echo "✅ JAR created: build/dist/climate-visualizer.jar"

# ===========================================
# WINDOWS EXECUTABLE (.exe)
# ===========================================
echo ""
echo "2. Building Windows executable..."

# Create Windows distribution folder
mkdir -p build/executables/windows/Climate-Data-Visualizer
cp build/dist/climate-visualizer.jar build/executables/windows/Climate-Data-Visualizer/

# Copy JavaFX for Windows
cp -r ~/Downloads/javafx-sdk-24.0.2/lib build/executables/windows/Climate-Data-Visualizer/javafx

# Create Windows batch launcher
cat > build/executables/windows/Climate-Data-Visualizer/Climate-Data-Visualizer.bat << 'EOF'
@echo off
cd /d "%~dp0"
java --module-path javafx --add-modules javafx.controls,javafx.fxml,javafx.swing -jar climate-visualizer.jar
pause
EOF

# Create simple README for Windows users
cat > build/executables/windows/Climate-Data-Visualizer/README.txt << 'EOF'
Climate Data Visualizer - Windows Version

1. Double-click Climate-Data-Visualizer.bat to run
2. Load your own CSV file with columns: Country, Year, Emission
3. No sample data included to keep download size small

Requirements: None (Java included)
File format: CSV with headers Country,Year,Emission
EOF

echo "✅ Windows version created (no sample data included)"

# ===========================================
# MACOS APP BUNDLE (.app)
# ===========================================
echo ""
echo "3. Building macOS app bundle..."

# Create macOS app structure
mkdir -p "build/executables/macos/Climate Data Visualizer.app/Contents/MacOS"
mkdir -p "build/executables/macos/Climate Data Visualizer.app/Contents/Resources/javafx"

# Copy JavaFX and JAR (no sample data)
cp -r ~/Downloads/javafx-sdk-24.0.2/lib/* "build/executables/macos/Climate Data Visualizer.app/Contents/Resources/javafx/"
cp build/dist/climate-visualizer.jar "build/executables/macos/Climate Data Visualizer.app/Contents/Resources/"

# Create launcher script
cat > "build/executables/macos/Climate Data Visualizer.app/Contents/MacOS/launch.sh" << 'EOF'
#!/bin/bash
cd "$(dirname "$0")/../Resources"
java --module-path javafx --add-modules javafx.controls,javafx.fxml,javafx.swing -jar climate-visualizer.jar
EOF

chmod +x "build/executables/macos/Climate Data Visualizer.app/Contents/MacOS/launch.sh"

# Create Info.plist
cat > "build/executables/macos/Climate Data Visualizer.app/Contents/Info.plist" << 'EOF'
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
    <key>CFBundleShortVersionString</key>
    <string>1.0</string>
</dict>
</plist>
EOF

echo "✅ macOS version created (no sample data included)"

# ===========================================
# LINUX DISTRIBUTION
# ===========================================
echo ""
echo "4. Building Linux distribution..."

# Create Linux distribution folder
mkdir -p build/executables/linux/climate-data-visualizer
cp build/dist/climate-visualizer.jar build/executables/linux/climate-data-visualizer/
cp -r ~/Downloads/javafx-sdk-24.0.2/lib build/executables/linux/climate-data-visualizer/javafx

# Create Linux launcher script
cat > build/executables/linux/climate-data-visualizer/climate-data-visualizer.sh << 'EOF'
#!/bin/bash
cd "$(dirname "$0")"
java --module-path javafx --add-modules javafx.controls,javafx.fxml,javafx.swing -jar climate-visualizer.jar
EOF

chmod +x build/executables/linux/climate-data-visualizer/climate-data-visualizer.sh

# Create README for Linux
cat > build/executables/linux/climate-data-visualizer/README.txt << 'EOF'
Climate Data Visualizer - Linux Version

1. Run: ./climate-data-visualizer.sh
2. Load your own CSV file with columns: Country, Year, Emission
3. No sample data included to keep download size small

Requirements: None (Java included)
File format: CSV with headers Country,Year,Emission
EOF

echo "✅ Linux version created (no sample data included)"

# ===========================================
# CREATE DISTRIBUTION PACKAGES
# ===========================================
echo ""
echo "5. Creating distribution packages..."

# Create Windows ZIP
cd build/executables/windows
zip -r ../Climate-Data-Visualizer-Windows.zip Climate-Data-Visualizer/
cd ../../..

# Create macOS ZIP
cd build/executables/macos
zip -r ../Climate-Data-Visualizer-macOS.zip "Climate Data Visualizer.app"
cd ../../..

# Create Linux TAR.GZ
cd build/executables/linux
tar -czf ../Climate-Data-Visualizer-Linux.tar.gz climate-data-visualizer/
cd ../../..

# Check file sizes
echo ""
echo "📊 File sizes:"
ls -lh build/executables/*.zip build/executables/*.tar.gz 2>/dev/null | awk '{print $5 " - " $9}'

echo ""
echo "🎉 BUILD COMPLETE - MINIMAL DISTRIBUTION!"
echo "=================================="
echo ""
echo "Distribution files created (no sample data):"
echo "📁 Windows: build/executables/Climate-Data-Visualizer-Windows.zip"
echo "📁 macOS:   build/executables/Climate-Data-Visualizer-macOS.zip"
echo "📁 Linux:   build/executables/Climate-Data-Visualizer-Linux.tar.gz"
echo ""
echo "Benefits of this approach:"
echo "• Smaller download sizes"
echo "• Users provide their own CSV data"
echo "• Faster uploads to GitHub"
echo ""
echo "User instructions:"
echo "• Download and extract for your platform"
echo "• Run the application"
echo "• Click 'Load Data' and select your CSV file"
echo "• CSV format: Country,Year,Emission"
echo ""

# Create README for GitHub
cat > build/executables/README.md << 'EOF'
# Climate Data Visualizer - Downloads

Choose your platform below. No sample data is included to keep downloads small.

## Windows
1. Download `Climate-Data-Visualizer-Windows.zip`
2. Extract the ZIP file
3. Double-click `Climate-Data-Visualizer.bat`

## macOS
1. Download `Climate-Data-Visualizer-macOS.zip`
2. Extract the ZIP file
3. Drag `Climate Data Visualizer.app` to Applications folder
4. Double-click to run (right-click → Open if security warning)

## Linux
1. Download `Climate-Data-Visualizer-Linux.tar.gz`
2. Extract: `tar -xzf Climate-Data-Visualizer-Linux.tar.gz`
3. Run: `./climate-data-visualizer/climate-data-visualizer.sh`

## Data Format
Prepare your CSV file with these columns:
```
Country,Year,Emission
United States,1990,19.267
China,1990,2.162
...
```

## Requirements
- No additional software needed (Java included)
- Works on Windows 10+, macOS 10.14+, Ubuntu 18.04+
- CSV file with climate data (not included)

## File Size
Each download is approximately 30-50MB (without sample data).
EOF

echo "📝 README.md created for GitHub"
echo "💡 Users will need to provide their own CSV data files"