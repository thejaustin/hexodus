# Hexodus AI Assistant Rules

As an AI assistant working on the Hexodus project, you must strictly adhere to the following rules when interacting with Git and GitHub.

## 1. Commit Messages (Conventional Commits)
Whenever you are asked to commit changes or push to GitHub, you **MUST** automatically generate a commit message following the Conventional Commits specification. 

**Format:** `<type>: <description>`

**Types:**
*   `feat:` - A new feature (Triggers a MINOR version bump).
*   `fix:` - A bug fix (Triggers a PATCH version bump).
*   `refactor:` - Code changes that neither fix a bug nor add a feature.
*   `chore:` - Build process, dependency updates, or auxiliary tool changes.

**Do not** ask the user to provide the commit message. Analyze the `git diff` and generate an appropriate, concise, single-line message yourself.

## 2. Versioning & Pushing
We have an automated versioning system in place via GitHub Actions (`.github/workflows/release.yml`) and `scripts/version_manager.py`.

*   **You do not need to manually update `version.properties` or create tags unless explicitly asked to create a `stable` release.**
*   The GitHub Action will automatically calculate the next version (alpha/beta) based on your commit prefix (`feat`, `fix`, etc.) and build the APK.
*   When asked to "push", simply use standard git commands:
    ```bash
    git add .
    git commit -m "fix: resolve crash in MainActivity"
    git push origin main
    ```

## 3. General Development
*   Always use `HexodusApplication.context` for global context needs within Singleton objects to avoid Memory Leaks and `UninitializedPropertyAccessException` errors.
*   Never use `lateinit var` for Android system services in Objects; use `lazy { ... }` initialization instead.
