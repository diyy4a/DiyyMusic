# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.9.4** (`versionCode 25`)

## What changed in 0.9.4

- The player no longer leaves a large unused area below the album and title.
- Live lyrics now fill the center of the normal player with previous, active, and next lines.
- Shuffle, previous, play/pause, next, and repeat are grouped at the bottom.
- Tapping the inline lyrics opens a MetroList-style full lyrics view with playback controls.
- Lyric timing now follows the Media3 clock every frame, supports playback speed and saved offsets, and uses smooth word or character progress.
- The main playback seek bar is frame-synced instead of updating in coarse intervals.
- Bumped the application to `versionCode 25` / `versionName 0.9.4`.

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

Push the project to the `main` branch and run the **DiyyMusic v0.9.4 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.9.4-universal.apk
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
