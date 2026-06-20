# DiyyMusic

DiyyMusic is an independent open-source Android music client with a modern Apple-Music-inspired interface, a stable pink brand palette, and a polished playback experience.

## Version

Current development release: **0.6.2**

## DiyyMusic 0.6.2

- Complete interface refresh based on the supplied Apple Music Player Redesign Figma community file.
- New DiyyMusic launcher icon and in-app branding using the supplied pink-note artwork.
- Rebuilt bottom navigation and mini player with a cleaner liquid-glass treatment.
- Redesigned Listen Now, Search, Library, Player, Profile, and account-session surfaces.
- Search copy no longer exposes the underlying YouTube source.
- Stable DiyyMusic color system. Album artwork never recolors the main UI.
- Fixed shuffle-state handling in the queue sheet.
- Rounded artwork, cleaner typography, and more consistent spacing across shared components.

## Main features

- Online and local music search
- Playlists, albums, artists, podcasts, downloads, cache, lyrics, and equalizer
- Account sync and advanced local session management
- Discord Rich Presence
- FOSS Android build flavor

## Build with GitHub Actions

1. Push the project to a GitHub repository.
2. Open **Actions**.
3. Run **Build DiyyMusic v0.6.2 APK**.
4. Download the `DiyyMusic-v0.6.2-APK` artifact.

The workflow builds an installable optimized FOSS debug APK for `armeabi-v7a`.

## Source and license

DiyyMusic is distributed under the GNU General Public License v3.0. The project is derived from the open-source Metrolist project and contains substantial branding and interface changes.

DiyyMusic is not affiliated with Apple, Google, YouTube, Discord, or the creator of the reference Figma file.
