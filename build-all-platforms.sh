#!/bin/bash
# Complete Standalone Application Builder for Climate Data Visualiser
# This script creates truly standalone executables with bundled Java runtime

echo "========================================"
echo "Climate Data Visualiser - Standalone Builder"
echo "Building for: Windows, macOS, Linux"
echo "========================================"

# Check for required tools
if ! command -v javac &> /dev/null; then
    echo "❌ Error: javac not found. Please install JDK 17+"
    exit 1
fi

if ! command -v jlink &> /dev/null; then
    echo "❌ Error: jlink not found. Please install JDK 17+"
    exit 1
fi

# Configuration
JAVAFX_PATH="$HOME/Downloads/javafx-sdk-24.0.2"
JAVAFX_JMODS="$HOME/Downloads/javafx-jmods-24.0.2"
MAIN_CLASS="ui.MainApp"
APP_NAME="Climate-Data-Visualiser"
APP_VERSION="1.0.0"

# Verify JavaFX exists
if [ ! -d "$JAVAFX_PATH" ]; then
    echo "❌ Error: JavaFX SDK not found at $JAVAFX_PATH"
    echo "Please download from: https://openjfx.io/"
    exit 1
fi

# Clean previous builds
echo "🧹 Cleaning previous builds..."
rm -rf build
rm -rf out

# Create directory structure
mkdir -p build/{classes,jars,runtime}
mkdir -p out/{windows,macos,linux}

# ============================================
# STEP 1: COMPILE THE APPLICATION
# ============================================
echo ""
echo "📦 Step 1: Compiling Java application..."

# Find the DataLoader file (handle case sensitivity)
DATA_LOADER=""
if [ -f "data/DataLoader.java" ]; then
    DATA_LOADER="data/DataLoader.java"
elif [ -f "data/dataloader.java" ]; then
    DATA_LOADER="data/dataloader.java"
else
    echo "❌ Error: DataLoader.java not found"
    exit 1
fi

# Compile all Java files
javac -d build/classes \
    --module-path "$JAVAFX_PATH/lib" \
    --add-modules javafx.controls,javafx.fxml,javafx.swing \
    ui/MainApp.java \
    "$DATA_LOADER" \
    data/DataRecord.java \
    data/DataAnalyser.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed"
    exit 1
fi
echo "✅ Compilation successful"

# ============================================
# STEP 2: CREATE JAR FILE
# ============================================
echo ""
echo "📦 Step 2: Creating JAR file..."

cd build/classes
jar cfe ../jars/$APP_NAME.jar $MAIN_CLASS \
    ui/*.class \
    data/*.class
cd ../..

if [ ! -f "build/jars/$APP_NAME.jar" ]; then
    echo "❌ JAR creation failed"
    exit 1
fi
echo "✅ JAR created successfully"

# ============================================
# STEP 3: CREATE MODULE INFO (for jlink)
# ============================================
echo ""
echo "📦 Step 3: Creating module configuration..."

cat > module-info.java << EOF
module climatevisualiser {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;
    requires java.logging;
    
    exports ui;
    exports data;
}
EOF

# Compile module-info
javac -d build/classes \
    --module-path "$JAVAFX_PATH/lib" \
    module-info.java

# Recreate JAR as a module
cd build/classes
jar cfe ../jars/$APP_NAME.jar $MAIN_CLASS \
    module-info.class \
    ui/*.class \
    data/*.class
cd ../..

# ============================================
# STEP 4: BUILD WINDOWS VERSION
# ============================================
echo ""
echo "🪟 Step 4: Building Windows version..."

# Create custom runtime for Windows
if [ -d "$JAVAFX_JMODS" ]; then
    echo "Creating Windows runtime with jlink..."
    jlink \
        --module-path "$JAVAFX_JMODS:$JAVA_HOME/jmods" \
        --add-modules javafx.controls,javafx.fxml,javafx.swing,java.desktop,java.logging \
        --output build/runtime/windows \
        --strip-debug \
        --compress=2 \
        --no-header-files \
        --no-man-pages
else
    echo "⚠️  JavaFX jmods not found, using system JRE"
    # Copy system JRE as fallback
    cp -r "$JAVA_HOME" build/runtime/windows
fi

# Create Windows distribution
mkdir -p "out/windows/$APP_NAME"
cp build/jars/$APP_NAME.jar "out/windows/$APP_NAME/"
cp -r build/runtime/windows "out/windows/$APP_NAME/runtime"

# Create Windows batch launcher with better error handling
cat > "out/windows/$APP_NAME/$APP_NAME.bat" << 'EOF'
@echo off
setlocal

:: Set the app home directory
set APP_HOME=%~dp0
cd /d "%APP_HOME%"

:: Check if runtime exists
if not exist "runtime\bin\java.exe" (
    echo Error: Java runtime not found!
    echo Please ensure the application was extracted correctly.
    echo.
    echo Expected location: %APP_HOME%runtime\bin\java.exe
    echo.
    
    :: Try to use system Java as fallback
    where java >nul 2>&1
    if %errorlevel% == 0 (
        echo Attempting to use system Java...
        java -version
        echo.
        set JAVA_CMD=java
    ) else (
        echo No Java installation found on your system.
        echo Please install Java 17 or higher from: https://adoptium.net/
        pause
        exit /b 1
    )
) else (
    set JAVA_CMD="%APP_HOME%runtime\bin\java.exe"
)

:: Run the application
echo Starting Climate Data Visualiser...
%JAVA_CMD% ^
    --module-path "%APP_HOME%runtime\lib" ^
    --add-modules javafx.controls,javafx.fxml,javafx.swing ^
    -jar "%APP_HOME%Climate-Data-Visualiser.jar"

if %errorlevel% neq 0 (
    echo.
    echo Application failed to start. Error code: %errorlevel%
    echo.
    echo Troubleshooting:
    echo 1. Make sure all files were extracted properly
    echo 2. Try running as administrator
    echo 3. Check that antivirus is not blocking the application
    pause
)
EOF

# Create Windows README
cat > "out/windows/$APP_NAME/README.txt" << EOF
Climate Data Visualiser - Windows Version
=========================================

TO RUN THE APPLICATION:
1. Double-click $APP_NAME.bat
2. If Windows Defender SmartScreen appears, click "More info" then "Run anyway"

REQUIREMENTS:
- Windows 10 or later (64-bit)
- No Java installation required (bundled)

TROUBLESHOOTING:
- If the app doesn't start, try right-clicking $APP_NAME.bat and selecting "Run as administrator"
- Make sure all files were extracted from the ZIP
- Ensure the 'runtime' folder is present

DATA FORMAT:
Your CSV files should have these columns:
Country,Year,Emission

SUPPORT:
If you encounter issues, check the console output or contact support.
EOF

echo "✅ Windows version created"

# ============================================
# STEP 5: BUILD MACOS VERSION
# ============================================
echo ""
echo "🍎 Step 5: Building macOS version..."

# Create macOS runtime
if [ -d "$JAVAFX_JMODS" ]; then
    jlink \
        --module-path "$JAVAFX_JMODS:$JAVA_HOME/jmods" \
        --add-modules javafx.controls,javafx.fxml,javafx.swing,java.desktop,java.logging \
        --output build/runtime/macos \
        --strip-debug \
        --compress=2 \
        --no-header-files \
        --no-man-pages
else
    cp -r "$JAVA_HOME" build/runtime/macos
fi

# Create macOS app bundle
APP_BUNDLE="out/macos/$APP_NAME.app"
mkdir -p "$APP_BUNDLE/Contents/MacOS"
mkdir -p "$APP_BUNDLE/Contents/Resources"
mkdir -p "$APP_BUNDLE/Contents/Java"

# Copy runtime and JAR
cp -r build/runtime/macos "$APP_BUNDLE/Contents/runtime"
cp build/jars/$APP_NAME.jar "$APP_BUNDLE/Contents/Java/"

# Create launcher script for macOS
cat > "$APP_BUNDLE/Contents/MacOS/$APP_NAME" << 'EOF'
#!/bin/bash

# Get the directory of this script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

# Set up paths
APP_HOME="$DIR/.."
JAVA_HOME="$APP_HOME/Contents/runtime"

# Check if runtime exists
if [ ! -f "$JAVA_HOME/bin/java" ]; then
    osascript -e 'display alert "Java Runtime Missing" message "The application runtime was not found. Please re-download the application." as critical'
    
    # Try system Java as fallback
    if command -v java &> /dev/null; then
        JAVA_CMD="java"
    else
        osascript -e 'display alert "Java Not Found" message "Please install Java 17+ from https://adoptium.net/" as critical'
        exit 1
    fi
else
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

# Run the application
exec "$JAVA_CMD" \
    --module-path "$JAVA_HOME/lib" \
    --add-modules javafx.controls,javafx.fxml,javafx.swing \
    -Xdock:name="Climate Data Visualiser" \
    -jar "$APP_HOME/Contents/Java/Climate-Data-Visualiser.jar"
EOF

chmod +x "$APP_BUNDLE/Contents/MacOS/$APP_NAME"

# Create Info.plist
cat > "$APP_BUNDLE/Contents/Info.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleExecutable</key>
    <string>$APP_NAME</string>
    <key>CFBundleIdentifier</key>
    <string>com.climate.visualiser</string>
    <key>CFBundleName</key>
    <string>Climate Data Visualiser</string>
    <key>CFBundleDisplayName</key>
    <string>Climate Data Visualiser</string>
    <key>CFBundleVersion</key>
    <string>$APP_VERSION</string>
    <key>CFBundleShortVersionString</key>
    <string>$APP_VERSION</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>NSHighResolutionCapable</key>
    <true/>
    <key>LSMinimumSystemVersion</key>
    <string>10.14</string>
    <key>NSRequiresAquaSystemAppearance</key>
    <false/>
</dict>
</plist>
EOF

echo "✅ macOS version created"

# ============================================
# STEP 6: BUILD LINUX VERSION
# ============================================
echo ""
echo "🐧 Step 6: Building Linux version..."

# Create Linux runtime
if [ -d "$JAVAFX_JMODS" ]; then
    jlink \
        --module-path "$JAVAFX_JMODS:$JAVA_HOME/jmods" \
        --add-modules javafx.controls,javafx.fxml,javafx.swing,java.desktop,java.logging \
        --output build/runtime/linux \
        --strip-debug \
        --compress=2 \
        --no-header-files \
        --no-man-pages
else
    cp -r "$JAVA_HOME" build/runtime/linux
fi

# Create Linux distribution
mkdir -p "out/linux/$APP_NAME"
cp build/jars/$APP_NAME.jar "out/linux/$APP_NAME/"
cp -r build/runtime/linux "out/linux/$APP_NAME/runtime"

# Create Linux launcher script
cat > "out/linux/$APP_NAME/$APP_NAME.sh" << 'EOF'
#!/bin/bash

# Get the directory of this script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Check if runtime exists
if [ ! -f "runtime/bin/java" ]; then
    echo "Error: Java runtime not found!"
    echo "Expected location: $SCRIPT_DIR/runtime/bin/java"
    echo ""
    
    # Try system Java as fallback
    if command -v java &> /dev/null; then
        echo "Using system Java..."
        java -version
        JAVA_CMD="java"
    else
        echo "No Java found. Please install Java 17+:"
        echo "  Ubuntu/Debian: sudo apt install openjdk-17-jdk"
        echo "  Fedora: sudo dnf install java-17-openjdk"
        echo "  Arch: sudo pacman -S jdk17-openjdk"
        exit 1
    fi
else
    JAVA_CMD="$SCRIPT_DIR/runtime/bin/java"
fi

# Run the application
echo "Starting Climate Data Visualiser..."
"$JAVA_CMD" \
    --module-path "$SCRIPT_DIR/runtime/lib" \
    --add-modules javafx.controls,javafx.fxml,javafx.swing \
    -jar "$SCRIPT_DIR/Climate-Data-Visualiser.jar"
EOF

chmod +x "out/linux/$APP_NAME/$APP_NAME.sh"

# Create desktop entry
cat > "out/linux/$APP_NAME/$APP_NAME.desktop" << EOF
[Desktop Entry]
Type=Application
Name=Climate Data Visualiser
Comment=Analyse and visualise CO2 emissions data
Exec=$APP_NAME.sh
Terminal=false
Categories=Education;Science;
EOF

echo "✅ Linux version created"

# ============================================
# STEP 7: CREATE DISTRIBUTION PACKAGES
# ============================================
echo ""
echo "📦 Step 7: Creating distribution packages..."

# Windows ZIP
cd out/windows
zip -r "../$APP_NAME-Windows-x64.zip" "$APP_NAME/" -q
cd ../..
echo "✅ Created: out/$APP_NAME-Windows-x64.zip"

# macOS ZIP
cd out/macos
zip -r "../$APP_NAME-macOS.zip" "$APP_NAME.app/" -q
cd ../..
echo "✅ Created: out/$APP_NAME-macOS.zip"

# Linux TAR.GZ
cd out/linux
tar -czf "../$APP_NAME-Linux-x64.tar.gz" "$APP_NAME/"
cd ../..
echo "✅ Created: out/$APP_NAME-Linux-x64.tar.gz"

# ============================================
# STEP 8: CREATE GITHUB README
# ============================================
cat > out/README.md << EOF
# Climate Data Visualiser - Downloads

## 🪟 Windows (64-bit)
**File:** \`$APP_NAME-Windows-x64.zip\` (~65MB)

1. Download and extract the ZIP file
2. Open the extracted folder
3. Double-click \`$APP_NAME.bat\`
4. If Windows SmartScreen appears, click "More info" → "Run anyway"

## 🍎 macOS (10.14+)
**File:** \`$APP_NAME-macOS.zip\` (~65MB)

1. Download and extract the ZIP file
2. Drag \`$APP_NAME.app\` to your Applications folder
3. First time: Right-click → Open → Open (to bypass Gatekeeper)
4. Subsequently: Double-click to run

## 🐧 Linux (64-bit)
**File:** \`$APP_NAME-Linux-x64.tar.gz\` (~65MB)

\`\`\`bash
tar -xzf $APP_NAME-Linux-x64.tar.gz
cd $APP_NAME
chmod +x $APP_NAME.sh
./$APP_NAME.sh
\`\`\`

## ✅ System Requirements
- **Windows:** Windows 10 or later (64-bit)
- **macOS:** macOS 10.14 (Mojave) or later
- **Linux:** Ubuntu 18.04+ or equivalent (64-bit)
- **RAM:** 2GB minimum, 4GB recommended
- **Disk:** 200MB free space

## 📊 CSV Data Format
Your CSV files should follow this format:
\`\`\`csv
Country,Year,Emission
Australia,1990,15.451
Australia,1991,15.234
...
\`\`\`

## ❌ Troubleshooting

### Windows
- Run as administrator if permission errors occur
- Ensure all files are extracted (especially the \`runtime\` folder)
- Check Windows Defender hasn't quarantined files

### macOS
- Use "Right-click → Open" for first launch
- Check Security & Privacy settings if blocked
- Ensure the app is in Applications folder

### Linux
- Make sure the script is executable: \`chmod +x $APP_NAME.sh\`
- Install GTK3 if UI issues: \`sudo apt install libgtk-3-0\`

## 📝 Version
Version: $APP_VERSION
Build Date: $(date +%Y-%m-%d)

## 🚀 No Java Required!
These packages include a bundled Java runtime - no separate Java installation needed.
EOF

# ============================================
# FINAL SUMMARY
# ============================================
echo ""
echo "========================================"
echo "✅ BUILD COMPLETE!"
echo "========================================"
echo ""
echo "📁 Distribution packages created in 'out/' directory:"
ls -lh out/*.zip out/*.tar.gz 2>/dev/null | awk '{print "   " $5 "  " $9}'
echo ""
echo "📊 Package contents:"
echo "   • Bundled Java runtime (no installation required)"
echo "   • JavaFX libraries included"
echo "   • Platform-specific launchers"
echo "   • Error handling and fallbacks"
echo ""
echo "📤 To distribute:"
echo "   1. Upload the files from 'out/' to GitHub Releases"
echo "   2. Share the README.md for installation instructions"
echo ""
echo "⚠️  Important notes:"
echo "   • Packages are ~65MB due to bundled Java runtime"
echo "   • Users do NOT need Java installed"
echo "   • First launch on macOS may require right-click → Open"
echo "   • Windows may show SmartScreen warning (normal for unsigned apps)"
echo ""
echo "🎉 Your application is ready for distribution!"