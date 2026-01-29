# Hexodus GitHub Repository Setup Guide

This guide will help you set up the Hexodus repository on GitHub with proper CI/CD using GitHub Actions.

## Step 1: Create GitHub Repository

1. Go to [GitHub.com](https://github.com) and sign in to your account
2. Click the "+" icon in the top-right corner and select "New repository"
3. Fill in the repository details:
   - Repository name: `hexodus`
   - Description: "A next-generation Android theming engine for Samsung One UI 8 (Android 16+) as a spiritual successor to 'Hex Installer', leveraging Shizuku for system-level operations without requiring root access."
   - Public: Yes (or Private if preferred)
   - Initialize with: README (already exists), .gitignore (already exists), and license (already exists)
4. Click "Create repository"

## Step 2: Add Remote Origin and Push Code

After creating the repository on GitHub, you'll need to connect your local repository:

```bash
# Navigate to your hexodus project directory
cd /data/data/com.termux/files/home/hexodus

# Add the remote origin (replace with your actual GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/hexodus.git

# Verify the remote is set correctly
git remote -v

# Push the code to GitHub
git branch -M main
git push -u origin main
```

## Step 3: Configure GitHub Secrets for Release Workflow

For the release workflow to work properly, you'll need to configure GitHub secrets:

1. Go to your repository on GitHub
2. Click on "Settings" tab
3. In the left sidebar, click on "Secrets and variables" → "Actions"
4. Click "New repository secret" and add the following secrets:

### Required Secrets for Release Workflow:
- `SIGNING_KEY`: Base64-encoded keystore file content
- `ALIAS`: Keystore alias
- `KEY_STORE_PASSWORD`: Keystore password
- `KEY_PASSWORD`: Key password

To generate these, you'll need to create a signing key:

```bash
# Generate a signing key (run this on your development machine, not Termux)
keytool -genkey -v -keystore hexodus-release-key.keystore -alias hexodus-key-alias -keyalg RSA -keysize 2048 -validity 10000

# Encode the keystore file to base64
base64 -i hexodus-release-key.keystore | pbcopy  # On macOS
base64 -i hexodus-release-key.keystore           # On Linux, then copy the output
```

## Step 4: GitHub Actions Workflows Explained

The repository includes two main GitHub Actions workflows:

### 1. Build Workflow (`.github/workflows/build.yml`)
- Builds the app on every push and pull request
- Runs unit tests
- Creates debug and release APKs
- Runs instrumentation tests on Android emulators with different API levels

### 2. Release Workflow (`.github/workflows/release.yml`)
- Creates a GitHub release when a tag is pushed
- Builds and signs the release APK
- Uploads the APK as a release asset

## Step 5: Enable Workflows

After pushing the code, you need to enable the workflows:

1. Go to your repository on GitHub
2. Click on "Actions" tab
3. You might see a message asking you to enable workflows
4. Click "I understand my workflows, go ahead and enable them"

## Step 6: Create First Release (Optional)

To test the release workflow:

```bash
# Create and push a tag
git tag -a v1.0.0 -m "First release of Hexodus"
git push origin v1.0.0
```

This will trigger the release workflow which will build a signed APK and create a GitHub release.

## Step 7: Verify Setup

After completing the setup:

1. Check that all files have been pushed to GitHub
2. Verify that the build workflow runs successfully
3. Confirm that the README.md displays properly on the repository homepage
4. Check that all documentation files are accessible

## Additional Configuration Options

### Branch Protection Rules
Consider setting up branch protection rules for the `main` branch:
- Require pull request reviews
- Require status checks to pass
- Require branches to be up to date before merging

### Issue Templates
The repository includes issue templates in `.github/ISSUE_TEMPLATE/` that will automatically appear when users create new issues.

### Code Review Settings
You can set up code review requirements in the repository settings to ensure quality control.

## Troubleshooting

### Workflow Not Running
- Ensure Actions are enabled in repository settings
- Check that the workflow files are in the correct location (`.github/workflows/`)
- Verify workflow syntax using the GitHub Actions editor

### Build Failures
- Check that the Android SDK and build tools versions in the workflow match the project requirements
- Verify that the gradle wrapper is properly configured
- Ensure all dependencies are correctly specified

### Signing Issues
- Verify that all required secrets are properly configured
- Check that the keystore file is properly encoded as base64
- Confirm that the alias and passwords match the keystore

## Next Steps

Once your GitHub repository is set up:

1. Update the README.md with your actual repository URL
2. Add collaborators if needed
3. Set up issue labels for better organization
4. Consider creating milestones for project planning
5. Update the documentation with your specific deployment instructions

Your Hexodus repository is now ready for collaborative development with full CI/CD capabilities!