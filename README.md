# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.9.5** (`versionCode 26`)

## What changed in 0.9.5

- Reduced the inline lyrics card so it no longer dominates the player.
- Moved favorite, full lyrics, and queue into a compact action rail below the song information instead of leaving them at the very bottom.
- Replaced the inline lyric block swap with a continuously scrolling lyric list.
- Smoothed full-screen lyric scrolling and removed the active-line scale jump that made the text feel stiff.
- Kept playback controls at the bottom, directly after the progress bar.
- Discord Rich Presence can now be toggled as a saved preference after the account is linked. Actual publishing still requires the official Discord Social SDK and approved presence scopes.

## Discord branding

The Discord application ID bundled for DiyyMusic is:

```text
1518124516893528125
```

Enable **Public Client** in **Discord Developer Portal → OAuth2**, then register this exact redirect under **Redirects**:

```text
http://127.0.0.1:6463/callback
```

The value can be overridden with `DISCORD_REDIRECT_URI` in `local.properties` or the build environment, but the value used by the APK and the value registered in Discord must remain identical.

By default, Discord OAuth requests only `identify`, so account linking works without restricted partner scopes. Actual mobile Rich Presence requires the official Discord Social SDK and approved presence scopes. Approved builds can set:

```properties
DISCORD_SOCIAL_SDK_ENABLED=true
```

Do not enable it on an unapproved Discord application, because Discord will reject the restricted scope.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v0.9.5 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.9.5-universal.apk
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
