# Build notes

The source is configured for Java 21 and Android SDK 37.

## GitHub Actions

Run the workflow **Build DiyyMusic v0.6.1 APK** from the Actions tab. The final APK is uploaded as the `DiyyMusic-v0.6.1-APK` artifact.

## Local build

```bash
chmod +x gradlew
./gradlew :app:assembleFossDebug
```

The APK will be kept in:

```text
app/build/outputs/apk/foss/debug/DiyyMusic-v0.6.1.apk
```
