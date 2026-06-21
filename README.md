# DiyyMusic

DiyyMusic is an independent open-source Android music client with a modern pink liquid-glass interface and a rebuilt Compose UI.

## Version

Current development release: **0.7.2** (`versionCode 12`)

## DiyyMusic 0.7.2

- Fixed the Kotlin compile failure caused by passing an unsupported `onOpenRadio` argument to `LibraryScreen`.
- Kept Radio navigation available from Listen Now.
- Retained the v0.7.x Home, Search, Library, Profile, Now Playing, mini-player, bottom navigation, pink-note branding, and clean slider UI.
- Retained the persistent debug-keystore reuse logic for Codemagic and GitHub Actions.

## Build with Codemagic

Push the updated project to the `main` branch. Codemagic runs **DiyyMusic v0.7.2 APK** and publishes `DiyyMusic-v0.7.2-armeabi-v7a.apk`.

## Build with GitHub Actions

Run **Build DiyyMusic v0.7.2 APK** and download the `DiyyMusic-v0.7.2-APK` artifact.

## License

DiyyMusic is distributed under the GNU General Public License v3.0 and is not affiliated with Apple, Google, YouTube, or Discord.
