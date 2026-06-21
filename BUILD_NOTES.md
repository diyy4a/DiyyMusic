# Build notes

DiyyMusic 0.8.1 uses Java 21, Android SDK 37, and the FOSS debug variant.

## Codemagic

The workflow builds:

```text
DiyyMusic-v0.8.1-universal.apk
```

The persistent debug keystore is reused when valid and recreated only when missing or invalid.

## GitHub Actions

Run **Build DiyyMusic v0.8.1 APK** and download the `DiyyMusic-v0.8.1-APK` artifact.

## Local

```bash
chmod +x gradlew
./gradlew :app:assembleFossDebug
```

The final APK is under `app/build/outputs/apk/foss/debug/`.

A full local Gradle build was not completed in the patching environment because the Gradle distribution and Android dependencies were unavailable offline. Codemagic/GitHub Actions remains the authoritative compile check.
