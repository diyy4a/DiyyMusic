# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink Liquid Glass interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.8.6** (`versionCode 19`)

## What changed in 0.8.6

- Switched Discord OAuth and Rich Presence to the official DiyyMusic Discord application ID: `1518124516893528125`.
- Added the same Discord application ID to the default Android build, Codemagic, and GitHub Actions environments.
- Discord authorization should now display **DiyyMusic** instead of **MetroList**, provided the OAuth configuration in the Discord Developer Portal is valid.
- Kept all UI, lyrics animation, playback, pull-to-refresh, and Discord settings improvements from version 0.8.5.

## Discord configuration

The project already uses this application ID by default:

```properties
DISCORD_APP_ID=1518124516893528125
```

In the Discord Developer Portal, the application name, icon, OAuth scopes, and redirect configuration must belong to that same application. Discord controls the authorization-page branding, because naturally even a login logo needs paperwork.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v0.8.6 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.8.6-universal.apk
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
