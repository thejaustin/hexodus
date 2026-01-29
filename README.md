# Hexodus

A next-generation Android theming engine for Samsung One UI 8 (Android 16+) as a spiritual successor to 'Hex Installer', leveraging Shizuku for system-level operations without requiring root access. Named as a combination of "Hex" (from Hex Installer) and "Android" (with "odus" suggesting a path forward).

## 🚀 Features

### Core Theming Engine
- **Hex-to-Overlay Compilation**: Converts hex color codes into system-compatible overlay APKs
- **Material You Override**: Bypasses One UI 8's aggressive Monet/Material You enforcement
- **Dynamic Color Generation**: Creates custom color schemes that integrate seamlessly with the system
- **Multi-Theme Support**: Apply different themes to different system components

### System Integration
- **Shizuku Bridge**: Leverages Shizuku for INSTALL_PACKAGES and DUMP permissions
- **Overlay Management**: Advanced overlay activation/deactivation through trusted shell processes
- **System UI Modification**: Safe modifications to SystemUI, Settings, and Framework components
- **Non-Root Privileges**: Operates without requiring root access

### Device Optimization
- **Foldable Display Support**: Optimized for Samsung Z Flip 5 with dual-screen awareness
- **Cover Screen Adaptation**: Automatically adjusts UI for cover screen visibility
- **Performance Optimization**: Efficient resource usage across all device types

### Advanced Capabilities
- **High Contrast Injection**: Exploits Samsung's High Contrast accessibility themes to bypass standard theme checks
- **Theme Sharing**: Export and import custom themes
- **Theme Presets**: Pre-made themes based on popular color palettes
- **Real-time Preview**: Live preview of themes before applying

### Enhanced Features Inspired by Awesome Shizuku Projects

#### System UI Tuner Capabilities (Inspired by System UI Tuner)
- **Hidden Settings Access**: Modify system settings that are normally hidden
- **Immersive Mode Toggle**: Enable/disable immersive mode programmatically
- **Quick Settings Customization**: Modify quick settings options and grid size
- **Status Bar Icon Control**: Hide/show specific status bar icons
- **System Property Access**: Get and modify system properties using Shizuku

#### Per-App Theming (Inspired by DarQ/Darken)
- **Force Dark Mode**: Enable/disable force dark mode for individual apps
- **App-Specific Themes**: Apply custom themes to specific applications
- **Theme Presets per App**: Save and load app-specific theme configurations
- **Per-App Accent Colors**: Customize accent colors for individual applications

#### Gesture Controls (Inspired by TapTap/TapGesture)
- **Back Gesture Customization**: Double/triple tap on back of device actions
- **Gesture Actions**: Launch apps, control media, adjust volume, toggle features via gestures
- **Customizable Gestures**: Assign different actions to various gestures
- **Edge Gesture Controls**: Swipe from screen edges for various actions
- **Gesture Mapping**: Associate gestures with specific actions

#### Media & Notifications (Inspired by AmbientMusicMod)
- **Now Playing Integration**: Display media information from various sources
- **Notification Customization**: Modify appearance and behavior of notifications
- **Media Session Control**: Control playback from the theming engine
- **Media Visualizations**: Add custom visualizations to media controls
- **Media Session Management**: Control media sessions across different apps

#### App Management Features (Inspired by various awesome-shizuku projects)
- **App Freezing/Unfreezing**: Freeze apps without uninstalling them
- **App Hiding**: Hide apps from the launcher with secure restoration
- **Batch Operations**: Perform actions on multiple apps simultaneously
- **App Permissions Management**: View and modify app permissions
- **App Data Management**: Clear app data, cache, and manage storage
- **App Information**: Get detailed information about installed apps

#### System Tuning (Inspired by System UI Tuner)
- **Hidden Settings Access**: Modify system settings normally hidden from users
- **Immersive Mode Control**: Toggle immersive mode programmatically
- **Quick Settings Customization**: Modify quick settings options and grid size
- **Status Bar Icon Control**: Hide/show specific status bar icons
- **System Property Access**: Get system properties using Shizuku

#### Advanced Theming (Inspired by various theming projects)
- **Gradient Themes**: Create themes with gradient color schemes
- **Animated Themes**: Apply themes with animated elements
- **Texture Themes**: Use textures in theme design
- **Theme Transitions**: Smooth transitions between different themes
- **Dynamic Color Palettes**: Generate color schemes from images

#### System Inspection (Inspired by LibChecker)
- **App Library Inspection**: View libraries used by applications
- **System Property Access**: Get system properties using Shizuku
- **App Resource Inspection**: Access app resources (drawables, strings, etc.)
- **Installation Source Detection**: Determine how apps were installed
- **ABI Information**: Get app architecture information

#### Gesture Management (Inspired by TapTap)
- **Back Gesture Customization**: Double/triple tap on back of device
- **Gesture Actions**: Launch apps, control media, adjust volume via gestures
- **Customizable Gestures**: Assign different actions to various gestures
- **Gesture Mapping**: Associate gestures with specific actions

#### Media & Notification Control (Inspired by AmbientMusicMod)
- **Now Playing Display**: Show media information from various sources
- **Notification Management**: Hide/show specific notifications
- **Media Session Control**: Control playback from the theming engine
- **Media Visualization**: Add custom visualizations to media controls

#### Backup & Restore (Inspired by various backup projects)
- **Comprehensive Backups**: Backup themes, settings, and configurations
- **Selective Restoration**: Restore specific components from backups
- **Theme Export/Import**: Share themes with other users
- **Multiple Backup Types**: Full, themes-only, settings-only backups

#### Device-Specific Features (Inspired by foldable projects)
- **Foldable Display Optimization**: Optimize for Samsung Z Flip 5 and other foldables
- **Cover Screen Adaptation**: Adjust UI for cover screen visibility
- **Display Context Awareness**: Detect and adapt to different display states
- **Multi-Window Support**: Enhanced support for Samsung DeX and multi-window

#### Privacy & Security (Inspired by privacy-focused projects)
- **App Permission Management**: View and modify app permissions
- **Privacy Score Calculation**: Assess app privacy based on permissions
- **Dangerous Permission Revocation**: Remove dangerous permissions from apps
- **Usage Statistics**: Get app usage statistics

#### Network Management (Inspired by firewall projects)
- **App Firewall**: Block network access for specific applications
- **Network Monitoring**: Monitor app network activity
- **Firewall Rules**: Create custom firewall rules
- **Network Status**: Get active network information

#### Performance Optimization (Inspired by power management projects)
- **Battery Statistics**: Get detailed battery usage information
- **App Optimization**: Optimize individual app performance
- **Power Mode Management**: Control performance vs battery saving modes
- **Storage Management**: Get storage statistics and clean storage

## 🎨 Feature Organization & UI/UX

### Visual Feature Dashboard
Hexodus features a comprehensive dashboard with:
- **Visual Indicators**: Each feature category has a distinct color indicator
- **Clear Toggles**: One-touch enable/disable for all features
- **Categorized Layout**: Features organized by function and purpose
- **Detailed Explanations**: Each feature includes a clear description
- **Status Indicators**: Visual cues showing feature status (enabled/disabled)

### Feature Categories
All features are organized into clear categories:
1. **Theming & Customization**: Core theming engine and color management
2. **System Integration**: System-level operations and settings
3. **App Management**: App freezing, hiding, and management
4. **Privacy & Security**: App locking, file hiding, and privacy controls
5. **Network & Power**: Firewall, battery optimization, and network controls
6. **Audio & Media**: Equalizer, audio effects, and media controls
7. **Interaction**: Gesture controls and device interaction
8. **Foldable Support**: Optimizations for foldable devices

### Intuitive Navigation
- **Dashboard View**: Overview of all features with quick toggles
- **Category Exploration**: Drill down into specific feature sets
- **Feature Details**: Detailed information about each capability
- **Status Tracking**: Visual indicators for feature states

## 🛠️ Architecture

### Core Components

## 🚀 GitHub Actions Build Process

Hexodus is configured with comprehensive GitHub Actions workflows that automatically build, test, and package the application when changes are pushed to the repository.

### Build Workflow Features
- **Automatic Building**: Builds debug and release APKs on every push
- **Unit Testing**: Runs comprehensive unit tests with each build
- **Multi-API Testing**: Tests on multiple Android API levels (26, 28, 29, 30, 31, 33, 34)
- **Artifact Upload**: Automatically uploads built APKs as workflow artifacts
- **Release Creation**: Creates GitHub releases with signed APKs when tags are pushed

### Workflow Configuration
The repository includes two main workflows:
1. **Build Workflow** (`.github/workflows/build.yml`):
   - Runs on every push and pull request
   - Builds debug and release APKs
   - Runs unit and instrumentation tests
   - Uploads APK artifacts

2. **Release Workflow** (`.github/workflows/release.yml`):
   - Triggers when a new tag is pushed
   - Creates a GitHub release
   - Builds and signs release APK
   - Uploads APK to release assets

### Building Locally
To build the project locally, ensure you have the Android SDK and build tools installed, then run:
```bash
./gradlew assembleDebug
```
or for a release build:
```bash
./gradlew assembleRelease
```

The APK files will be generated in `app/build/outputs/apk/`.

### Continuous Integration
The CI/CD pipeline ensures:
- Code quality through automated testing
- Compatibility across different Android versions
- Proper APK signing for releases
- Automatic documentation of changes

1. **ThemeCompiler**: Compiles hex colors into overlay APK structures in memory
2. **ShizukuBridgeService**: Proxies overlay commands through trusted shell processes
3. **MonetOverrideService**: Spoofs the system's color palette generation
4. **HighContrastInjectorService**: Generates signed APKs mimicking Samsung's High Contrast themes
5. **FoldableDisplayService**: Handles dual-display contexts for Z Flip 5

### Inspired by Awesome Shizuku Projects

This project draws inspiration from various projects in the [awesome-shizuku](https://github.com/timschneeb/awesome-shizuku) repository:

- **Hex Installer**: Original concept for hex-based theming
- **LibChecker**: For system library inspection capabilities
- **PrivacyFlip**: For system state awareness
- **ShizuWall**: For secure system operations
- **MacroDroid**: For automation capabilities

## 🔧 Setup & Installation

### Prerequisites
- Android 16+ (One UI 8)
- Shizuku installed and properly configured
- Unlocked bootloader (for Sui on rooted devices)

### Installation Steps
1. Install Shizuku from GitHub/F-Droid
2. Grant Shizuku the necessary permissions
3. Install Hexodus from releases
4. Launch Hexodus and grant requested permissions

### Configuration
- Connect Shizuku via ADB or root (Sui)
- Configure theme preferences
- Apply your first hex-based theme

## 📦 Dependencies

```gradle
dependencies {
    // Core theming
    implementation 'androidx.compose.material3:material3:1.2.0'
    implementation 'com.google.android.material:material:1.11.0'
    
    // Shizuku integration
    implementation 'rikka.shizuku:api:13.1.5'
    implementation 'rikka.shizuku:provider:13.1.5'
    
    // Window management (for foldables)
    implementation 'androidx.window:window:1.5.1'
    implementation 'androidx.window:window-java:1.5.1'
    
    // Security
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'
    
    // Permissions (for Compose)
    implementation 'com.google.accompanist:accompanist-permissions:0.32.0'
}
```

## 🎨 Usage

### Creating a Theme
1. Open Hexodus
2. Enter your desired hex color code (e.g., #FF6200EE)
3. Customize which system components to theme
4. Preview the theme
5. Apply to system

### Advanced Options
- **Component Selection**: Choose which system elements to theme (status bar, navigation, etc.)
- **Intensity Control**: Adjust how strongly the theme affects the system
- **Export Themes**: Share your custom themes with others
- **Import Themes**: Apply themes created by others

## 🛡️ Security Model

Hexodus operates within Android's permission model while leveraging Shizuku for elevated operations:

### Core Security Principles
- **All system modifications are reversible**: Every change can be undone
- **Themes can be disabled instantly**: Immediate rollback capability
- **No permanent system changes without consent**: Explicit user approval required
- **Secure communication channels**: Encrypted communication with system services

### Enhanced Security Features
- **APK Signature Validation**: All overlay APKs are validated before installation
- **Path Validation**: All file paths are validated to prevent directory traversal
- **Input Sanitization**: All user inputs are sanitized to prevent injection attacks
- **Command Filtering**: Shell commands are filtered to prevent dangerous operations
- **Component Isolation**: Services are isolated to prevent unauthorized access
- **Permission Validation**: Proper permission checks for all operations
- **Secure Storage**: Sensitive data is stored using Android Keystore system
- **Encrypted Preferences**: Sensitive settings are stored encrypted
- **Runtime Validation**: All operations are validated at runtime
- **Secure IPC**: Protected inter-process communication between components

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

### Development Setup
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Commit your changes (`git commit -m 'Add amazing feature'`)
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🚀 Release & Versioning

### Versioning System
Hexodus follows Semantic Versioning (SemVer) with the format `MAJOR.MINOR.PATCH`:
- **MAJOR** version: For incompatible API changes or major feature additions
- **MINOR** version: For backward-compatible feature additions
- **PATCH** version: For backward-compatible bug fixes

### CI/CD Pipeline
- Automated builds on every commit to `main` branch
- Comprehensive testing pipeline
- Automatic release generation with changelog
- APK artifacts for both debug and release builds

### Release Process
1. Code changes are made in feature branches
2. Pull requests undergo code review and automated testing
3. Approved changes are merged to `develop` branch
4. Periodically, `develop` is merged to `main` for release
5. GitHub Actions automatically builds and creates releases

### Current Version
- Latest Release: [![Latest Release](https://img.shields.io/github/v/release/username/hexodus)](https://github.com/username/hexodus/releases)
- Build Status: [![Build Status](https://github.com/username/hexodus/actions/workflows/release.yml/badge.svg)](https://github.com/username/hexodus/actions/workflows/release.yml)

For detailed changes, see the [Changelog](CHANGELOG.md).

## 🙏 Acknowledgments

- The original Hex Installer by bodhi for inspiring this project
- The Shizuku project for enabling system-level operations without root
- The awesome-shizuku community for showcasing innovative Shizuku applications
- Google for Material Design and Material You concepts
- Samsung for One UI and its theming capabilities

## 🐛 Issues & Support

If you encounter any issues or have suggestions, please file an issue on the [GitHub Issues](https://github.com/username/hexodus/issues) page.

For support, join our community discussions or refer to the documentation.

---

**Disclaimer**: This project is for educational purposes and personal customization. The authors are not responsible for any issues that may arise from using this software. Use at your own risk.