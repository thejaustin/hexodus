import os
import glob
import re

service_files = glob.glob('app/src/main/java/com/hexodus/services/*.kt')

for file_path in service_files:
    with open(file_path, 'r') as f:
        content = f.read()

    # 1. Replace filterNotNull() with filter { it != null } to avoid possible extension issues
    content = content.replace('.filterNotNull()', '.filter { it != null }')
    
    # 2. Fix the recursive type check in ShizukuInstaller
    # (Already fixed manually, but let's be sure)
    
    # 3. Ensure no manager has 'private val context' or 'private val appContext'
    # We want them to use HexodusApplication.context directly to be 100% safe.
    
    if "object " in content:
        content = re.sub(r'private val appContext[:\w\. ]+get\(\) = [^\n]+', '', content)
        content = content.replace('appContext.', 'HexodusApplication.context.')
        content = content.replace('appContext,', 'HexodusApplication.context,')
        content = content.replace('appContext)', 'HexodusApplication.context)')

    with open(file_path, 'w') as f:
        f.write(content)

print("Surgical fix complete")
