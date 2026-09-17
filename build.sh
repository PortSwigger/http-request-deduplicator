#!/bin/bash

# UniReq Build Script
# Builds the Burp Suite extension JAR file using Maven

set -e  # Exit on any error

echo "🚀 Building UniReq - HTTP Request Deduplicator Extension"
echo "=============================================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven is not installed or not in PATH"
    echo "Please install Maven 3.6+ and try again"
    echo "Visit: https://maven.apache.org/install.html"
    exit 1
fi

# Check Java version
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed or not in PATH"
    echo "Please install JDK 17+ and try again"
    exit 1
fi

if ! command -v javac &> /dev/null; then
    echo "❌ Error: A Java compiler was not found in PATH"
    echo "Please install JDK 17+ and set JAVA_HOME to that JDK"
    exit 1
fi

# Extract Java version more reliably
JAVA_VERSION_OUTPUT=$(java -version 2>&1 | head -1)
if [[ $JAVA_VERSION_OUTPUT =~ \"([0-9]+)\.([0-9]+)\.([0-9]+) ]]; then
    JAVA_MAJOR=${BASH_REMATCH[1]}
    JAVA_MINOR=${BASH_REMATCH[2]}
elif [[ $JAVA_VERSION_OUTPUT =~ \"([0-9]+) ]]; then
    JAVA_MAJOR=${BASH_REMATCH[1]}
    JAVA_MINOR=0
else
    echo "❌ Error: Could not determine the installed Java version"
    exit 1
fi

if [ "$JAVA_MAJOR" -lt 17 ]; then
    echo "❌ Error: Java 17 or higher is required (found Java $JAVA_MAJOR)"
    echo "Please upgrade your Java installation"
    exit 1
fi

echo "✅ Java $JAVA_MAJOR detected"

# Verify that the compiler, not only the runtime, supports Java 17
JAVAC_VERSION_OUTPUT=$(javac -version 2>&1)
if [[ $JAVAC_VERSION_OUTPUT =~ ([0-9]+)\.([0-9]+) ]]; then
    JAVAC_MAJOR=${BASH_REMATCH[1]}
elif [[ $JAVAC_VERSION_OUTPUT =~ ([0-9]+) ]]; then
    JAVAC_MAJOR=${BASH_REMATCH[1]}
else
    echo "❌ Error: Could not determine the installed Java compiler version"
    exit 1
fi

if [ "$JAVAC_MAJOR" -lt 17 ]; then
    echo "❌ Error: JDK 17 or higher is required (found javac $JAVAC_MAJOR)"
    echo "Please set JAVA_HOME and PATH to a JDK 17+ installation"
    exit 1
fi

echo "✅ javac $JAVAC_MAJOR detected"

# Display Maven version
MVN_VERSION=$(mvn -version | head -1)
echo "✅ $MVN_VERSION"

echo ""
echo "🔧 Cleaning previous builds..."
mvn clean -q

echo "📦 Compiling, testing, and packaging extension..."
mvn verify -q

# Check if build was successful
JAR_PATH=$(find target -maxdepth 1 -type f -name 'unireq-deduplicator-*.jar' ! -name 'original-*' -print -quit)
if [ -n "$JAR_PATH" ]; then
    echo ""
    echo "🎉 Build completed successfully!"
    echo "📍 Extension JAR location: $JAR_PATH"
    echo ""
    echo "📋 Next steps:"
    echo "1. Open Burp Suite"
    echo "2. Go to Extensions → Installed"
    echo "3. Click 'Add' and select the JAR file"
    echo "4. Look for the 'UniReq' tab in Burp's interface"
    echo ""
    echo "📊 File size: $(ls -lh "$JAR_PATH" | awk '{print $5}')"
else
    echo "❌ Build failed - JAR file not found"
    exit 1
fi
