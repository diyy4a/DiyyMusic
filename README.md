# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 1.1.4** (`versionCode 36`)

## Changes in 1.1.4

- Forced fully transparent backgrounds at every wrapper level around the mini-player/nav bar to rule out any remaining stray fill behind them.
- Redesigned the startup splash: entrance animation for the logo, a soft breathing brand-colored glow, and a custom three-dot loading indicator instead of a generic spinner.
- Actually matched the full-lyrics playback dock's size to the real player screen's controls this time (previous attempt matched it to the wrong reference — the mini-player — instead of the real `PlayerControlsDock`).
- Started modernizing the icon set with Material Symbols Rounded (a softer, more rounded style closer to Apple Music's look) on the most visible surfaces: bottom navigation, mini-player, and both playback control docks (full player and full lyrics). The rest of the app's icons are unchanged for now — swapping every icon in the app is a large, ongoing job; let me know if you'd like it continued screen by screen.
- Carried forward from 1.1.3: main-tab switching rebuilt on `AnimatedContent` for reliable slide direction, and the doubled mini-player border/shadow fix.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v1.1.4 APK** workflow. The generated artifact is:

```text
DiyyMusic-v1.1.4-universal.apk
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
