# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.9.3** (`versionCode 24`)

## What changed in 0.9.3

- Fixed the FOSS build failure caused by the missing `LyricsMessage` composable in the full-screen lyrics page.
- Added a centered empty/error state for “Play a song first” and “Lyrics not found for this song”.
- No playback, lyric timing, Search, Discord, or UI behavior was otherwise changed in this hotfix.
- Bumped the application to `versionCode 24` / `versionName 0.9.3`.

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

By default, Discord OAuth requests only `identify`, so account linking works without restricted partner scopes. Mobile Rich Presence requires approved Discord Social SDK access. Approved builds can set:

```properties
DISCORD_SOCIAL_SDK_ENABLED=true
```

Do not enable it on an unapproved Discord application, because Discord will reject the restricted scope.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v0.9.3 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.9.3-universal.apk
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
