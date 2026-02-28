import os
import glob
import re

service_files = glob.glob('app/src/main/java/com/hexodus/services/*.kt')

for file_path in service_files:
    with open(file_path, 'r') as f:
        content = f.read()

    changed = False
    
    # Fix the double HexodusApplication.context
    if "context.HexodusApplication.context" in content:
        content = content.replace("context.HexodusApplication.context", "HexodusApplication.context")
        changed = True

    # Fix unescaped quotes in ShizukuInstaller
    if 'pm install -r "' in content and '""' in content:
        content = content.replace('pm install -r "${apkFile.absolutePath}"', 'pm install -r \"${apkFile.absolutePath}\"')
        changed = True

    # Ensure Capabilities -> DeviceCapabilities in all service files
    if "CapabilityManager.Capabilities" in content:
        content = content.replace("CapabilityManager.Capabilities", "CapabilityManager.DeviceCapabilities")
        changed = True

    if changed:
        with open(file_path, 'w') as f:
            f.write(content)

print("Final fix complete")
