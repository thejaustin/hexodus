import os
import glob
import re

service_files = glob.glob('app/src/main/java/com/hexodus/services/*.kt')

for file_path in service_files:
    with open(file_path, 'r') as f:
        content = f.read()

    # If it's one of our refactored objects
    if "object " in content and ("onStartCommand" in content or "sendBroadcast" in content):
        changed = False
        
        # Add Service import for START_STICKY if not present
        if "START_STICKY" in content and "import android.app.Service" not in content:
            content = re.sub(r'(package com\.hexodus\.services)', r'\1\n\nimport android.app.Service', content)
            changed = True
            
        # Fix START_STICKY reference
        if "return START_STICKY" in content:
            content = content.replace("return START_STICKY", "return Service.START_STICKY")
            changed = True

        # Fix sendBroadcast
        if "sendBroadcast(" in content and "HexodusApplication.context.sendBroadcast" not in content:
            if "import com.hexodus.HexodusApplication" not in content:
                content = re.sub(r'(package com\.hexodus\.services)', r'\1\nimport com.hexodus.HexodusApplication', content)
            content = content.replace("sendBroadcast(", "HexodusApplication.context.sendBroadcast(")
            changed = True

        if changed:
            with open(file_path, 'w') as f:
                f.write(content)

print("Done fixing services")
