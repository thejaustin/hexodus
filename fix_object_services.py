import os
import glob
import re

service_files = glob.glob('app/src/main/java/com/hexodus/services/*.kt')

for file_path in service_files:
    with open(file_path, 'r') as f:
        content = f.read()

    if "object " in content:
        changed = False
        
        # Remove methods that only belong to Service
        # override fun onCreate() { ... }
        # override fun onDestroy() { ... }
        content = re.sub(r'override fun onCreate\(\)\s*\{[^{}]*(?:{[^{}]*}[^{}]*)*\}', '', content)
        content = re.sub(r'override fun onDestroy\(\)\s*\{[^{}]*(?:{[^{}]*}[^{}]*)*\}', '', content)
        
        # Fix startForeground / stopForeground calls
        # Objects cannot be foreground services. 
        # We should just remove these calls as they are no longer services.
        if "startForeground(" in content:
            content = re.sub(r'startForeground\([^)]*\)', '// startForeground not supported in object', content)
            changed = True
        if "stopForeground(" in content:
            content = re.sub(r'stopForeground\([^)]*\)', '// stopForeground not supported in object', content)
            changed = True
            
        # Fix getSystemService calls
        if "getSystemService(" in content and "HexodusApplication.context.getSystemService" not in content:
            content = content.replace("getSystemService(", "HexodusApplication.context.getSystemService(")
            changed = True
            
        # Fix 'this' as Context
        # In many places 'this' was used as Context. Replace with HexodusApplication.context
        # But only where it's likely a context.
        # Simple heuristic: Intent(this, ...), PendingIntent.getActivity(this, ...), NotificationCompat.Builder(this, ...)
        content = content.replace("Intent(this,", "Intent(HexodusApplication.context,")
        content = content.replace("PendingIntent.getActivity(this,", "PendingIntent.getActivity(HexodusApplication.context,")
        content = content.replace("NotificationCompat.Builder(this,", "NotificationCompat.Builder(HexodusApplication.context,")
        content = content.replace("packageName", "HexodusApplication.context.packageName")
        
        if changed:
            with open(file_path, 'w') as f:
                f.write(content)

print("Done")
