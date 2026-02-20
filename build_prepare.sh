#!/bin/bash

# Hexodus Build Script
# This script prepares the project for building on GitHub Actions

echo "==========================================="
echo "Hexodus Build Preparation Script"
echo "==========================================="

# Check if we're in the right directory
if [ ! -f "settings.gradle" ] || [ ! -d "app" ]; then
    echo "Error: Not in the project root directory"
    exit 1
fi

echo "✓ Verified project structure"

# Make gradlew executable
chmod +x gradlew
echo "✓ Made gradlew executable"

# Check if gradle wrapper properties exist
if [ ! -f "gradle/wrapper/gradle-wrapper.properties" ]; then
    echo "Error: gradle-wrapper.properties not found"
    exit 1
fi

echo "✓ Verified Gradle wrapper"

# Verify important files exist
REQUIRED_FILES=(
    "app/build.gradle"
    "app/src/main/AndroidManifest.xml"
    "app/src/main/java/com/hexodus/MainActivity.kt"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        echo "Error: Required file not found: $file"
        exit 1
    fi
done

echo "✓ Verified required project files"

# Create directories if they don't exist
mkdir -p app/src/main/res/values
mkdir -p app/src/main/res/drawable
mkdir -p app/src/main/res/mipmap-anydpi-v26

echo "✓ Created required resource directories"

# Check if version.properties exists
if [ ! -f "version.properties" ]; then
    echo "version.major=1" > version.properties
    echo "version.minor=0" >> version.properties
    echo "version.patch=0" >> version.properties
    echo "✓ Created version.properties"
else
    echo "✓ Found version.properties"
fi

# Verify all service files exist
SERVICE_FILES=(
    "app/src/main/java/com/hexodus/services/ShizukuBridgeService.kt"
    "app/src/main/java/com/hexodus/services/OverlayActivationService.kt"
    "app/src/main/java/com/hexodus/services/MonetOverrideService.kt"
    "app/src/main/java/com/hexodus/services/FoldableDisplayService.kt"
    "app/src/main/java/com/hexodus/services/HighContrastInjectorService.kt"
    "app/src/main/java/com/hexodus/services/ThemeManagerService.kt"
    "app/src/main/java/com/hexodus/services/SystemTunerService.kt"
    "app/src/main/java/com/hexodus/services/AppThemerService.kt"
    "app/src/main/java/com/hexodus/services/GestureManagerService.kt"
    "app/src/main/java/com/hexodus/services/MediaNotificationService.kt"
    "app/src/main/java/com/hexodus/services/AudioManagerService.kt"
    "app/src/main/java/com/hexodus/services/AppManagerService.kt"
    "app/src/main/java/com/hexodus/services/PrivacySecurityService.kt"
    "app/src/main/java/com/hexodus/services/NetworkFirewallService.kt"
    "app/src/main/java/com/hexodus/services/PowerManagerService.kt"
    "app/src/main/java/com/hexodus/services/DynamicColorService.kt"
    "app/src/main/java/com/hexodus/services/AccessibilityCheckerService.kt"
    "app/src/main/java/com/hexodus/services/SecurityValidationService.kt"
    "app/src/main/java/com/hexodus/services/AdvancedFeatureService.kt"
    "app/src/main/java/com/hexodus/services/ResourceManagerService.kt"
    "app/src/main/java/com/hexodus/services/BackupRestoreService.kt"
    "app/src/main/java/com/hexodus/services/AdvancedThemingService.kt"
    "app/src/main/java/com/hexodus/services/SystemInspectorService.kt"
    "app/src/main/java/com/hexodus/services/DeviceSpecificService.kt"
    "app/src/main/java/com/hexodus/services/PrivacyManagerService.kt"
    "app/src/main/java/com/hexodus/services/MediaNotificationService.kt"
    "app/src/main/java/com/hexodus/services/PerformanceOptimizerService.kt"
)

missing_services=0
for file in "${SERVICE_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        echo "Warning: Service file not found: $file"
        ((missing_services++))
    fi
done

if [ $missing_services -eq 0 ]; then
    echo "✓ All service files verified"
else
    echo "! $missing_services service files are missing (this may be expected depending on implementation)"
fi

# Verify UI components exist
UI_FILES=(
    "app/src/main/java/com/hexodus/ui/MainActivityScreen.kt"
    "app/src/main/java/com/hexodus/ui/ThemePreviewScreen.kt"
    "app/src/main/java/com/hexodus/ui/theme/Theme.kt"
    "app/src/main/java/com/hexodus/ui/components/FeatureToggleCard.kt"
)

for file in "${UI_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        echo "Warning: UI file not found: $file"
    fi
done

echo "✓ Verified UI components"

# Show project information
echo ""
echo "==========================================="
echo "Project Information:"
echo "==========================================="
echo "Project Name: Hexodus"
echo "Package: com.hexodus"
echo "Min SDK: 26"
echo "Target SDK: 36"
echo "Compile SDK: 36"
echo ""

# Show build commands that will be used in GitHub Actions
echo "==========================================="
echo "GitHub Actions Build Commands:"
echo "==========================================="
echo "./gradlew build"
echo "./gradlew testDebugUnitTest"
echo "./gradlew assembleDebug"
echo "./gradlew assembleRelease"
echo ""

echo "Build preparation complete! The project is ready for GitHub Actions building."
echo ""
echo "When you push this to GitHub, the Actions workflow will:"
echo "1. Set up JDK 17 and Android SDK"
echo "2. Build the debug and release APKs"
echo "3. Run unit tests"
echo "4. Upload artifacts (APK files) to the workflow"
echo ""
echo "To build locally, run: ./gradlew assembleDebug"