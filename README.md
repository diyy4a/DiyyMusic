# DiyyMusic

DiyyMusic is an independent open-source Android music client. Version **0.6.1** replaces the former application interface with a new UI built from the supplied **Apple Music Player Redesign (Community)** Figma reference while preserving the playback, API, database, download, session, synchronization, lyrics, and service layers.

## DiyyMusic 0.6.1

- Removed the previous in-app UI source tree instead of reskinning its components.
- Rebuilt the application shell, navigation, mini player, Listen Now, Radio, Library, Categories/Search, full player, profile, history, collection, display options, settings, and crash screen.
- Uses the Figma navigation model: **Listen Now · Radio · Library · Search**.
- Uses a fixed red/pink editorial palette, white canvas, compact dividers, and restrained rounded surfaces.
- Search uses the existing online backend and no longer exposes its underlying provider in the interface.
- Existing playback, queue, shuffle, repeat, local database, online API, account session, cache, downloads, widgets, Discord, recognition, and sync code remains in the project.
- Rebuilt adaptive and legacy launcher assets with a transparent, safely inset foreground so Android does not zoom the music note.

## Build with GitHub Actions

1. Push the project to GitHub.
2. Open **Actions**.
3. Run **Build DiyyMusic v0.6.1 APK**.
4. Download the `DiyyMusic-v0.6.1-APK` artifact.

The workflow builds the optimized FOSS debug APK for `armeabi-v7a`.

## License

DiyyMusic is distributed under the GNU General Public License v3.0. It is not affiliated with Apple, Google, YouTube, Discord, or the creator of the reference Figma file.
