#!/bin/bash

TYPE=${1:-alpha}

if [ "$TYPE" == "stable" ]; then
    VERSION=$(python3 scripts/version_manager.py)
    TAG="v$VERSION"
    echo "Preparing stable release $TAG..."
    git add version.properties
    git commit -m "chore: release $TAG"
    git tag -a "$TAG" -m "Release $TAG"
    echo "Now run: git push origin main --tags"
else
    VERSION=$(python3 scripts/version_manager.py alpha)
    echo "Bumped to alpha version: $VERSION"
    echo "Commit your changes with 'feat:' or 'fix:' and push to trigger build."
fi
