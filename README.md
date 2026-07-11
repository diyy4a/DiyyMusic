# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 1.1.3** (`versionCode 35`)

## Changes in 1.1.3

- Rebuilt the main-tab switching (Home / Search / Library / Profile) from scratch: it no longer goes through `NavHost`'s own transition system (which was silently overriding the custom animation twice in a row). Tab content now uses `AnimatedContent` directly, so the slide direction is always correct — e.g. Search → Home slides in from the left, Home → Search from the right — and it isn't fighting any competing default animation anymore.
- Fixed a doubled border/shadow on the mini-player that could look like a second faint card sitting behind the mini-player and nav bar: it had both `LiquidGlassBox`'s own edge and a second, separate outline drawn on top of it. Removed the duplicate, added a little more breathing room between the mini-player and the nav bar, and slightly lowered both components' shadow elevation.
- Aligned the mini-player shown while viewing full lyrics with the regular mini-player's corner radius and spacing.
- Polished the "New playlist" text field (icon, floating label, brand-colored focus state) and other input/option rows.
- Carried forward from 1.1.2: mini-player/nav bar visible on every screen except the full player, no more gap at the bottom of the player screen, smoother startup splash, more visible "liquid glass" cards in every theme, theme-aware full-screen Lyrics view, and a GitHub Releases update-check dialog on launch.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v1.1.3 APK** workflow. The generated artifact is:

```text
DiyyMusic-v1.1.3-universal.apk
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
