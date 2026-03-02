# Deprecation Bar Usage Guide

## Overview

The deprecation bar system provides a visual indicator for deprecated tools and features, showing users:
- ⚠️ That a feature is deprecated
- 📅 When it was deprecated
- 🗑️ When it will be removed
- ✅ What replacement to use instead
- 📖 Optional migration guide links

## Components

### 1. `DeprecationInfo` Data Class

Use this to define deprecation information for any feature:

```kotlin
DeprecationInfo(
    message = "Deprecated - Use new engine instead",
    replacement = "Hexodus Theme Engine v2",
    deprecatedSince = "2025.1",
    removeInVersion = "2026.2",
    migrationGuide = "See migration docs"
)
```

**Parameters:**
- `message` (required): Brief deprecation notice
- `replacement` (optional): Name of the replacement feature/tool
- `deprecatedSince` (optional): Version/date when deprecated
- `removeInVersion` (optional): Version when it will be removed
- `migrationGuide` (optional): Link or reference to migration documentation

### 2. `DeprecationBar` Component

A prominent red bar that appears at the top of a feature card:

```kotlin
@Composable
fun DeprecationBar(
    deprecationInfo: DeprecationInfo,
    modifier: Modifier = Modifier
)
```

**Visual Features:**
- Red error container background
- Warning icon
- Deprecated since / Remove in version chips
- Replacement suggestion box with swap icon
- Migration guide link (if provided)

### 3. `SoftDeprecationBar` Component

A less severe deprecation notice (blue/tertiary color):

```kotlin
@Composable
fun SoftDeprecationBar(
    deprecationInfo: DeprecationInfo,
    modifier: Modifier = Modifier
)
```

Use this for features in maintenance mode or soft-deprecated items.

### 4. `VersionChip` Component

Shows version information badges:

```kotlin
VersionChip(
    version = "2.0.0",
    type = "version" // or "api", "target"
)
```

## Usage in FeatureToggleCard

Add deprecation info to any `FeatureToggleCard`:

```kotlin
FeatureToggleCard(
    title = "Legacy Theme Engine",
    description = "Old theme system using Xposed framework",
    icon = Icons.Default.Palette,
    isEnabled = themingEnabled,
    onToggle = { themingEnabled = it },
    colorIndicator = MaterialTheme.colorScheme.primary,
    deprecationInfo = DeprecationInfo(
        message = "Deprecated - Use new engine instead",
        replacement = "Hexodus Theme Engine v2",
        deprecatedSince = "2025.1",
        removeInVersion = "2026.2",
        migrationGuide = "docs/migration.md"
    )
)
```

## Usage in FeatureDashboardScreen

The `FeatureCard` helper function supports deprecation:

```kotlin
FeatureCard(
    title = "Substratum Overlay Manager",
    description = "Classic overlay management system",
    icon = Icons.Default.Layers,
    isEnabled = systemTunerEnabled,
    onToggle = { systemTunerEnabled = it },
    requirements = listOf("Root"),
    deprecationInfo = DeprecationInfo(
        message = "Deprecated - No longer maintained",
        replacement = "RRO Overlay System",
        deprecatedSince = "2024.3",
        removeInVersion = "2026.1"
    )
)
```

## Visual Design

### Deprecation Bar Layout

```
┌─────────────────────────────────────────┐
│ ⚠️ Deprecated - Use new engine instead  │
│                                         │
│ [Deprecated since: 2025.1] [Remove in: │
│ 2026.2]                                 │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 🔄 Use instead:                     │ │
│ │    Hexodus Theme Engine v2          │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ 📖 Migration Guide: docs/migration.md   │
└─────────────────────────────────────────┘
```

### Feature Card with Deprecation

```
┌─────────────────────────────────────────┐
│ [DEPRECATION BAR - RED]                 │
│                                         │
│  🎨  Legacy Theme Engine        [Toggle]│
│      Old theme system using Xposed      │
└─────────────────────────────────────────┘
```

## Best Practices

### When to Use Deprecation Bars

1. **Hard Deprecation** (Red Bar):
   - Feature will be removed soon
   - Security vulnerabilities
   - Better alternative exists
   - Breaking changes planned

2. **Soft Deprecation** (Blue Bar):
   - Feature in maintenance mode
   - Discouraged but still supported
   - Performance concerns
   - Minor issues

### Deprecation Message Guidelines

✅ **Good:**
- "Deprecated - Use RRO Overlay System"
- "Deprecated since 2025.1 - Remove in 2026.2"
- "No longer maintained - Migrate to Native Implementation"

❌ **Bad:**
- "Old stuff" (too vague)
- "Don't use this" (not helpful)
- "Broken" (unprofessional)

### Version Numbering

Use semantic versioning or year-based versioning:
- `2025.1` (Year.Quarter)
- `2.0.0` (Major.Minor.Patch)
- `v3.1.4` (Explicit version prefix)

## Examples

### Example 1: Simple Deprecation

```kotlin
FeatureCard(
    title = "GravityBox Tweaks",
    description = "System tweaks via GravityBox module",
    icon = Icons.Default.Tune,
    isEnabled = systemTunerEnabled,
    onToggle = { systemTunerEnabled = it },
    requirements = listOf("Xposed"),
    deprecationInfo = DeprecationInfo(
        message = "Deprecated - Use native implementation",
        replacement = "System Tuner (Native)",
        deprecatedSince = "2025.2"
    )
)
```

### Example 2: Full Deprecation with Migration

```kotlin
FeatureCard(
    title = "Legacy Theme Engine",
    description = "Old theme system using Xposed framework",
    icon = Icons.Default.Palette,
    isEnabled = themingEnabled,
    onToggle = { themingEnabled = it },
    requirements = listOf("Root"),
    deprecationInfo = DeprecationInfo(
        message = "Deprecated - Use new engine instead",
        replacement = "Hexodus Theme Engine v2",
        deprecatedSince = "2025.1",
        removeInVersion = "2026.2",
        migrationGuide = "https://hexodus.com/docs/migration-v2"
    )
)
```

### Example 3: Soft Deprecation

```kotlin
// In your custom composable
SoftDeprecationBar(
    deprecationInfo = DeprecationInfo(
        message = "Maintenance mode - Bug fixes only",
        replacement = "New Feature X (Recommended)"
    )
)
```

## Files Modified/Created

1. **New File:** `app/src/main/java/com/hexodus/ui/components/DeprecationBar.kt`
   - Contains `DeprecationInfo`, `DeprecationBar`, `SoftDeprecationBar`, `VersionChip`

2. **Modified:** `app/src/main/java/com/hexodus/ui/components/FeatureToggleCard.kt`
   - Added `deprecationInfo` and `version` parameters
   - Integrated `DeprecationBar` rendering

3. **Modified:** `app/src/main/java/com/hexodus/ui/FeatureDashboardScreen.kt`
   - Added import for `DeprecationInfo`
   - Updated `FeatureCard` to support deprecation
   - Added "Deprecated Tools" section with examples

## Future Enhancements

Potential improvements:

1. **Dismissible Deprecation:** Allow users to dismiss deprecation notices
2. **Analytics Tracking:** Track which deprecated features are still in use
3. **Auto-Migration:** One-click migration to replacement features
4. **Deprecation Timeline:** Visual timeline showing deprecation schedule
5. **Severity Levels:** Different colors for different deprecation severities

## Testing

Test the deprecation bar by:

1. Running the app
2. Scrolling to the "⚠️ Deprecated Tools" section
3. Verifying the red deprecation bars appear
4. Checking that replacement text is visible
5. Confirming version chips display correctly

## Accessibility

The deprecation bar includes:
- High contrast colors (error container)
- Clear warning icon
- Large touch targets
- Screen reader friendly content descriptions
- Proper text scaling
