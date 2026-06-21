# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink Liquid Glass interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.8.5** (`versionCode 18`)

## What changed in 0.8.5

- Added real account avatar support to the Home profile button.
- Added spring-based page transitions, press feedback, bottom-navigation motion, player artwork breathing, and an animated startup logo.
- Rebuilt synced lyrics motion with smooth auto-scroll, active-line scale, opacity depth, animated color, and tap-to-seek.
- Restyled media cards with a deeper Liquid Glass presentation.
- Expanded Search with trending searches and recent-search shortcuts.
- Rebuilt Discord Rich Presence settings with a connected profile card, reconnect and disconnect controls, a compact Presence section, and a collapsed Advanced section.
- Reduced Discord Advanced settings to useful controls only: custom text, activity style, status, and two text templates.
- Fixed the duplicate `playOnlineSection` declaration left in the previous source package.
- Made the Discord application ID configurable through `DISCORD_APP_ID` in `local.properties` or the build environment.

## Discord branding

Discord shows the OAuth application name attached to the configured Discord application ID. The fallback ID still belongs to the upstream MetroList application, so its authorization page may display **MetroList**.

To display **DiyyMusic**, create a Discord application named DiyyMusic in the Discord Developer Portal, configure the same OAuth redirect URI used by this project, then provide its numeric application ID:

```properties
DISCORD_APP_ID=YOUR_DIYYMUSIC_DISCORD_APPLICATION_ID
```

For Codemagic, add `DISCORD_APP_ID` as an environment variable. This is controlled by Discord, not by a drawable or a text string inside the APK, because apparently branding needed its own bureaucracy.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v0.8.5 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.8.5-universal.apk
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
