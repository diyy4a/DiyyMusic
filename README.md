# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.8.7** (`versionCode 20`)

## What changed in 0.8.7

- Replaced the large Theme Mode button strip with a compact dropdown setting.
- Rebuilt Streaming Quality with the same dropdown pattern for a cleaner, consistent settings layout.
- Simplified the Now Playing screen so it is calmer and easier to read.
- Removed the breathing artwork effect, oversized glass controls, and decorative play-button gradient.
- Moved shuffle and repeat into the main playback controls.
- Grouped Favorite, Lyrics, Queue, Download, and More into one restrained action row.
- Added subtle artwork and play/pause transitions instead of flashy motion.
- Increased playback progress refresh frequency for smoother movement while keeping playback state untouched.

## Discord branding

The Discord application ID bundled for DiyyMusic is:

```text
1518124516893528125
```

The application name, icon, OAuth scopes, and redirect configuration are controlled from the Discord Developer Portal.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v0.8.7 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.8.7-universal.apk
```

## Build locally

Requirements: Java 21 and Android SDK 37.

```bash
chmod +x gradlew
./gradlew :app:assembleFossDebug
```

The APK is generated under:

```text
app/build/outputs/apk/foss/debug/
```

## License

DiyyMusic is distributed under the GNU General Public License v3.0 and preserves the license and attribution requirements of its upstream open-source foundation.
