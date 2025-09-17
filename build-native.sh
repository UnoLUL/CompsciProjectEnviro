#!/bin/bash
# Troubleshoot Native Executable Issues

echo "🔍 Troubleshooting Climate Data Visualizer Native Executable"
echo "============================================================"

# Check what was created
echo "1️⃣  Checking build outputs..."
if [ -d "build/native" ]; then
    echo "✅ Build directory exists"
    ls -la build/native/
    echo ""
else
    echo "❌ Build directory not found!"
    echo "💡 Run ./build-native.sh first"
    exit 1
fi

# Check Java version
echo "2️⃣  Checking Java version..."
java -version
echo ""

# Check if jpackage worked
echo "3️⃣  Checking for common issues..."

# macOS specific issues
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "🍎 macOS Detected - Checking for common macOS issues..."
    
    # Check for Gatekeeper issues
    echo "⚠️  Common macOS Issue: Gatekeeper blocking unsigned apps"
    echo "   If you see 'cannot be opened because it is from an unidentified developer'"
    echo "   Solution: Right-click app → 'Open' → 'Open' (bypass Gatekeeper)"
    echo ""
    
    # Check for quarantine
    if [ -f "build/native/Climate Data Visualizer.dmg" ]; then
        echo "🔓 Removing quarantine flag from DMG..."
        xattr -rd com.apple.quarantine "build/native/Climate Data Visualizer.dmg"
    fi
    
    # Mount DMG and check contents
    if [ -f "build/native/Climate Data Visualizer.dmg" ]; then
        echo "📦 Checking DMG contents..."
        hdiutil attach "build/native/Climate Data Visualizer.dmg" -quiet
        ls -la "/Volumes/Climate Data Visualizer/"
        hdiutil detach "/Volumes/Climate Data Visualizer" -quiet
    fi
fi

echo "4️⃣  Creating simple test to verify JAR works..."
echo "Testing JAR directly..."
java --module-path ~/Downloads/javafx-sdk-24.0.2/lib \
     --add-modules javafx.controls,javafx.fxml,javafx.swing \
     -jar build/dist/climate-visualizer.jar &
TEST_PID=$!
sleep 2

if ps -p $TEST_PID > /dev/null; then
    echo "✅ JAR works fine - issue is with native packaging"
    kill $TEST_PID
else
    echo "❌ JAR itself has issues"
    echo "Check console output above for errors"
fi

echo ""
echo "🔧 ALTERNATIVE SOLUTION - Create App Bundle (Simpler)"
echo "===================================================="
echo "This creates a simpler executable that's more reliable:"

# Create alternative app bundle script
cat > create-app-bundle.sh << 'EOF'
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
EOF

chmod +x create-app-bundle.sh

echo ""
echo "🎯 QUICK FIXES TO TRY:"
echo "====================="
echo ""
echo "Option 1 - Fix Gatekeeper (macOS):"
echo "   Right-click the app → Open → Open (bypass security)"
echo ""
echo "Option 2 - Use simple app bundle:"
echo "   ./create-app-bundle.sh"
echo "   Double-click 'Climate Data Visualizer.app'"
echo ""
echo "Option 3 - Run JAR directly (always works):"
echo "   java --module-path ~/Downloads/javafx-sdk-24.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.swing -jar build/dist/climate-visualizer.jar"
echo ""
echo "Option 4 - Create simple launcher script:"

cat > run-app.sh << 'EOF'
#!/bin/bash
cd "$(dirname "$0")"
java --module-path ~/Downloads/javafx-sdk-24.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.swing -jar build/dist/climate-visualizer.jar
EOF

chmod +x run-app.sh
echo "   ✅ Created run-app.sh - just double-click this!"
echo ""

# Final diagnosis
echo "🩺 DIAGNOSTIC COMPLETE"
echo "======================"
echo "Most likely issues:"
echo "1. macOS Gatekeeper blocking unsigned app"
echo "2. JavaFX paths not properly bundled"  
echo "3. Missing native libraries"
echo ""
echo "💡 Try the solutions above in order!"
echo "   The JAR + script method (Option 4) should always work."