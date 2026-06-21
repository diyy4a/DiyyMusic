# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.9.1** (`versionCode 22`)

## What changed in 0.9.1

- Reduced the in-app brand mark and added more launcher-icon safe area.
- Hidden the mini player until a real track exists.
- Rebuilt preference selection as a rounded theme-aware dialog instead of the cramped platform popup.
- Anchored the Now Playing action bar near the bottom on normal-height devices and reduced oversized artwork on compact screens.
- Reworked synced lyrics using MetroList's centered-scroll behavior, readable inactive lines, longer easing, tap-to-seek, and frame-driven word highlighting.
- Added a working 8-band equalizer with presets, preamp, and manual band controls backed by the existing audio processor.
- Expanded dark and pure-black palettes so cards, controls, outlines, dialogs, and player surfaces change with the theme.
- Changed Discord OAuth to one fixed loopback redirect URI so it can be registered once in the Discord Developer Portal.
- Reduced unnecessary visual work while keeping playback progress and karaoke timing isolated from the rest of the screen.

## Discord branding

The Discord application ID bundled for DiyyMusic is:

```text
1518124516893528125
```

Register this exact redirect under **Discord Developer Portal → OAuth2 → Redirects**:

```text
http://127.0.0.1:6463/callback
```

The value can be overridden with `DISCORD_REDIRECT_URI` in `local.properties` or the build environment, but the value used by the APK and the value registered in Discord must remain identical.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v0.9.1 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.9.1-universal.apk
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
