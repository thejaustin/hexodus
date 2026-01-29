# Hexodus: Comprehensive Feature Overview

## Introduction

Hexodus is a next-generation Android theming engine that incorporates features inspired by numerous projects from the awesome-shizuku repository. This document provides a comprehensive overview of all the features and capabilities, organized by category.

## Core Theming Engine

### Hex-to-Overlay Compilation
- Converts hex color codes into system-compatible overlay APKs
- Generates AndroidManifest.xml with proper overlay declarations
- Creates values resources with custom color definitions
- Generates Material You (Android 12+) resource overlays
- Creates overlay assets for advanced theming

### Material You Override
- Bypasses One UI 8's aggressive Monet/Material You enforcement
- Generates custom color palettes from hex input
- Creates dynamic color schemes that integrate with the system
- Component-specific color application

### High Contrast Injection
- Creates signed APKs mimicking Samsung's High Contrast themes
- Injects custom RROs (Runtime Resource Overlays)
- Bypasses standard theme validation checks
- Component-specific theming

## System Integration & Management

### Shizuku Bridge
- Leverages Shizuku for INSTALL_PACKAGES and DUMP permissions
- Executes shell commands through Shizuku
- Manages overlay commands (enable/disable/set-priority)
- Handles APK installation/uninstallation
- Provides security validation

### Overlay Management
- Advanced overlay activation/deactivation through trusted shell processes
- Validates APK signatures for security
- Refreshes system UI to apply changes
- Manages overlay lifecycles
- Provides security checks

### System UI Tuner Capabilities
- Modify system settings using Shizuku
- Get current system setting values
- Toggle immersive mode
- Access hidden settings normally unavailable to apps
- Modify quick settings options and grid size
- Control status bar icons

## App & Device Management

### Per-App Theming (Inspired by DarQ)
- Enable/disable force dark mode for individual apps
- Apply custom themes to specific applications
- Query current app theme settings
- Manage app-specific theme configurations

### Advanced App Management (Inspired by Hail, Ice Box, Inure App Manager)
- **Freeze/Unfreeze Apps**: Disable/enable apps without uninstalling
- **Hide/Unhide Apps**: Hide apps from the launcher interface
- **Force Stop**: Terminate app processes
- **Batch Operations**: Perform actions on multiple apps simultaneously
- **App Information**: Get detailed information about installed apps
- **System App Management**: Manage system applications

### App Locking & Security (Inspired by AppLock)
- Secure apps with PIN or biometric authentication
- Lock/unlock apps with various authentication methods
- Manage locked app states

## Media & Notifications

### Now Playing Integration (Inspired by AmbientMusicMod)
- Update now playing information
- Display media information from various sources
- Control media sessions
- Customize notification appearance and behavior

### Notification Management
- Hide/show specific notifications
- Customize notification appearance and behavior
- Control notification settings

## Audio Enhancement

### Audio System Management (Inspired by RootlessJamesDSP)
- **Equalizer Control**: Adjust various frequency bands for fine-tuned sound
- **Bass Boost**: Enhance bass frequencies for richer audio
- **Audio Effects**: Apply various audio processing effects
- **Session Management**: Control audio for specific applications
- **Audio Session Control**: Get active audio sessions

## Privacy & Security

### File & App Hiding (Inspired by Amarok-Hider)
- Hide/unhide files from other apps
- Hide/unhide apps from the launcher interface
- Change file attributes to hide them
- Manage hidden file lists

### Privacy Management (Inspired by PrivacyFlip)
- Manage privacy based on device lock state
- Context-aware privacy controls
- Privacy scanning for potential issues
- Automatic privacy adjustments

## Network Management

### Firewall Capabilities (Inspired by ShizuWall, de1984)
- **App Firewall**: Block network access for specific applications
- **Custom Rules**: Create custom firewall rules
- **Network Monitoring**: Scan and monitor network activity
- **Connection Control**: Allow/block specific network types (WiFi/mobile)
- **Network Activity Tracking**: Monitor app network usage

## Power Management

### Battery Optimization (Inspired by BatStats, EnforceDoze)
- **Battery Statistics**: Get detailed battery usage information
- **Doze Control**: Enforce or disable Doze mode
- **Power Profiles**: Set performance vs battery saving profiles
- **App Optimization**: Optimize battery usage for specific apps
- **Scheduling**: Schedule power optimizations
- **Power Usage Monitoring**: Get power usage for specific apps

## Interaction & Automation

### Gesture Controls (Inspired by TapTap)
- Register custom gestures with associated actions
- Execute various actions (launch apps, media control, volume control)
- Support for back gesture customization
- Customizable gesture-action mappings
- Double/triple tap on back of device actions

### Automation Features (Inspired by MacroDroid, Tasker)
- Trigger actions based on various conditions
- Automate routine tasks
- Create custom macros for productivity

## File Management

### Advanced File Operations (Inspired by MiXplorer, SDMaid)
- Access protected directories like /Android/data and /Android/obb
- Batch operations on files
- Advanced file management capabilities

## Development & System Tools

### System Inspection (Inspired by LibChecker)
- View libraries used in apps on your device
- Determine installation sources of other apps
- Analyze app components and permissions

### Development Utilities (Inspired by various tools)
- Access to system APIs with elevated privileges
- Development tools for debugging and analysis
- System-level operations without requiring device root

## Foldable Device Support

### Dual-Screen Optimization (Inspired by various foldable tools)
- Detect cover/main screen usage
- Adapt UI for different screen contexts
- Optimize for Samsung Z Flip 5
- Manage dual-screen layouts

## Patching & Modification

### App Patching (Inspired by LSPatch, Morphe, Universal-ReVanced-Manager)
- Non-root app modification capabilities
- Apply patches to applications
- Customize app behavior
- Remove unwanted features

## Quick Settings & System Controls

### Quick Settings Enhancement (Inspired by various QS tiles)
- Add custom quick settings tiles
- Control system features from quick settings
- Toggle various system modes

## Vendor-Specific Features

### Samsung OneUI Enhancements
- Galaxy MaxHz controls
- Font customization
- Battery tweaks
- Various Samsung-specific optimizations

### Google Pixel Features
- Smartspacer integration
- Carrier settings optimization
- Various Pixel-specific features

## Security Model

Hexodus implements multiple layers of security:
1. APK signature validation
2. Path validation for file operations
3. Input sanitization to prevent injection attacks
4. Component isolation for service security
5. Permission checks for all operations

## Integration Capabilities

The app integrates with various system services and third-party applications to provide a seamless experience while maintaining security and stability.