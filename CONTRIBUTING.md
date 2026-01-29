# Contributing to Hexodus

We welcome contributions to Hexodus! This document provides guidelines for contributing to the project.

## Code of Conduct

Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md) to ensure a welcoming environment for everyone.

## How to Contribute

### Reporting Bugs

Before submitting a bug report, please:
1. Check if the issue has already been reported
2. Verify the issue exists in the latest version
3. Provide detailed information about the problem

Use the bug report template when creating an issue.

### Suggesting Features

Feature requests are welcome! Please:
1. Explain the problem the feature would solve
2. Describe the solution you'd like
3. Consider alternatives you've thought of
4. Use the feature request template

### Pull Requests

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Add tests if applicable
5. Update documentation if needed
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

## Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/username/hexodus.git
   ```

2. Open in Android Studio

3. Sync Gradle files

4. Build and run on a device with Shizuku installed

## Technical Guidelines

### Code Style
- Follow Android/Kotlin official style guides
- Use 4 spaces for indentation (no tabs)
- Use descriptive variable and function names
- Comment complex logic appropriately
- Follow Material 3 Extended (M3E) design guidelines

### Architecture
- Use modern Android architecture components
- Follow MVVM pattern where applicable
- Implement proper separation of concerns
- Use Jetpack Compose for new UI components
- Ensure proper state management

### Security
- Validate all inputs from external sources
- Sanitize data before passing to system APIs
- Implement proper permission checks
- Follow Android security best practices
- Use encrypted storage for sensitive data

### Accessibility
- Ensure proper content descriptions for UI elements
- Maintain minimum touch target sizes (48dp x 48dp)
- Provide sufficient color contrast ratios
- Support various accessibility services
- Test with TalkBack and other accessibility tools

### Testing
- Write unit tests for business logic
- Write instrumented tests for UI components
- Test on multiple device configurations
- Verify functionality with different Android versions
- Test with various Shizuku configurations

## Project Structure

```
hexodus/
├── app/                    # Main application code
│   ├── src/main/
│   │   ├── java/com/hexodus/
│   │   │   ├── core/       # Core theming engine
│   │   │   ├── services/   # System-level services
│   │   │   ├── ui/         # UI components and screens
│   │   │   ├── utils/      # Utility functions
│   │   │   └── theme/      # Material 3 theme definitions
│   │   └── res/            # Resources
├── .github/                # GitHub configuration
│   ├── workflows/          # CI/CD configurations
│   └── ISSUE_TEMPLATE/     # Issue templates
├── docs/                   # Documentation
└── build.gradle            # Build configuration
```

## Theming Engine Architecture

### Core Components
- `ThemeCompiler`: Converts hex colors to system-compatible overlays
- `ShizukuBridgeService`: Handles communication with Shizuku
- `OverlayActivationService`: Manages overlay activation/deactivation
- `MonetOverrideService`: Bypasses One UI 8's aggressive Material You enforcement
- `HighContrastInjectorService`: Exploits accessibility themes for deeper customization
- `FoldableDisplayService`: Optimizes for foldable devices like Z Flip 5

### Service Architecture
All services follow a consistent pattern:
- Use proper permission checks
- Validate inputs before processing
- Broadcast results via Intents
- Handle errors gracefully
- Follow Android service lifecycle

## Shizuku Integration

Hexodus leverages Shizuku for system-level operations:
- Use Shizuku APIs for elevated permissions
- Implement proper error handling for Shizuku unavailability
- Provide fallback options when possible
- Follow Shizuku security best practices

## Theming Guidelines

### Color Generation
- Follow Material You color generation principles
- Generate harmonious color palettes from hex input
- Ensure proper contrast ratios
- Support both light and dark themes

### Overlay Creation
- Create properly signed overlay APKs
- Follow Android overlay specification
- Include proper metadata and permissions
- Validate overlay integrity before installation

## Questions?

If you have questions about contributing, feel free to open an issue with the "question" label.

Thank you for contributing to Hexodus!