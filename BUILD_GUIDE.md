# Hexodus Build Guide

This document provides instructions for building Hexodus locally and explains the GitHub Actions build process.

## Prerequisites

To build Hexodus, you'll need:

1. **Java Development Kit (JDK) 17** or higher
2. **Android SDK** with the following components:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0 or later
   - Android SDK Command-Line Tools
   - Android SDK Platform-Tools
3. **Android NDK** (optional, for native code)
4. **Git** for version control

## Setting Up the Development Environment

### Option 1: Using Android Studio (Recommended)

1. Install [Android Studio](https://developer.android.com/studio)
2. Clone the repository:
   ```bash
   git clone https://github.com/your-username/hexodus.git
   cd hexodus
   ```
3. Open the project in Android Studio
4. Sync the Gradle files (Tools → Sync Project with Gradle Files)

### Option 2: Command Line Setup

1. Install JDK 17 or higher
2. Download and set up Android SDK:
   ```bash
   # Install command line tools
   # Download sdk-tools from Android developer site
   
   # Set environment variables
   export ANDROID_HOME=/path/to/android/sdk
   export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
   export PATH=$PATH:$ANDROID_HOME/platform-tools
   ```
3. Install required SDK components:
   ```bash
   sdkmanager "platforms;android-34" "build-tools;34.0.0" "cmdline-tools;latest"
   ```

## Building the Project

### Using Gradle Wrapper (Recommended)

The project includes a Gradle wrapper that ensures consistent builds across different environments:

```bash
# Make sure the gradlew script is executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build all variants
./gradlew build

# Run unit tests
./gradlew testDebugUnitTest

# Run connected device tests
./gradlew connectedDebugAndroidTest
```

### Build Outputs

The built APKs will be located in:
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

## GitHub Actions Build Process

Hexodus includes comprehensive GitHub Actions workflows for automated building and testing:

### Build Workflow (`.github/workflows/build.yml`)

This workflow runs on every push and pull request:

- Sets up JDK 17 and Android SDK
- Builds debug and release APKs
- Runs unit tests
- Runs instrumentation tests on multiple API levels
- Uploads APK artifacts

### Release Workflow (`.github/workflows/release.yml`)

This workflow runs when a new tag is pushed:

- Creates a GitHub release
- Builds and signs release APK
- Uploads APK to release assets

## Build Configuration

### Gradle Properties

The project uses the following key Gradle properties:

- `android.useAndroidX=true` - Enables Jetpack libraries
- `android.enableJetifier=true` - Migrates libraries to AndroidX
- `kotlin.code.style=official` - Uses official Kotlin coding conventions
- `android.enableR8.fullMode=true` - Enables advanced code shrinking and optimization

### Build Variants

The project supports multiple build variants:

- **Debug**: For development and testing (with debug symbols)
- **Release**: For production releases (optimized, signed)

## Troubleshooting Common Build Issues

### Issue: "Failed to find target with hash string 'android-34'"

**Solution:**
```bash
sdkmanager "platforms;android-34"
```

### Issue: "Could not find com.android.tools.build:gradle:X.X.X"

**Solution:**
Check that your `build.gradle` file has the correct repository configuration:

```gradle
repositories {
    google()
    mavenCentral()
}
```

### Issue: "Execution failed for task ':app:lint'"

**Solution:**
```bash
# To run without lint checks
./gradlew assembleDebug -x lint

# To fix lint issues
./gradlew lintFix
```

### Issue: "Out of memory error during build"

Add the following to your `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.enableR8.fullMode=false
```

## Testing

### Unit Tests

Run unit tests with:
```bash
./gradlew testDebugUnitTest
```

### Instrumentation Tests

Run instrumentation tests with:
```bash
./gradlew connectedDebugAndroidTest
```

## Code Quality Checks

### Static Analysis

The project uses various tools for code quality:

- **Lint**: Checks for structural problems in the code
- **Detekt**: Static code analysis for Kotlin
- **Ktlint**: Kotlin code formatter

Run all checks:
```bash
./gradlew check
```

## Release Process

### Creating a Release

1. Update the version in `version.properties`:
   ```
   version.major=X
   version.minor=Y
   version.patch=Z
   ```

2. Create and push a tag:
   ```bash
   git tag -a vX.Y.Z -m "Release version X.Y.Z"
   git push origin vX.Y.Z
   ```

3. The release workflow will automatically create a GitHub release with the APK.

## Continuous Integration

The GitHub Actions workflows ensure:

- All code changes are built and tested automatically
- Pull requests are validated before merging
- Code quality checks are performed
- APK artifacts are generated and stored
- Proper versioning is maintained

## Building for Different Architectures

To build for specific architectures:

```bash
# Build for arm64-v8a only
./gradlew assembleDebug -Pandroid.ndk.abiFilters=arm64-v8a

# Build for multiple architectures
./gradlew assembleDebug -Pandroid.ndk.abiFilters=arm64-v8a,armeabi-v7a,x86,x86_64
```

## Proguard/R8 Configuration

The project includes Proguard rules for code shrinking and obfuscation. The configuration is located in `app/proguard-rules.pro`.

To build with different shrinking options:

```bash
# Build with full R8 optimization
./gradlew assembleRelease

# Build with disabled shrinking
./gradlew assembleRelease -Pandroid.disableResourceShrinking=true
```

## Environment Variables

For local builds, you can set these environment variables:

- `ANDROID_HOME`: Path to Android SDK
- `JAVA_HOME`: Path to JDK installation
- `GRADLE_OPTS`: Additional options for Gradle (e.g., `-Dorg.gradle.daemon=false`)

## Build Performance Tips

1. Use Gradle daemon for faster builds (enabled by default)
2. Enable parallel builds by adding `org.gradle.parallel=true` to `gradle.properties`
3. Use build cache by adding `org.gradle.caching=true` to `gradle.properties`
4. Increase heap size if building on a machine with plenty of RAM

## Verifying Build Artifacts

After building, verify the APK:

```bash
# Check APK contents
aapt dump badging app/build/outputs/apk/debug/app-debug.apk

# Verify APK signature
apksigner verify app/build/outputs/apk/release/app-release.apk
```

This build guide ensures that anyone can successfully build Hexodus and understand the automated build process through GitHub Actions.