import os
import subprocess
import sys
import json
import http.client

def get_staged_diff():
    try:
        return subprocess.check_output(['git', 'diff', '--cached'], stderr=subprocess.STDOUT).decode('utf-8')
    except subprocess.CalledProcessError:
        return ""

def get_staged_files():
    try:
        output = subprocess.check_output(['git', 'diff', '--cached', '--name-only']).decode('utf-8')
        return output.strip().split('\n') if output.strip() else []
    except:
        return []

def generate_heuristic_message(files, diff):
    if not files:
        return "chore: update repository"
    
    # Analyze files to determine prefix
    is_fix = "fix" in diff.lower() or "error" in diff.lower() or "bug" in diff.lower()
    is_feat = len(files) > 2 or any("Screen" in f for f in files)
    is_refactor = any("Service" in f or "Manager" in f for f in files)
    is_build = any(f.endswith('.gradle') or f.endswith('.properties') for f in files)
    
    prefix = "chore"
    if is_fix: prefix = "fix"
    elif is_feat: prefix = "feat"
    elif is_refactor: prefix = "refactor"
    elif is_build: prefix = "chore"
    
    # Try to find a meaningful subject
    main_file = files[0].split('/')[-1]
    subject = f"update {main_file}"
    
    if "Shizuku" in diff:
        subject = "improve Shizuku integration"
    elif any("ui" in f for f in files):
        subject = "enhance UI components"
    elif any("services" in f for f in files):
        subject = "optimize background services"
        
    return f"{prefix}: {subject}"

def generate_ai_message(diff):
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        return None
    
    prompt = f"Generate a single-line Conventional Commit message (max 70 chars) for this diff:\n\n{diff[:4000]}"
    
    try:
        conn = http.client.HTTPSConnection("generativelanguage.googleapis.com")
        payload = json.dumps({
            "contents": [{
                "parts": [{"text": prompt}]
            }],
            "generationConfig": {
                "temperature": 0.1,
                "topK": 1,
                "topP": 1,
                "maxOutputTokens": 100,
            }
        })
        headers = {'Content-Type': 'application/json'}
        conn.request("POST", f"/v1beta/models/gemini-1.5-flash:generateContent?key={api_key}", payload, headers)
        res = conn.getresponse()
        data = json.loads(res.read().decode("utf-8"))
        
        text = data['candidates'][0]['content']['parts'][0]['text'].strip()
        # Clean up any quotes or markdown
        text = text.replace('`', '').replace('"', '').replace("'", "").split('\n')[0]
        return text
    except Exception as e:
        print(f"AI Error: {e}", file=sys.stderr)
        return None

if __name__ == "__main__":
    diff = get_staged_diff()
    files = get_staged_files()
    
    if not diff:
        print("No changes staged. Use 'git add' first.")
        sys.exit(1)
        
    message = generate_ai_message(diff)
    if not message:
        message = generate_heuristic_message(files, diff)
        
    print(message)
