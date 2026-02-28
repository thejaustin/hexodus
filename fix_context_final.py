import os
import glob
import re

service_files = glob.glob('app/src/main/java/com/hexodus/services/*.kt')

# 1. Ensure each object has 'private val appContext get() = HexodusApplication.context'
# 2. Replace all standalone 'context' with 'appContext' in the logic
# 3. Fix the 'listOf(...).filterNotNull()' for SecurityUtils calls

for file_path in service_files:
    with open(file_path, 'r') as f:
        content = f.read()

    if "object " in content:
        # Normalize the header
        content = re.sub(r'private val appContext: android\.content\.Context get\(\) = com\.hexodus\.HexodusApplication\.context', 'private val appContext get() = com.hexodus.HexodusApplication.context', content)
        content = re.sub(r'private val context: android\.content\.Context get\(\) = com\.hexodus\.HexodusApplication\.context', 'private val appContext get() = com.hexodus.HexodusApplication.context', content)
        
        # Replace standalone 'context' with 'appContext'
        # But NOT when it's part of a longer word or property
        # Use word boundaries \b
        content = re.sub(r'\bcontext\b', 'appContext', content)
        
        # Cleanup double qualification if it happened
        content = content.replace('appContext.HexodusApplication.appContext', 'com.hexodus.HexodusApplication.context')
        content = content.replace('com.hexodus.HexodusApplication.appContext', 'com.hexodus.HexodusApplication.context')
        
        # Fix the SecurityUtils.isValidFilePath calls
        # Find listOf(...) inside SecurityUtils.isValidFilePath
        content = re.sub(r'(SecurityUtils\.isValidFilePath\([^,]+,\s*listOf\([^)]+\))', r'\1.filterNotNull()', content)
        # Cleanup if I double added .filterNotNull()
        content = content.replace('.filterNotNull().filterNotNull()', '.filterNotNull()')

        with open(file_path, 'w') as f:
            f.write(content)

print("Final context fix complete")
