import os
import glob
import re

service_files = glob.glob('app/src/main/java/com/hexodus/services/*.kt')

# List of common context properties that need qualification
properties = ['cacheDir', 'filesDir', 'packageName', 'contentResolver', 'packageManager', 'applicationContext']

for file_path in service_files:
    with open(file_path, 'r') as f:
        content = f.read()

    if "object " in content:
        changed = False
        
        for prop in properties:
            # Matches prop but not when preceded by . or part of a longer word
            # Avoid qualifying it if it's already qualified or if it's a parameter (though parameter usually has different name)
            # Use negative lookbehind (?<!\.) and negative lookahead
            pattern = r'(?<![\w\.])' + prop + r'(?!\w)'
            replacement = 'HexodusApplication.context.' + prop
            
            if re.search(pattern, content):
                content = re.sub(pattern, replacement, content)
                changed = True

        if changed:
            # Cleanup double qualification if it happened (e.g. HexodusApplication.context.HexodusApplication.context)
            content = content.replace('HexodusApplication.context.HexodusApplication.context', 'HexodusApplication.context')
            with open(file_path, 'w') as f:
                f.write(content)

print("Qualification fix complete")
