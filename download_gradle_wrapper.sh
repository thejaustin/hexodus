#!/bin/bash

# Script to download the Gradle wrapper JAR file
# This file is required for the Gradle wrapper to function

WRAPPER_DIR="gradle/wrapper"
WRAPPER_JAR_PATH="$WRAPPER_DIR/gradle-wrapper.jar"
WRAPPER_PROPERTIES_PATH="$WRAPPER_DIR/gradle-wrapper.properties"

echo "Downloading Gradle wrapper JAR..."

# Create the wrapper directory if it doesn't exist
mkdir -p "$WRAPPER_DIR"

# Download the Gradle wrapper JAR based on the version in gradle-wrapper.properties
if [ -f "$WRAPPER_PROPERTIES_PATH" ]; then
    GRADLE_VERSION=$(grep "distributionUrl" "$WRAPPER_PROPERTIES_PATH" | sed -n 's/.*gradle-\([^-]*\)-bin\.zip.*/\1/p')
    echo "Detected Gradle version: $GRADLE_VERSION"
    
    # Download the specific version of Gradle wrapper
    curl -L https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-wrapper.jar -o "$WRAPPER_JAR_PATH"
    
    if [ $? -eq 0 ]; then
        echo "Successfully downloaded Gradle wrapper JAR to $WRAPPER_JAR_PATH"
        echo "File size: $(ls -lh "$WRAPPER_JAR_PATH" | awk '{print $5}')"
    else
        echo "Failed to download Gradle wrapper JAR"
        exit 1
    fi
else
    echo "gradle-wrapper.properties not found!"
    exit 1
fi