#!/usr/bin/env bash

set -euo pipefail

# Define paths
SRC_FILE="ffm_kata.c"
OUT_DIR="."
mkdir -p "$OUT_DIR"

# Determine output file name based on OS
OS="$(uname -s)"
case "$OS" in
    Darwin)
        OUT_FILE="$OUT_DIR/libffm_kata.dylib"
        COMPILE_CMD="gcc -shared -fPIC -o $OUT_FILE $SRC_FILE"
        ;;
    Linux)
        OUT_FILE="$OUT_DIR/libffm_kata.so"
        COMPILE_CMD="gcc -shared -fPIC -o $OUT_FILE $SRC_FILE"
        ;;
    MINGW*|MSYS*|CYGWIN*)
        OUT_FILE="$OUT_DIR/ffm_kata.dll"
        COMPILE_CMD="gcc -shared -o $OUT_FILE $SRC_FILE"
        ;;
    *)
        echo "Unsupported OS: $OS"
        exit 1
        ;;
esac

echo "Compiling $SRC_FILE → $OUT_FILE"
eval "$COMPILE_CMD"
echo "✅ Build complete."
