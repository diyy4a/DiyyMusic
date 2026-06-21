# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.10.0** (`versionCode 31`)

## What changed in 0.10.0

- Rebuilt the full-lyrics playback dock so it matches the main player: pink controls, compact spacing, and a rounded progress track without the Android vertical slider thumb.
- Added original bundled artwork for Search categories, Search recommendations, and Home recommendations.
- Replaced the flat Search category blocks with image cards and added a visual **For You** section.
- Replaced the single Home gradient promo with image-based **For You** and **Recommended Tonight** carousels.
- The Home greeting now updates automatically and uses **Good night** from 22:00 through 03:59.
- Preserved the Discord Rich Presence Gateway implementation from 0.9.9.

See `CHANGES-0.10.0.md` and `DISCORD-SETUP.md` for details.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v0.10.0 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.10.0-universal.apk
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
