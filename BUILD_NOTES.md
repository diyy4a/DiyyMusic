# Build notes

The source is configured for Java 21 and Android SDK 37.

## GitHub Actions

Run the workflow **Build DiyyMusic v0.7.0 APK** from the Actions tab. The final APK is uploaded as the `DiyyMusic-v0.7.0-APK` artifact.

## Local build

```bash
chmod +x gradlew
./gradlew :app:assembleFossDebug
```

The compact build task keeps the APK at:

```text
app/build/outputs/apk/foss/debug/DiyyMusic-v0.7.0.apk
```
