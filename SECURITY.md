# Security Policy

## Supported Versions

We provide security updates for the following versions of Hexodus:

| Version | Supported          |
| ------- | ------------------ |
| 1.x.x   | ✅ Supported       |

## Reporting a Vulnerability

If you discover a security vulnerability in Hexodus, please report it responsibly using one of the following methods:

### GitHub Private Security Reporting
Use GitHub's private security reporting feature to disclose vulnerabilities:
1. Go to the [Security tab](https://github.com/username/hexodus/security) of the repository
2. Click on "Report a vulnerability"
3. Fill out the vulnerability report form with detailed information

### Email Reporting
Alternatively, you can email us directly at [security-contact@example.com] with the following information:
- A detailed description of the vulnerability
- Steps to reproduce the issue
- Potential impact of the vulnerability
- Any proof-of-concept code (if applicable)

### Response Timeline
- **Acknowledgment**: Within 48 hours of submission
- **Initial Assessment**: Within 1 week of submission
- **Update on Progress**: Every 2 weeks until resolution
- **Resolution**: Within 30 days for critical issues, 90 days for less critical issues

## Security Best Practices

### For Users
- Only install Hexodus from official sources
- Keep your device's operating system updated
- Ensure Shizuku is properly configured and updated
- Review permissions carefully before granting them

### For Developers
- Follow secure coding practices
- Validate all inputs and sanitize data
- Use proper error handling
- Implement appropriate access controls
- Regular security audits of code changes

## Known Security Considerations

### Shizuku Integration
- Hexodus relies on Shizuku for system-level operations
- Ensure Shizuku is installed and properly configured
- Review Shizuku permissions carefully

### Overlay Management
- Hexodus creates and manages system overlays
- These overlays can modify system UI elements
- Only install themes from trusted sources

### System Access
- Hexodus accesses system APIs through Shizuku
- This access is limited to theming and customization operations
- All operations are logged for security auditing

## Security Features

### Input Validation
- All user inputs are validated against security policies
- Path validation to prevent directory traversal attacks
- Command filtering to prevent injection attacks

### Permission Management
- Minimal permission requirements
- Runtime permission requests with clear explanations
- Secure storage for sensitive data

### Secure Communication
- Encrypted storage for sensitive configuration
- Secure communication with system services
- Proper authentication for all operations

## Audit Trail

We maintain logs of security-related events:
- Theme installation attempts
- System API access requests
- Overlay activation/deactivation
- Permission changes

These logs are retained for 90 days and are used for security analysis.

## Dependencies Security

We regularly audit our dependencies for security vulnerabilities:
- AndroidX libraries are kept up-to-date
- Shizuku API is monitored for security updates
- Third-party libraries are evaluated for security posture