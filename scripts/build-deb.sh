#!/usr/bin/env bash

set -euo pipefail

APP_NAME="Linuvera"
APP_VERSION="0.1.1"
MAIN_JAR="linuvera-desktop-0.1.0-SNAPSHOT.jar"
MAIN_CLASS="io.github.iroshperera.linuvera.LinuveraApplication"

echo "Building application JAR..."

mvn clean package

echo "Preparing packaging files..."

rm -rf target/packaging
rm -rf target/javafx-modules
rm -rf target/dist

mkdir -p target/packaging
mkdir -p target/javafx-modules
mkdir -p target/dist

cp "target/$MAIN_JAR" \
   target/packaging/

mvn dependency:copy-dependencies \
    -DincludeScope=runtime \
    -DoutputDirectory=target/packaging

cp target/packaging/javafx-*-linux.jar \
   target/javafx-modules/

echo "Building Debian package..."

jpackage \
    --type deb \
    --name "$APP_NAME" \
    --app-version "$APP_VERSION" \
    --vendor "Irosh Perera" \
    --description "Open-source Linux developer and system support toolkit" \
    --input target/packaging \
    --main-jar "$MAIN_JAR" \
    --main-class "$MAIN_CLASS" \
    --module-path target/javafx-modules \
    --add-modules javafx.controls,javafx.graphics,javafx.base \
    --dest target/dist \
    --linux-shortcut \
    --linux-menu-group Development

echo
echo "Package created successfully:"
find target/dist -maxdepth 1 -type f -printf '%f\n'
