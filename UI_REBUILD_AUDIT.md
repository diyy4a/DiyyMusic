# DiyyMusic UI Rebuild Audit

## Scope

DiyyMusic 0.6.2 removes the former application UI source tree and replaces it with a new Compose interface based on the supplied **Apple Music Player Redesign (Community)** Figma reference.

## New active UI source

The active UI is limited to the rebuilt files under:

- `app/src/main/kotlin/com/diyy/music/ui/AppRoot.kt`
- `app/src/main/kotlin/com/diyy/music/ui/Navigation.kt`
- `app/src/main/kotlin/com/diyy/music/ui/component/FigmaComponents.kt`
- `app/src/main/kotlin/com/diyy/music/ui/screens/`
- `app/src/main/kotlin/com/diyy/music/ui/theme/Theme.kt`

The former MetroList-derived screen, menu, player, component, and navigation files are not included in the active UI tree.

## Preserved backend

All original non-UI Kotlin source files from DiyyMusic 0.5.0 remain present. Preserved systems include:

- playback service, player connection, queue, shuffle, repeat, and media session
- online API and search data sources
- Room database, entities, DAO, migrations, and repositories
- account/session synchronization
- downloads, cache, lyrics, recognition, Discord integration, widgets, and alarms
- ViewModels and data flows used by the rebuilt interface

Non-visual wrapped models and shared UI-independent types that previously lived under the old UI package were moved to backend-neutral packages rather than deleted.

## Launcher icon

The adaptive launcher icon uses a transparent foreground containing only the music note with safe margins. The dark rounded background remains separate, preventing Android launchers from zooming and clipping the note.

## Validation performed

- all Android resource XML files parsed successfully
- all drawable references used by the rebuilt UI resolve to existing resources
- no former UI package paths remain in the active source tree
- all original non-UI Kotlin source files are still present
- Kotlin parser check found no syntax-level errors in the rebuilt UI source

A full Android build was not completed in the packaging container because the required Gradle distribution was not available offline. The included GitHub Actions workflow performs the real Android build in a networked runner.
