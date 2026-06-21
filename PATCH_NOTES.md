# DiyyMusic v0.8.0 Functional Backend Patch

Apply this patch over DiyyMusic v0.7.2.

## Main fixes

- Rewires song playback to the existing MetroList/Media3 backend instead of placeholder UI callbacks.
- Replaces fragile online song radio-only launches with immediate `ListQueue` playback.
- Connects all Now Playing controls to the real player.
- Adds working Queue, Lyrics, song overflow menu, Start Radio, and Retry Playback sheets/actions.
- Makes Favorites persist, even when an online track has not been inserted into Room yet.
- Routes Profile cards and feature rows to the correct destinations.
- Adds a real Downloads collection.
- Disables minification/resource shrinking for the installable debug APK.
- Builds a universal ARM APK.

Extract the ZIP into the project root and overwrite the existing files.
