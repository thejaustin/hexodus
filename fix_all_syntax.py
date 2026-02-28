import os
import glob
import re

service_files = glob.glob('app/src/main/java/com/hexodus/services/*.kt')

# Define the clean template
template = """
    private val context: android.content.Context get() = com.hexodus.HexodusApplication.context
    private val packageName: String get() = context.packageName
    private val cacheDir: java.io.File get() = context.cacheDir
    private val filesDir: java.io.File get() = context.filesDir
    private val contentResolver: android.content.ContentResolver get() = context.contentResolver
    private val packageManager: android.content.pm.PackageManager get() = context.packageManager
    private val applicationContext: android.content.Context get() = context
    private val resources: android.content.res.Resources get() = context.resources
"""

def clean_content(content):
    # 1. Remove all existing context-related properties to start fresh
    props_to_remove = [
        r'private val context: android\.content\.Context get\(\) = [^ \n]+',
        r'private val packageName: String get\(\) = [^ \n]+',
        r'private val cacheDir: java\.io\.File get\(\) = [^ \n]+',
        r'private val filesDir: java\.io\.File get\(\) = [^ \n]+',
        r'private val contentResolver: android\.content\.ContentResolver get\(\) = [^ \n]+',
        r'private val packageManager: android\.content\.pm\.PackageManager get\(\) = [^ \n]+',
        r'private val applicationContext: android\.content\.Context get\(\) = [^ \n]+',
        r'private val resources: android\.content\.res\.Resources get\(\) = [^ \n]+'
    ]
    for prop in props_to_remove:
        content = re.sub(prop, '', content)
    
    # 2. Fix the double/triple qualifications
    content = content.replace("android.app.Service.android.app.Service.START_STICKY", "android.app.Service.START_STICKY")
    content = content.replace("context.HexodusApplication.context", "com.hexodus.HexodusApplication.context")
    content = content.replace("HexodusApplication.context.HexodusApplication.context", "com.hexodus.HexodusApplication.context")
    
    # 3. Insert the template after the object declaration
    match = re.search(r'object\s+\w+\s*\{', content)
    if match:
        content = content[:match.end()] + template + content[match.end():]
    
    # 4. Fix common unresolved references by pointing them to our new properties
    # Ensure they aren't already qualified
    content = re.sub(r'(?<![\w\.])resources\.', 'resources.', content) # This doesn't change anything, just for thought
    
    return content

for file_path in service_files:
    if "ShizukuBridge" in file_path: continue
    
    with open(file_path, 'r') as f:
        content = f.read()
    
    new_content = clean_content(content)
    
    if new_content != content:
        with open(file_path, 'w') as f:
            f.write(new_content)

print("Systematic syntax fix complete")
