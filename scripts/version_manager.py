import re
import sys
import subprocess
from datetime import datetime

def get_current_version():
    props = {}
    with open('version.properties', 'r') as f:
        for line in f:
            if '=' in line:
                key, val = line.strip().split('=')
                props[key] = val
    return props

def get_commits_since_last_tag():
    try:
        # Get the last tag
        tag = subprocess.check_output(['git', 'describe', '--tags', '--abbrev=0']).strip().decode('utf-8')
        # Get commits since tag
        commits = subprocess.check_output(['git', 'log', f'{tag}..HEAD', '--pretty=format:%s']).strip().decode('utf-8')
        return commits.split('
') if commits else []
    except:
        # No tags yet, get all commits
        commits = subprocess.check_output(['git', 'log', '--pretty=format:%s']).strip().decode('utf-8')
        return commits.split('
')

def calculate_bump(commits):
    major = False
    minor = False
    patch = False
    
    for msg in commits:
        prefix = msg.split(':')[0].lower()
        if '!' in prefix or 'breaking change' in msg.lower():
            major = True
        elif prefix.startswith('feat'):
            minor = True
        elif prefix.startswith('fix') or prefix.startswith('refactor') or prefix.startswith('chore'):
            patch = True
            
    if major: return 'major'
    if minor: return 'minor'
    return 'patch'

def bump_version(props, bump_type, pre_release=None):
    major = int(props['version.major'])
    minor = int(props['version.minor'])
    patch = int(props['version.patch'])
    code = int(props['version.code'])
    
    if bump_type == 'major':
        major += 1
        minor = 0
        patch = 0
    elif bump_type == 'minor':
        minor += 1
        patch = 0
    else:
        patch += 1
        
    version_name = f"{major}.{minor}.{patch}"
    if pre_release:
        # Add timestamp or increment for alpha
        if 'alpha' in pre_release:
            timestamp = datetime.now().strftime("%Y%m%d%H%M")
            version_name += f"-alpha.{timestamp}"
        else:
            version_name += f"-{pre_release}"
        
    props['version.major'] = str(major)
    props['version.minor'] = str(minor)
    props['version.patch'] = str(patch)
    props['version.name'] = version_name
    props['version.code'] = str(code + 1)
    return props

def save_version(props):
    with open('version.properties', 'w') as f:
        f.write("# Version properties for Hexodus
")
        # Ensure order
        keys = ['version.major', 'version.minor', 'version.patch', 'version.name', 'version.code']
        for key in keys:
            if key in props:
                f.write(f"{key}={props[key]}
")

if __name__ == "__main__":
    # Usage: python3 version_manager.py [alpha|beta]
    pre = sys.argv[1] if len(sys.argv) > 1 else None
    props = get_current_version()
    commits = get_commits_since_last_tag()
    bump = calculate_bump(commits)
    new_props = bump_version(props, bump, pre)
    save_version(new_props)
    print(new_props['version.name'])
