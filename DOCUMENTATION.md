# Hexodus Documentation

## Overview

Hexodus is a next-generation Android theming engine for Samsung One UI 8 (Android 16+) as a spiritual successor to 'Hex Installer'. It leverages Shizuku for system-level operations without requiring root access, enabling deep customization of the Android UI.

## Architecture

### Core Components

#### 1. ThemeCompiler
- **Location**: `com.hexodus.core.ThemeCompiler`
- **Purpose**: Compiles hex color codes into system-compatible overlay APKs
- **Features**:
  - Generates AndroidManifest.xml with proper overlay declarations
  - Creates values resources with custom color definitions
  - Generates Material You (Android 12+) resource overlays
  - Creates overlay assets for advanced theming
  - Component-specific theming based on user preferences

#### 2. ShizukuBridgeService
- **Location**: `com.hexodus.services.ShizukuBridgeService`
- **Purpose**: Bridges communication between app and Shizuku
- **Features**:
  - Executes shell commands through Shizuku
  - Manages overlay commands (enable/disable/set-priority)
  - Handles APK installation/uninstallation
  - Provides security validation
  - Monitors Shizuku connection state

#### 3. OverlayActivationService
- **Location**: `com.hexodus.services.OverlayActivationService`
- **Purpose**: Manages activation/deactivation of theme overlays
- **Features**:
  - Installs and enables overlays using Shizuku
  - Validates APK signatures for security
  - Refreshes system UI to apply changes
  - Manages overlay lifecycles
  - Provides security checks

#### 4. MonetOverrideService
- **Location**: `com.hexodus.services.MonetOverrideService`
- **Purpose**: Overrides system's Material You color generation
- **Features**:
  - Generates custom color palettes from hex input
  - Bypasses One UI 8's aggressive Monet enforcement
  - Creates dynamic color schemes that integrate with system
  - Component-specific color application

#### 5. HighContrastInjectorService
- **Location**: `com.hexodus.services.HighContrastInjectorService`
- **Purpose**: Exploits Samsung's High Contrast accessibility themes
- **Features**:
  - Creates signed APKs mimicking High Contrast themes
  - Injects custom RROs (Runtime Resource Overlays)
  - Bypasses standard theme validation checks
  - Component-specific theming

#### 6. FoldableDisplayService
- **Location**: `com.hexodus.services.FoldableDisplayService`
- **Purpose**: Handles foldable device contexts
- **Features**:
  - Detects cover/main screen usage
  - Adapts UI for different screen contexts
  - Optimizes for Samsung Z Flip 5
  - Manages dual-screen layouts

#### 7. ThemeManagerService
- **Location**: `com.hexodus.services.ThemeManagerService`
- **Purpose**: Central theme management
- **Features**:
  - Creates themes from hex colors and preferences
  - Applies themes to system
  - Manages theme persistence
  - Handles theme sharing/export
  - Component-specific theming

### 8. SystemTunerService
- **Location**: `com.hexodus.services.SystemTunerService`
- **Purpose**: Access and modify hidden system settings
- **Features**:
  - Modify system settings using Shizuku
  - Get current system setting values
  - Toggle immersive mode
  - Access hidden settings normally unavailable to apps
  - Inspired by System UI Tuner project

### 9. AppThemerService
- **Location**: `com.hexodus.services.AppThemerService`
- **Purpose**: Per-app theming features
- **Features**:
  - Enable/disable force dark mode for individual apps
  - Apply custom themes to specific applications
  - Query current app theme settings
  - Manage app-specific theme configurations
  - Inspired by DarQ project

### 10. GestureManagerService
- **Location**: `com.hexodus.services.GestureManagerService`
- **Purpose**: Gesture and interaction customization
- **Features**:
  - Register custom gestures with associated actions
  - Execute various actions (launch apps, media control, volume control)
  - Manage gesture-action mappings
  - Support for back gesture customization
  - Inspired by TapTap project

### 11. MediaNotificationService
- **Location**: `com.hexodus.services.MediaNotificationService`
- **Purpose**: Media and notification customization
- **Features**:
  - Update now playing information
  - Hide/show specific notifications
  - Customize notification appearance and behavior
  - Control media sessions
  - Inspired by AmbientMusicMod project

### 12. AudioManagerService
- **Location**: `com.hexodus.services.AudioManagerService`
- **Purpose**: Audio system management and enhancement
- **Features**:
  - Set equalizer values for audio sessions
  - Configure bass boost levels
  - Apply various audio effects
  - Get active audio sessions
  - Inspired by RootlessJamesDSP project

### 13. AppManagerService
- **Location**: `com.hexodus.services.AppManagerService`
- **Purpose**: Advanced app management capabilities
- **Features**:
  - Freeze/unfreeze apps using Shizuku
  - Hide/unhide apps from launcher
  - Force stop apps
  - Batch operations on multiple apps
  - Get detailed app information
  - Inspired by Hail, Ice Box, and Inure App Manager projects

### 14. PrivacySecurityService
- **Location**: `com.hexodus.services.PrivacySecurityService`
- **Purpose**: Privacy and security features
- **Features**:
  - Lock/unlock apps with PIN/biometric
  - Hide/unhide files and apps
  - Manage privacy based on device lock state
  - Scan for privacy issues
  - Inspired by AppLock, Amarok-Hider, and PrivacyFlip projects

### 15. NetworkFirewallService
- **Location**: `com.hexodus.services.NetworkFirewallService`
- **Purpose**: Network management and firewall capabilities
- **Features**:
  - Block/allow network access for apps
  - Set custom firewall rules
  - Scan network activity
  - Get firewall rules
  - Monitor app network usage
  - Inspired by ShizuWall and de1984 projects

### 16. PowerManagerService
- **Location**: `com.hexodus.services.PowerManagerService`
- **Purpose**: Power management and battery optimization
- **Features**:
  - Get battery statistics
  - Enforce/doze disable doze mode
  - Set power profiles (performance, balanced, battery save)
  - Optimize app battery usage
  - Get power usage for apps
  - Schedule power optimizations
  - Inspired by BatStats and EnforceDoze projects

## Features

### Core Theming Engine
- **Hex-to-Overlay Compilation**: Converts hex color codes into system-compatible overlay APKs with enhanced security validation
- **Material You Override**: Bypasses One UI 8's aggressive Monet/Material You enforcement with dynamic color generation
- **Dynamic Color Generation**: Creates custom color schemes that integrate seamlessly with the system using advanced tonal palettes
- **Multi-Component Theming**: Apply themes to specific system components (status bar, navigation bar, system UI, settings, launcher, etc.)
- **Theme Persistence**: Save and load custom themes with full component configuration
- **Theme Sharing**: Export and import themes with complete configuration preservation

### System Integration
- **Shizuku Bridge**: Leverages Shizuku for INSTALL_PACKAGES, DUMP, and SYSTEM_ALERT_WINDOW permissions with security validation
- **Overlay Management**: Advanced overlay activation/deactivation through trusted shell processes with signature verification
- **System UI Modification**: Safe modifications to SystemUI, Settings, Framework, and other system components
- **Non-Root Privileges**: Operates without requiring root access while maintaining system-level capabilities

### Device Optimization
- **Foldable Display Support**: Optimized for Samsung Z Flip 5 with dual-screen awareness and adaptive layouts
- **Cover Screen Adaptation**: Automatically adjusts UI for cover screen visibility with optimized layouts
- **Performance Optimization**: Efficient resource usage across all device types with adaptive rendering
- **Multi-Window Support**: Enhanced support for Samsung DeX and multi-window environments

### Advanced Capabilities
- **High Contrast Injection**: Exploits Samsung's High Contrast accessibility themes to bypass standard theme checks with security validation
- **Dynamic Color Adaptation**: Real-time color adaptation based on wallpaper and user preferences
- **Component-Specific Theming**: Granular control over individual system UI components
- **Real-time Preview**: Live preview of themes before applying with instant feedback

### Enhanced Features Inspired by Awesome Shizuku Projects

#### System UI Tuner Capabilities (Inspired by System UI Tuner)
- **Hidden Settings Access**: Modify system settings that are normally hidden with safety checks
- **Immersive Mode Toggle**: Enable/disable immersive mode programmatically with proper validation
- **Quick Settings Customization**: Modify quick settings options, grid size, and tile arrangements
- **Status Bar Icon Control**: Hide/show specific status bar icons with accessibility compliance
- **System Animation Control**: Adjust system animation scales and behaviors
- **Display Cutout Customization**: Modify display cutout behaviors and appearances

#### Per-App Theming (Inspired by DarQ/Darken)
- **Force Dark Mode**: Enable/disable force dark mode for individual apps with proper validation
- **App-Specific Themes**: Apply custom themes to specific applications with component-level control
- **Theme Presets per App**: Save and load app-specific theme configurations with full state preservation
- **Per-App Accent Colors**: Customize accent colors for individual applications
- **App Component Theming**: Theme specific UI elements within apps (toolbars, buttons, etc.)

#### Gesture Controls (Inspired by TapTap/TapGesture)
- **Back Gesture Customization**: Double/triple tap on back of device actions with safety validation
- **Gesture Actions**: Launch apps, control media, adjust volume, toggle features via gestures
- **Customizable Gestures**: Assign different actions to various gestures with extensive customization
- **Edge Gesture Controls**: Swipe from screen edges for various actions
- **Air Gesture Simulation**: Simulate air gesture controls for enhanced interaction

#### Media & Notifications (Inspired by AmbientMusicMod)
- **Now Playing Integration**: Display media information from various sources with rich controls
- **Notification Customization**: Modify appearance, behavior, and grouping of notifications
- **Media Session Control**: Control playback and display metadata from the theming engine
- **Media Visualizations**: Add custom visualizations to media controls
- **Smart Pause Integration**: Automatically pause media when headphones disconnect

#### App Management Features (Inspired by various awesome-shizuku projects)
- **App Freezing/Unfreezing**: Freeze apps without uninstalling them with batch operations
- **App Hiding**: Hide apps from the launcher with secure restoration
- **Batch Operations**: Perform actions on multiple apps simultaneously with progress tracking
- **App Permissions Management**: View and modify app permissions with safety checks
- **App Data Management**: Clear app data, cache, and manage storage with validation

#### Security & Privacy Enhancements
- **APK Signature Validation**: Verify signatures of all theme overlays before installation
- **Path Validation**: Validate all file paths to prevent directory traversal attacks
- **Input Sanitization**: Sanitize all user inputs to prevent injection attacks
- **Command Validation**: Validate shell commands before execution through Shizuku
- **Package Name Validation**: Validate package names to prevent injection attacks
- **File Integrity Checks**: Verify integrity of theme files before application

#### Accessibility Features
- **High Contrast Mode**: Enhanced high contrast support for better visibility
- **Large Text Support**: Proper scaling for users with large text preferences
- **Reduced Motion**: Respects user preferences for reduced motion
- **Screen Reader Compatibility**: Full compatibility with TalkBack and other screen readers
- **Touch Target Sizes**: Proper touch target sizes for users with motor impairments
- **Color Blindness Support**: Alternative color schemes for different types of color blindness

#### Performance & Optimization
- **Efficient Resource Usage**: Optimized for minimal battery and memory usage
- **Background Processing**: Intelligent background operations with battery optimization
- **Cache Management**: Efficient caching of theme resources and previews
- **Memory Management**: Proper memory management to prevent leaks and crashes
- **Threading Model**: Proper threading model with background processing for heavy operations

### Security Model
- **Input Validation**: All inputs are validated against security policies
- **Path Sanitization**: File paths are sanitized to prevent directory traversal
- **Command Filtering**: Shell commands are filtered to prevent dangerous operations
- **Package Verification**: All packages are verified before theme application
- **Permission Checks**: All operations verify proper permissions before execution
- **Secure Storage**: Sensitive data is stored securely using Android Keystore

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

## Security Model

Hexodus implements multiple layers of security:

1. **APK Signature Validation**: All overlay APKs are validated before installation
2. **Path Validation**: File paths are checked against allowed directories
3. **Input Sanitization**: All user inputs are sanitized to prevent injection attacks
4. **Component Isolation**: Services are isolated to prevent unauthorized access
5. **Permission Checks**: All operations verify Shizuku permissions before execution

## Usage

### Creating a Theme
1. Enter a hex color code (e.g., #FF6200EE)
2. Select which system components to theme
3. Preview the theme
4. Apply to system

### Advanced Options
- **Component Selection**: Choose which system elements to theme
- **Theme Persistence**: Save and load custom themes
- **Theme Sharing**: Export themes for sharing with others

## Dependencies

The project uses the following key dependencies:
- `androidx.compose.material3:material3` - For modern UI with Material You support
- `rikka.shizuku:api` and `rikka.shizuku:provider` - For Shizuku integration
- `androidx.window:window` - For foldable device support
- `androidx.security:security-crypto` - For secure data storage
- `com.google.accompanist:accompanist-permissions` - For permission management in Compose

## Integration with Awesome Shizuku Projects

Hexodus draws inspiration from various projects in the awesome-shizuku repository:
- **Hex Installer**: Original concept for hex-based theming
- **LibChecker**: For system library inspection capabilities
- **PrivacyFlip**: For system state awareness
- **ShizuWall**: For secure system operations
- **MacroDroid**: For automation capabilities

## Development

### Building the Project
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on a device with Shizuku installed

### Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Troubleshooting

### Common Issues
- **Shizuku Not Connected**: Ensure Shizuku is installed and properly configured
- **Permission Denied**: Grant all requested permissions to the app
- **Theme Not Applied**: Check if overlay installation was successful in system logs

### Debugging
- Check Android logs for service-specific messages
- Verify Shizuku connection status
- Ensure device meets minimum requirements (Android 16+, One UI 8+)

## Additional Services

### 17. SystemTunerService
- **Location**: `com.hexodus.services.SystemTunerService`
- **Purpose**: Access and modify hidden system settings
- **Features**:
  - Modify system settings using Shizuku
  - Get current system setting values
  - Toggle immersive mode
  - Access hidden settings normally unavailable to apps
  - Inspired by System UI Tuner project

### 18. AppThemerService
- **Location**: `com.hexodus.services.AppThemerService`
- **Purpose**: Per-app theming features
- **Features**:
  - Enable/disable force dark mode for individual apps
  - Apply custom themes to specific applications
  - Query current app theme settings
  - Manage app-specific theme configurations
  - Inspired by DarQ project

### 19. GestureManagerService
- **Location**: `com.hexodus.services.GestureManagerService`
- **Purpose**: Gesture and interaction customization
- **Features**:
  - Register custom gestures with associated actions
  - Execute various actions (launch apps, media control, volume control)
  - Manage gesture-action mappings
  - Support for back gesture customization
  - Inspired by TapTap project

### 20. MediaNotificationService
- **Location**: `com.hexodus.services.MediaNotificationService`
- **Purpose**: Media and notification customization
- **Features**:
  - Update now playing information
  - Hide/show specific notifications
  - Customize notification appearance and behavior
  - Control media sessions
  - Inspired by AmbientMusicMod project

### 21. BackupRestoreService
- **Location**: `com.hexodus.services.BackupRestoreService`
- **Purpose**: Theme and settings backup/restore functionality
- **Features**:
  - Create comprehensive backups of themes and settings
  - Restore themes and settings from backup files
  - Export/import individual themes
  - List available backups
  - Validate backup files
  - Delete backup files
  - Support for multiple backup types (full, themes-only, settings-only)

### 22. AdvancedThemingService
- **Location**: `com.hexodus.services.AdvancedThemingService`
- **Purpose**: Advanced theming features
- **Features**:
  - Create gradient themes with multiple colors
  - Generate animated themes with various effects
  - Apply texture-based themes
  - Get theme presets
  - Apply theme transitions
  - Create theme animations
  - Generate dynamic color palettes from images
  - Inspired by various theming projects from awesome-shizuku

### 23. SystemInspectorService
- **Location**: `com.hexodus.services.SystemInspectorService`
- **Purpose**: System resource inspection and management
- **Features**:
  - Get libraries used by apps
  - Access system properties
  - Get app resources (drawables, strings, colors, layouts)
  - Determine app installation sources
  - Get app ABI information
  - Get system health information
  - Get detailed app information
  - List all installed apps
  - Inspired by LibChecker project

### 24. DeviceSpecificService
- **Location**: `com.hexodus.services.DeviceSpecificService`
- **Purpose**: Device-specific optimizations and features
- **Features**:
  - Get detailed device information
  - Optimize UI for foldable devices
  - Get display features including foldable capabilities
  - Manage Samsung DeX mode
  - Get Bixby capabilities on Samsung devices
  - Manage One UI specific features
  - Monitor display changes for foldable devices
  - Handle window layout information for different device states
  - Inspired by various device-specific projects from awesome-shizuku

### 25. PrivacyManagerService
- **Location**: `com.hexodus.services.PrivacyManagerService`
- **Purpose**: Privacy and permission management
- **Features**:
  - Get permissions for an app
  - Set permissions for an app
  - Get usage statistics for an app
  - Manage app tracking settings
  - Calculate privacy scores for apps
  - Get apps with dangerous permissions
  - Revoke dangerous permissions for apps
  - Inspired by privacy-focused projects from awesome-shizuku

### 26. NetworkFirewallService
- **Location**: `com.hexodus.services.NetworkFirewallService`
- **Purpose**: Network management and firewall features
- **Features**:
  - Block network access for apps
  - Allow network access for apps
  - Get network access status for apps
  - Get network activity information
  - Manage firewall rules
  - Get firewall status
  - Start/stop network monitoring
  - Get active network information
  - Inspired by firewall projects from awesome-shizuku

### 27. PerformanceOptimizerService
- **Location**: `com.hexodus.services.PerformanceOptimizerService`
- **Purpose**: System performance and optimization
- **Features**:
  - Get battery statistics
  - Optimize individual apps
  - Manage power modes (performance, balanced, battery_saver)
  - Get storage statistics
  - Clean storage
  - Get memory information
  - Get app-specific performance stats
  - Force app optimization
  - Inspired by power management projects from awesome-shizuku

## Roadmap

Future enhancements planned for Hexodus:
- Enhanced theme sharing capabilities
- Integration with more system components
- Advanced animation theming
- Community theme marketplace
- Automated theme generation from wallpapers