import os
import glob
import re

service_files = glob.glob('app/src/main/java/com/hexodus/services/*.kt')

for file_path in service_files:
    with open(file_path, 'r') as f:
        content = f.read()

    changed = False

    # Fix ShizukuPlusAPI.Shell.executeCommand().output -> ShizukuPlusAPI.Shell.executeCommand().output
    # Wait, the data class has val output: String. So it SHOULD work.
    # Ah, let's look at FeatureFlagsService again.
    
    # Check if we are assigning Unit to Boolean
    # In ShizukuInstaller.kt:
    # try { ShizukuPlusAPI.PackageManager.installPackage(apkFile.absolutePath); true }
    
    if "ShizukuPlusAPI.PackageManager.installPackage" in content and "true" not in content:
        # Example: val success: Boolean = if (...) { ShizukuPlusAPI.PackageManager.installPackage(...); true }
        # Let's check ShizukuInstaller.kt specifically.
        pass

    if changed:
        with open(file_path, 'w') as f:
            f.write(content)

print("Check complete")
