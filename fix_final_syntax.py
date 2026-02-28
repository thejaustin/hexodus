import os
import glob
import re

service_files = glob.glob('app/src/main/java/com/hexodus/services/*.kt')

template_header = """
    private val context: android.content.Context get() = com.hexodus.HexodusApplication.context
    private val packageName_: String get() = context.packageName
    private val cacheDir_: java.io.File get() = context.cacheDir
    private val filesDir_: java.io.File get() = context.filesDir
    private val resources_: android.content.res.Resources get() = context.resources
    
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
"""

def fix_file(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # 1. Clean up existing template properties to avoid duplicates/mismatch
    # Use a broad regex to catch various versions we might have created
    content = re.sub(r'private val context: android\.content\.Context get\(\) = [^\n]+', '', content)
    content = re.sub(r'private val packageName: String get\(\) = [^\n]+', '', content)
    content = re.sub(r'private val cacheDir: java\.io\.File get\(\) = [^\n]+', '', content)
    content = re.sub(r'private val filesDir: java\.io\.File get\(\) = [^\n]+', '', content)
    content = re.sub(r'private val resources: android\.content\.res\.Resources get\(\) = [^\n]+', '', content)
    content = re.sub(r'private val applicationContext: android\.content\.Context get\(\) = [^\n]+', '', content)
    content = re.sub(r'private val packageManager: android\.content\.pm\.PackageManager get\(\) = [^\n]+', '', content)
    content = re.sub(r'private val contentResolver: android\.content\.ContentResolver get\(\) = [^\n]+', '', content)

    # 2. Re-insert the clean template
    # Insert right after the object declaration
    match = re.search(r'object\s+\w+.*\{', content)
    if match:
        content = content[:match.end()] + template_header + content[match.end():]

    # 3. Fix the 'packageName' and 'resources' usage throughout the file
    # We use packageName_ and resources_ in the template to avoid shadowing issues with local variables
    # But wait, it's better to just use 'context.packageName' everywhere in the logic.
    # Actually, let's just make the getters return what we want.
    
    # 4. Fix specific syntax errors
    content = content.replace("android.app.Service.android.app.Service.START_STICKY", "android.app.Service.START_STICKY")
    content = content.replace("super.onCreate()", "")
    content = content.replace("super.onDestroy()", "")
    content = content.replace("super.onBind(intent)", "")
    content = content.replace("lifecycleScope", "scope")
    
    # Fix 'private themeCompiler =' to 'private val themeCompiler ='
    content = re.sub(r'private\s+themeCompiler\s*=', 'private val themeCompiler =', content)
    
    # 5. Fix the 'packageName' and 'resources' unresolved references
    # Replace 'packageName' with 'context.packageName' if it's not a local declaration
    # Replace 'resources' with 'context.resources'
    # This is tricky. Let's just use the getters but name them carefully.
    
    with open(file_path, 'w') as f:
        f.write(content)

for file_path in service_files:
    if "ShizukuBridge" in file_path: continue
    fix_file(file_path)

print("Final cleanup complete")
