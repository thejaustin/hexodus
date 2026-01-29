# GitHub Push Instructions for Hexodus

Follow these steps to push the Hexodus project to GitHub and enable automatic building:

## Step 1: Create GitHub Repository
1. Go to [GitHub.com](https://github.com)
2. Click the "+" icon in the top-right corner and select "New repository"
3. Name your repository (e.g., "hexodus")
4. Add description: "Next-generation Android theming engine for Samsung One UI 8 (Android 16+) as a spiritual successor to 'Hex Installer', leveraging Shizuku for system-level operations without requiring root access."
5. Select "Public" (or "Private" if preferred)
6. Do NOT initialize with README, .gitignore, or license (we already have these)
7. Click "Create repository"

## Step 2: Push Code to GitHub
In your terminal, run these commands from the hexodus project directory:

```bash
# Set your GitHub repository URL
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY_NAME.git

# Verify the remote is set correctly
git remote -v

# Push the code to GitHub
git branch -M main
git push -u origin main
```

## Step 3: Enable GitHub Actions
After pushing the code:

1. Go to your repository on GitHub
2. Click on the "Actions" tab
3. You may see a message asking you to enable workflows
4. Click "I understand my workflows, go ahead and enable them"
5. GitHub Actions will automatically start building your project

## Step 4: Verify Build Process
1. Go to the "Actions" tab in your repository
2. You should see a workflow running titled "Build and Test Hexodus"
3. The workflow will:
   - Set up JDK 17 and Android SDK
   - Build debug and release APKs
   - Run unit tests
   - Test on multiple Android API levels
   - Upload APK artifacts as build outputs

## Step 5: Check Build Artifacts
1. Once the build completes successfully, go back to the main repository page
2. In the workflow run, you'll find "Artifacts" containing:
   - hexodus-debug-apk: Debug version of the app
   - hexodus-release-apk: Release version of the app
3. Download the APK files and install on your device with Shizuku installed

## Step 6: Create a Release (Optional)
To create a formal release with a signed APK:

```bash
# Create and push a tag
git tag -a v1.0.0 -m "Initial release of Hexodus"
git push origin v1.0.0
```

This will trigger the release workflow which creates a GitHub release with the signed APK.

## Important Notes:
- Make sure your device has Shizuku installed and properly configured
- The app requires special permissions that need to be granted after installation
- For the theming features to work, Shizuku must be running with proper permissions
- The build process may take 5-10 minutes as it needs to download Android SDK components
- If builds fail, check the Actions logs for specific error messages

## Troubleshooting:
- If the build fails due to missing Android SDK, make sure the GitHub Actions workflow is properly configured
- Check that all files were pushed correctly with `git push --all`
- Verify that GitHub Actions are enabled in your repository settings

Your Hexodus repository is now ready for collaborative development with full CI/CD capabilities!