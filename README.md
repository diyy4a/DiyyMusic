# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 1.1.1** (`versionCode 33`)

## Changes in 1.1.1

- Fixed the bottom mini-player and navigation bar popping in/out abruptly when opening or closing the full player screen; it now fades and slides in sync with the page transition.
- Fixed bottom sheets (Song options, Add to playlist, Queue) closing instantly instead of animating away.
- Fixed the "New playlist" row in the Add to playlist sheet being misaligned instead of centered (same fix applied to the Queue row and Song options menu rows).
- Removed the placeholder "Radio" stations screen (TuneIn-style station list) and its "Get 1 month free" promo, along with all its navigation entry points.
- Replaced the "Start radio" action with a local-library-first matching algorithm (`LocalMatchQueue.kt`) that prioritizes songs from the same artist, weighted by likes and play time, before falling back to an online mix.
- Redesigned the theme-mode (and similar) picker dialogs with proper radio-style rows and a spring pop-in/pop-out animation instead of a flat, instantly-appearing list.
- Added an "Add to playlist" option to the player's Song options sheet.
- Minor performance work: cached repeated gradient allocations in the glass card component, raised the image memory cache size, and added smooth item-placement animations to song/album/artist/queue lists.

## Changes in 1.1.0

- Removed the Discord Rich Presence integration entirely (service hooks, settings UI, preferences, build config, CI variables).
- Playlists: added a "New playlist" button and dialog (Playlists screen), an "Add to playlist" button on every song row across the app (local library, downloads, albums, artists, recently played, and online search/album/playlist/artist results), and a bottom sheet to pick an existing playlist or create a new one on the fly.
- Playlist detail screen: added drag-to-reorder (only while sorted by "Custom order"), a sort menu (custom / date added / title / artist / play time, ascending or descending), a "hide video songs" filter, and a per-song "remove from playlist" button.
- Removed the two unused MetroList Wrapped images: `wrapped_playlistv1.png` and `wrapped_playlistv2.png`.
- Cleaned project documentation so `README.md` is the only Markdown file.
- Updated Codemagic and GitHub Actions artifact names to DiyyMusic 1.1.0.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v1.1.1 APK** workflow. The generated artifact is:

```text
DiyyMusic-v1.1.1-universal.apk
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
