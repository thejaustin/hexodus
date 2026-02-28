#!/bin/bash

# stage all changes if requested
if [ "$1" == "-a" ]; then
    git add .
fi

# Check for staged changes
if git diff --cached --quiet; then
    echo "No staged changes found. Use 'git add' or run './commit.sh -a'"
    exit 1
fi

echo "Analyzing changes with AI..."
MESSAGE=$(python3 scripts/ai_commit.py)

if [ $? -ne 0 ]; then
    echo "Error generating commit message."
    exit 1
fi

echo "------------------------------------------------"
echo "Suggested title: $MESSAGE"
echo "------------------------------------------------"

read -p "Use this title? (y/n/e to edit): " choice

case "$choice" in
  y|Y )
    git commit -m "$MESSAGE"
    echo "Committed successfully."
    ;;
  e|E )
    read -p "Enter new title: " NEW_MESSAGE
    git commit -m "$NEW_MESSAGE"
    echo "Committed with custom title."
    ;;
  * )
    echo "Commit cancelled."
    ;;
esac
