#!/bin/bash
# Generate Kotlin protobuf files for Android.

set -e

PROTO_DIR="../metroproto"
OUT_DIR="src/main/java"
SOURCE_PROTO="$PROTO_DIR/listentogether.proto"

if [ ! -f "$SOURCE_PROTO" ]; then
    echo "Missing proto file at $SOURCE_PROTO"
    exit 1
fi

mkdir -p "$OUT_DIR"
protoc --java_out=lite:"$OUT_DIR" --kotlin_out="$OUT_DIR" \
    -I="$PROTO_DIR" \
    "$SOURCE_PROTO"

echo "Protobuf files generated successfully in $OUT_DIR"
