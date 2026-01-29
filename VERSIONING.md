# Hexodus Versioning System

## Version Numbering Convention

Hexodus follows Semantic Versioning (SemVer) with the format `MAJOR.MINOR.PATCH`:

- **MAJOR** version: Incremented for incompatible API changes or major feature additions
- **MINOR** version: Incremented for backward-compatible feature additions
- **PATCH** version: Incremented for backward-compatible bug fixes

### Version Code Format

The version code follows the format `MMmmddd` (Major.Minor.Patch):
- `MM` = Major version (2 digits)
- `mm` = Minor version (2 digits)  
- `ddd` = Patch version (3 digits)

Example: Version 1.2.15 has version code 102015

## Release Types

### Alpha Releases
- Internal testing
- Experimental features
- May contain bugs
- Version format: `vX.Y.Z-alpha.N`

### Beta Releases
- Public testing
- Feature-complete but may have bugs
- Feedback encouraged
- Version format: `vX.Y.Z-beta.N`

### Release Candidates (RC)
- Near-final versions
- Ready for production if no critical issues found
- Version format: `vX.Y.Z-rc.N`

### Production Releases
- Stable, ready for general use
- Version format: `vX.Y.Z`

## Release Process

### Pre-release Checklist
- [ ] All planned features implemented and tested
- [ ] All known bugs fixed
- [ ] Code reviewed and approved
- [ ] Automated tests passing
- [ ] Manual testing completed
- [ ] Documentation updated
- [ ] Release notes prepared

### Release Steps
1. Update version numbers in `app/build.gradle`
2. Update `version.properties` file
3. Create release branch from `main`
4. Prepare release notes
5. Create Git tag with version number
6. Build release APK
7. Test on multiple devices
8. Publish release to GitHub
9. Update documentation if needed

## Version History

### v1.0.0 - 2026-01-29
- Initial release
- Core theming engine with hex color support
- Material You override for Samsung One UI 8
- High contrast theme injection
- Foldable device support for Z Flip 5
- Shizuku integration for system-level operations

## Branch Strategy

- `main`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: Individual feature branches
- `release/*`: Release preparation branches
- `hotfix/*`: Urgent bug fixes for production

## Dependency Management

- Dependencies are updated regularly in minor releases
- Critical security patches may trigger patch releases
- Breaking dependency changes require major version increments

## API Stability

- Public APIs are stable within major versions
- Deprecated APIs remain functional for at least one major version
- Breaking changes are clearly documented in release notes