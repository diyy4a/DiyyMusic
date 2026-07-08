# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 1.1.0** (`versionCode 32`)

## Changes in 1.1.0

- Removed the Discord Rich Presence integration entirely (service hooks, settings UI, preferences, build config, CI variables).
- Playlists: added a "New playlist" button and dialog (Playlists screen), an "Add to playlist" button on every song row across the app (local library, downloads, albums, artists, recently played, and online search/album/playlist/artist results), and a bottom sheet to pick an existing playlist or create a new one on the fly.
- Playlist detail screen: added drag-to-reorder (only while sorted by "Custom order"), a sort menu (custom / date added / title / artist / play time, ascending or descending), a "hide video songs" filter, and a per-song "remove from playlist" button.
- Removed the two unused MetroList Wrapped images: `wrapped_playlistv1.png` and `wrapped_playlistv2.png`.
- Cleaned project documentation so `README.md` is the only Markdown file.
- Updated Codemagic and GitHub Actions artifact names to DiyyMusic 1.1.0.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v1.1.0 APK** workflow. The generated artifact is:

```text
DiyyMusic-v1.1.0-universal.apk
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
